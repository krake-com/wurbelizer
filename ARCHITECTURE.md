# Architecture

This document is a design overview of the Wurbelizer for people who want to work on
its code. It explains how the parts fit together and why. For how to *use* the
Wurbelizer, see the [README](README.md) and the
[Table of Contents](src/docs/toc.md) of the documentation.

## The Big Picture

The Wurbelizer works in two separate steps, usually bound to two build phases:

```
 wurbile (compile the generators)

   Foo.wrbl ──> Wurbiler ──┬──> Foo.java ──(javac)──> Foo.class
                           └──> Foo.ser   (fixed output-level text)

 wurbel (run the generators)

   Bar.java ──> Wurbler ──> SourceDocument ──> for each @wurblet anchor:
      ^            |                             load Foo.class + Foo.ser
      |            |                             run it, capture its output
      |            |                             merge into the guarded block
      └────────────┘
       write back if changed, repeat until stable
```

- The **wurbiler** translates a wurblet source (`.wrbl`) into a Java class and a
  serialized file holding the invariant text. It runs once per wurblet.
- The **wurbler** scans an application source file for wurblet anchors, runs the
  compiled wurblets and merges their output into guarded blocks of that same file.
  It runs once per source file and phase.

The two steps share no state except the compiled artifacts. Wurblets are ordinary
classes on a classpath and are usually built in one module and used in others.

The terms *wurblet level*, *output level* and *source level* used below are explained
in [Code Levels](src/docs/code-levels.md).

## Modules

| Module                    | Role                                                       |
|---------------------------|------------------------------------------------------------|
| `wurbelizer`              | the core library, contains all of the logic described here |
| `wurbelizer-maven-plugin` | Maven mojos driving the core                               |
| `ant-wurbelizer`          | Ant tasks driving the core                                 |
| `console-wurbelizer`      | command-line runners, illustrative                         |

The core has no dependency on any build tool. The integrations are thin drivers. They
collect the files, set up the configuration and logging, invoke the core per file and
report the results.

The core is split into four packages:

| Package                  | Contents                                                                                                                      |
|--------------------------|-------------------------------------------------------------------------------------------------------------------------------|
| `org.wurbelizer.wurbile` | the wurbiler: `Wurbiler`, `AbstractWurbiler`, `SourceWurbiler`                                                                |
| `org.wurbelizer.wurbel`  | the wurbler: `Wurbler`, `AbstractWurbler`, `SourceWurbler`, the source document model, heap and source files, `WurbelContext` |
| `org.wurbelizer.wurblet` | what a wurblet sees: `Wurblet`, `AbstractWurblet`, `AbstractJavaWurblet`, `WurbletData`                                       |
| `org.wurbelizer.misc`    | `Logger`, `Verbosity`, `Settings`, `Version`, `Constants`                                                                     |

## The Wurbiler

`AbstractWurbiler` is a hand-written, character-driven translator. It reads a `.wrbl`
stream that starts at the output level and switches levels at the `@[ ]@`, `@( )@`
and `@{ }@` markers:

- Text at the **output level** is not embedded into the generated Java code. It is
  appended to a list of strings, the *fixed source*, and replaced in the Java code by
  `out.print(source[n])`.
- Text at the **wurblet level** is copied verbatim into the Java code. The markers
  are not interpreted there, so wurblets can generate wurblets.
- `@(expr)@` becomes `out.print(expr)`.
- **Directives** such as `@{package}@`, `@{extends}@` or `@{phase}@` do not produce
  code. They set properties of the wurbiler which shape the generated class.
  `@{include}@` pushes a `NestedReader` onto a reader stack.

`SourceWurbiler` wraps this translation for files. It writes two artifacts into the
package directory of the wurblet:

- `<Name>.java`, a class extending `AbstractWurblet` (or the class named by
  `@{extends}@`). The translated code becomes the body of the class. The generated
  `run()` method delegates to a method `wurbel()`, which every wurblet source has to
  declare at the wurblet level.
- `<Name>.ser`, a serialized `WurbletData` with the fixed source and, if set by
  `@{args}@`, the preset arguments.

Keeping the output-level text out of the Java code avoids all quoting and escaping
problems and the size limits of string constants, whatever the wurblet generates.
The price is that a wurblet class is only usable together with its `.ser` resource,
which is why the wurbler loads both.

See [Wurblet Syntax](src/docs/wurblet-syntax.md) for the language itself.

## The Wurbler

### Container and wurblet

A `Wurbler` is the *container* of a wurblet. The `Wurblet` interface is small:
`run()`, `process()`, `cleanup()`, plus phase, configuration and thread-safety
accessors. Everything a wurblet needs from its environment it gets from the
container: the fixed source, the output stream, the arguments, the properties, the
logger.

`AbstractWurbler.loadWurblet` resolves the wurblet class by name, trying the
configured wurblet packages for unqualified names. It instantiates the class,
attaches it to the container and deserializes the `.ser` resource next to it. A new
wurblet instance is created for every anchor and pass, so wurblets are stateless
between invocations unless they keep state on purpose, for example in heap files.

### The source document

`SourceDocument` holds a source file as a `StringBuilder` partitioned into
`SourceElement`s of type `CODE`, `SINGLE_COMMENT`, `BLOCK_COMMENT` or `GUARDED`.
Comment elements are scanned for sub elements:

- `SourceWurbletSubElement`, a wurblet anchor,
- `SourceHereSubElement`, a here-document,
- `SourcePropertySubElement`, a variable block.

The document is the only place that modifies the text. It offers `insertGuarded`,
`replaceGuarded`, `deleteGuarded` and `replaceInline`. This scanner is deliberately
not a Java parser. It only knows comments, so the wurbler works on any file whose
comments it understands, even if the code does not compile yet.

Two SPIs, both loaded with `ServiceLoader` through `WurbelHelper`, adapt the wurbler
to its surroundings:

- `SourceRegion`, the *guard type*, creates and recognizes the begin and end
  comments of a guarded block. See [Guard Types](src/docs/guard-types.md).
- `SourceType` is selected by file extension. It extracts properties from the
  document, for example the class name of a Java file, and decides whether smart
  indent applies.

### The per-file loop

`SourceWurbler.wurbel()` processes a single source file for a single phase:

1. Read the file through `SourceFileFactory` and build a `SourceDocument`. Extract
   the source type's properties and rebuild the document until the properties are
   stable, because they may be referenced in anchor arguments.
2. Walk the elements. Write here-documents to heap files or to disk.
3. For each anchor: evaluate the wurbler directives, load the wurblet and run it if
   it belongs to the current phase. The output is captured in memory, passed through
   `Wurblet.process()`, indented and then merged into the guarded block of the same
   name. The block is created if it does not exist yet.
4. Delete guarded blocks that no anchor refers to anymore.
5. Compare the result with the text the pass started with. If it is the same, stop.
   Otherwise, write the file and start over with the new text.

The loop in step 5 exists because generated code may contain new anchors. The pass
number is available to wurblets as the invocation count. Because of the comparison,
a file is only written when its contents have changed, which keeps incremental
builds and file timestamps intact.

A failing wurblet does not abort the file. Its error is logged and its stack trace
is written into the guarded block as a comment, so that the problem shows up where
it originates. Only a `WurbelTerminationException` stops the run.

### Phases and the driver loop

A wurblet runs in a *phase*, set by `@{phase}@` or overridden per anchor. Phases let
wurblets depend on the results of others across files. The loop over the phases is
not part of the core. It belongs to the driver, such as `AbstractWurbelMojo`:

- **Phase 0** processes here-documents only. This updates models kept in
  here-documents before any wurblet reads them.
- While scanning, each `SourceWurbler` adds the phases it finds to a shared set. The
  driver continues with the next higher phase in that set until none is left.
- Within a phase, all files are processed in sorted order. If a wurblet changed a
  here-document on disk, the phase starts over.

Heap files are cleared at the start of each such round. They therefore live across
all source files of a round, which makes them the channel for
[cross-file generation](src/docs/source-syntax.md#cross-file-generation-with-heap-files).

See [Processing Order and Phases](src/docs/source-syntax.md#processing-order-and-phases)
for the user's view.

## State and Concurrency

The API available to wurblets is static: `Settings`, `WurbelHelper`,
`HeapFileFactory` and `SourceFileFactory`. Behind these accessors, all state of a
run is kept in a `WurbelContext`: the encoding, the extra properties, the heap
files, the source files and the wurblet classloader.

A context is bound to the current thread with a `ScopedValue`. The Maven mojos create
a fresh context per execution, so concurrent executions in a parallel build do not
see each other's state. Callers that bind nothing, such as the Ant tasks, the console
runners and the tests, share one global context. This keeps the static API, and with
it existing wurblets, working without changes.

Wurblets themselves may not be thread-safe, for example if they use static fields.
`Wurblet.isThreadSafe()` defaults to `false` in `AbstractWurblet`. The wurbler takes
a JVM-wide lock, `AbstractWurbler.WURBLET_LOCK`, before loading a wurblet and
releases it early only if the instance reports to be thread-safe. All other wurblets
run one at a time, regardless of the context they run in.

See [Thread safety](src/docs/writing-wurblets.md#thread-safety) and
[Parallel builds](src/docs/maven.md#parallel-builds).

## Build Tool Integration

### Maven

| Goal          | Mojo             | Default phase          |
|---------------|------------------|------------------------|
| `wurbile`     | `WurbileMojo`    | `generate-sources`     |
| `wurbel`      | `WurbelMojo`     | `process-sources`      |
| `test-wurbel` | `TestWurbelMojo` | `process-test-sources` |

`AbstractWurbelizerMojo` is the common base. Its `execute()` binds a new
`WurbelContext` and delegates to `doExecute`. `AbstractWurbelMojo` adds the driver
loop described above and is shared by `WurbelMojo` and `TestWurbelMojo`. All mojos
are declared `threadSafe`.

Classloading needs special care, because the wurblets are not part of the plugin:

- `ClassRealmConfigurator` (`set-classrealm`) hands the plugin's class realm to the
  wurbel mojos.
- For the configured `wurbletDependencies`, the mojo resolves the artifacts and
  creates a child realm per execution. The realm imports the core packages from the
  plugin's realm, so that wurblets and the wurbler share the same `Wurblet` and
  `Wurbler` classes. It is set as the context's wurblet classloader and disposed
  after the execution.

See [Maven Integration](src/docs/maven.md).

### Ant and console

`AntWurbiler` and `AntWurbler` are Ant tasks. `AntWurbler` implements the same driver
loop as the Maven mojo on top of `SourceWurbler`. See [Ant](src/docs/ant.md).

`ConsoleWurbiler` is a `main` class around `SourceWurbiler`. `ConsoleWurbler` is a
different kind of container. It extends `AbstractWurbler` directly and runs a single
wurblet with arguments from the command line, without a source document. See
[Console](src/docs/console.md).

Both use the global context.

## Extension Points

| Extension         | How                                                                         |
|-------------------|-----------------------------------------------------------------------------|
| wurblet           | a `.wrbl` file, optionally with a hand-written base class via `@{extends}@` |
| guard type        | implement `SourceRegion`, register in `META-INF/services`                   |
| source type       | implement `SourceType`, register in `META-INF/services`                     |
| wurblet selection | implement `WurbletExecutionFilter` and pass it to the `SourceWurbler`       |
| build tool        | drive `SourceWurbiler` and `SourceWurbler`, provide a `Logger`              |
| container         | extend `AbstractWurbler`, as the `ConsoleWurbler` does                      |

## Design Principles

- **Java all the way down.** Wurblets compile to plain classes. There is no template
  interpreter at generation time.
- **Generate in place.** Generated code lives in guarded blocks of hand-written
  files. The anchor in a comment is the single source of truth for a block. Remove
  the anchor and the block disappears.
- **Idempotent.** Running the wurbler on an up-to-date file changes nothing and
  writes nothing.
- **Comments only.** The wurbler understands comments, not the language around
  them.
- **Small core.** The core depends on no build tool and no parser library, and the
  integrations contain no generation logic.
