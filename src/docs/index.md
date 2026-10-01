# Index

An alphabetical index of terms and concepts across the Wurbelizer documentation.
See the [Table of Contents](toc.md) for the documents in reading order.

[← Back to README](../../README.md)

---

## A

- **AbstractJavaWurblet** — [Writing Wurblets](writing-wurblets.md#wurbelizer-api),
  [Quickstart: base class](quickstart.md#the-wurblet-base-class)
- **AbstractWurblet** — [Writing Wurblets](writing-wurblets.md#wurbelizer-api)
- **analyze directory (`analyzeDir`, `getAnalyzeFile`)** — [Writing Wurblets: The Container](writing-wurblets.md#the-container),
  [Maven: Goals and parameters](maven.md#goals-and-parameters), [Ant: Wurbel](ant.md#wurbel)
- **anchor** — *see* wurblet anchor
- **Ant integration** — [Ant Integration](ant.md)
- **ant namespace (`"ant"`)** — [Writing Wurblets: Wurbler Properties](writing-wurblets.md#wurbler-properties),
  [Ant: Wurbel](ant.md#wurbel)
- **args directive** — [Wurblet Level Syntax: args](wurblet-syntax.md#args)
- **arguments (wurblet)** — [Source Level Syntax: Wurblet Anchor](source-syntax.md#wurblet-anchor),
  [args directive](wurblet-syntax.md#args)
- **arguments, quoting (`"..."`, `'...'`)** — [Source Level Syntax: Wurblet Anchor](source-syntax.md#wurblet-anchor)

## B

- **backslash escaping / continuation line** —
  [Source Level Syntax](source-syntax.md), [Wurblet Level Syntax](wurblet-syntax.md)
- **build order (reactor, `wurbletDependencies`)** — [Maven Integration: Parallel builds](maven.md#parallel-builds)
- **build tool integration** — [Build Tool Integration](integration.md)
- **built-in variables (`classname`, `packagename`, ...)** — [Source Level Syntax: Built-in Variables](source-syntax.md#built-in-variables)

## C

- **class realm (wurblet classloader)** — [Maven Integration: Parallel builds](maven.md#parallel-builds),
  [Writing Wurblets: Thread safety](writing-wurblets.md#thread-safety)
- **cleanup() (wurblet)** — [Writing Wurblets: Wurblet Lifecycle](writing-wurblets.md#wurblet-lifecycle)
- **code directive** — [Wurblet Level Syntax: code](wurblet-syntax.md#code)
- **code levels** — [Code Levels](code-levels.md),
  [example](code-levels.md#the-three-levels-in-one-example)
- **code switching (`@[ ]@`)** — [Wurblet Level Syntax: Code Switching](wurblet-syntax.md#code-switching)
- **coffeebytes guard type** — [Guard Types](guard-types.md)
- **comment directive** — [Wurblet Level Syntax: comment](wurblet-syntax.md#comment)
- **config directive** — [Wurblet Level Syntax: config](wurblet-syntax.md#config)
- **conditional anchor (`test`)** — [Source Level Syntax: Conditional Anchor](source-syntax.md#conditional-anchor)
- **console integration** — [Console Integration](console.md)
- **continuation line** — [Source Level Syntax](source-syntax.md), [Wurblet Level Syntax](wurblet-syntax.md)
- **currentDir / currentFile** — [Maven: Goals and parameters](maven.md#goals-and-parameters),
  [Wurblet Level Syntax: include](wurblet-syntax.md#include)

## D

- **directives (wurbiler)** — [Wurblet Level Syntax: Wurbiler Directives](wurblet-syntax.md#wurbiler-directives)
- **directives (wurbler)** — [Source Level Syntax: Wurblet Anchor](source-syntax.md#wurblet-anchor)
- **discard (heap file)** — [Writing Wurblets: Errorhandling](writing-wurblets.md#errorhandling),
  [Heap Files API](writing-wurblets.md#heap-files-api)
- **destdir (Ant)** — [Ant: Wurbile](ant.md#wurbile)
- **DTO tutorial** — [Quickstart](quickstart.md)
- **dry run (`dryRun`)** — [Maven: Goals and parameters](maven.md#goals-and-parameters), [Ant: Wurbel](ant.md#wurbel)

## E

- **editor fold (`fold=`)** — [Source Level Syntax: Editor Fold](source-syntax.md#editor-fold)
- **empty block comment (`/**/`, `/*@*/`)** —
  [Source Level Syntax: Unnamed Anchor](source-syntax.md#unnamed-anchor)
- **error handling / exceptions** — [Writing Wurblets: Errorhandling](writing-wurblets.md#errorhandling)
- **escaping (`\@`, `\ `)** — [Wurblet Level Syntax: Escaping](wurblet-syntax.md#escaping)
- **extends directive** — [Wurblet Level Syntax: extends](wurblet-syntax.md#extends)
- **extra indentation (`indent=`)** — [Source Level Syntax: Extra Indentation](source-syntax.md#extra-indentation)
- **extra properties** — [Writing Wurblets: Wurbler Properties](writing-wurblets.md#wurbler-properties),
  [Maven: Goals and parameters](maven.md#goals-and-parameters)

## F

- **filecontents (property)** — [Writing Wurblets: Wurbler Properties](writing-wurblets.md#wurbler-properties)
- **filesets (Maven)** — [Maven: real-world configuration](maven.md#a-real-world-configuration),
  [Processing Order and Phases](source-syntax.md#processing-order-and-phases)
- **firstPhase / lastPhase (Maven)** — [Maven: Goals and parameters](maven.md#goals-and-parameters)
- **fold type (`foldtype`)** — [Guard Types: Fold Types](guard-types.md#fold-types),
  [Source Level Syntax: Editor Fold](source-syntax.md#editor-fold)

## G

- **guard type** — [Guard Types](guard-types.md)
- **guard type, custom** — [Guard Types: Custom Guard Types](guard-types.md#custom-guard-types)
- **guarded block** — [Code Levels](code-levels.md), [Guard Types](guard-types.md),
  [example](source-syntax.md#anchor-and-guarded-block)
- **guarded block, lifecycle** — [Source Level Syntax: Lifecycle of a Guarded Block](source-syntax.md#lifecycle-of-a-guarded-block)
- **guided tour (Tentackle wurblets)** — [Writing Wurblets: A guided tour](writing-wurblets.md#a-guided-tour)

## H

- **heap file (`.`-prefixed)** — [Source Level Syntax: Here Documents](source-syntax.md#here-documents),
  [Cross-file generation](source-syntax.md#cross-file-generation-with-heap-files)
- **heap files API (`HeapFileFactory`, `WurbelHelper.openReader`)** — [Writing Wurblets: Heap Files API](writing-wurblets.md#heap-files-api)
- **HeapStream** — [Source Level Syntax: Cross-file generation](source-syntax.md#cross-file-generation-with-heap-files),
  [Wurblet Level Syntax: to](wurblet-syntax.md#to), [Heap Files API](writing-wurblets.md#heap-files-api)
- **here document (`@> @<`)** — [Source Level Syntax: Here Documents](source-syntax.md#here-documents),
  [Quickstart: source file](quickstart.md#the-source-file),
  [model example](source-syntax.md#example-the-model-next-to-the-code)
- **here document, origin line (`#@`, `--nosource`)** — [Source Level Syntax: Here-Document Details](source-syntax.md#here-document-details)

## I

- **implements directive** — [Wurblet Level Syntax: implements](wurblet-syntax.md#implements)
- **import directive** — [Wurblet Level Syntax: import](wurblet-syntax.md#import)
- **include directive** — [Wurblet Level Syntax: include](wurblet-syntax.md#include)
- **includeAnchors / includeWurblets / excludeAnchors / excludeWurblets (Maven)** —
  [Maven: Goals and parameters](maven.md#goals-and-parameters)
- **indent directive** — [Wurblet Level Syntax: indent](wurblet-syntax.md#indent)
- **inline wurblet** — [Source Level Syntax: Unnamed Anchor](source-syntax.md#unnamed-anchor)
- **inline tags (`-`, `<`, `+`, `>`)** — [Source Level Syntax: Unnamed Anchor](source-syntax.md#unnamed-anchor)
- **Inject wurblet (Tentackle)** — [Source Level Syntax: Unnamed Anchor](source-syntax.md#unnamed-anchor)
- **isThreadSafe (wurblet)** — [Writing Wurblets: Thread safety](writing-wurblets.md#thread-safety),
  [Maven Integration: Parallel builds](maven.md#parallel-builds)
- **invocation count (pass)** — [Writing Wurblets: The Container](writing-wurblets.md#the-container)
- **`.incl` fragments** — [Wurblet Level Syntax: include](wurblet-syntax.md#include),
  [Writing Wurblets: parent-class pattern](writing-wurblets.md#the-parent-class-pattern)

## J

- **jar URL cache (`setUseCaches(false)`)** — [Writing Wurblets: Thread safety](writing-wurblets.md#thread-safety)
- **JavaSourceType** — [Guard Types: Source Types](guard-types.md#source-types),
  [Built-in Variables](source-syntax.md#built-in-variables)

## L

- **level switching** — [Wurblet Level Syntax: Level Switching](wurblet-syntax.md#level-switching)
- **lifecycle (wurblet)** — [Writing Wurblets: Wurblet Lifecycle](writing-wurblets.md#wurblet-lifecycle)
- **logging** — [Writing Wurblets: Logging](writing-wurblets.md#logging)

## M

- **Maven 4** — [Maven Integration: Maven 4](maven.md#maven-4)
- **Maven integration** — [Maven Integration](maven.md),
  [real-world configuration](maven.md#a-real-world-configuration)
- **Maven parameters** — [Maven: Goals and parameters](maven.md#goals-and-parameters)
- **model** — [Quickstart: the model](quickstart.md#the-model),
  [Source Level Syntax: Here Documents](source-syntax.md#here-documents),
  [Code Levels: example](code-levels.md#the-three-levels-in-one-example)
- **ModelWurblet / DbModelWurblet (Tentackle)** —
  [Wurblet Level Syntax: extends](wurblet-syntax.md#extends),
  [Writing Wurblets: parent-class pattern](writing-wurblets.md#the-parent-class-pattern)

## N

- **netbeans guard type (default)** — [Guard Types](guard-types.md)

## O

- **output level** — [Code Levels](code-levels.md)

## P

- **package directive** — [Wurblet Level Syntax: package](wurblet-syntax.md#package)
- **parallel builds (`mvn -T`)** — [Maven Integration: Parallel builds](maven.md#parallel-builds),
  [Writing Wurblets: Thread safety](writing-wurblets.md#thread-safety)
- **phase directive** — [Wurblet Level Syntax: phase](wurblet-syntax.md#phase)
- **phase wurbler directive (`phase=`)** — [Source Level Syntax: Execution Phase](source-syntax.md#execution-phase)
- **phases, processing order** — [Source Level Syntax: Processing Order and Phases](source-syntax.md#processing-order-and-phases)
- **process() (wurblet)** — [Writing Wurblets: Wurblet Lifecycle](writing-wurblets.md#wurblet-lifecycle)
- **project / projectRoot / parentProject (properties)** — [Maven: Goals and parameters](maven.md#goals-and-parameters)
- **properties (wurbler)** — [Writing Wurblets: Wurbler Properties](writing-wurblets.md#wurbler-properties)

## Q

- **quickstart** — [Quickstart](quickstart.md)

## S

- **SourceRegion (interface)** — [Guard Types: Custom Guard Types](guard-types.md#custom-guard-types)
- **SourceType (interface)** — [Guard Types: Source Types](guard-types.md#source-types)
- **source type, default (`NoneSourceType`)** — [Guard Types: Source Types](guard-types.md#source-types)
- **source level** — [Code Levels](code-levels.md), [Source Level Syntax](source-syntax.md)
- **skeleton wurblet** — [Writing Wurblets: Skeleton](writing-wurblets.md#skeleton-extending-abstractjavawurblet)

## T

- **template (`.wrbl`)** — [Quickstart: the template](quickstart.md#the-template),
  [Wurblet Level Syntax](wurblet-syntax.md)
- **Tentackle** — [README](../../README.md#used-in-the-wild-tentackle),
  [Writing Wurblets](writing-wurblets.md#examples),
  [guided tour](writing-wurblets.md#a-guided-tour),
  [Maven configuration](maven.md#a-real-world-configuration),
  [parallel builds](maven.md#parallel-builds)
- **thread safety** — [Writing Wurblets: Thread safety](writing-wurblets.md#thread-safety),
  [Maven Integration: Parallel builds](maven.md#parallel-builds)
- **to directive** — [Wurblet Level Syntax: to](wurblet-syntax.md#to)

## U

- **unnamed anchor** — [Source Level Syntax: Unnamed Anchor](source-syntax.md#unnamed-anchor)

## V

- **value extraction (`@( )@`)** — [Wurblet Level Syntax: Value Extraction](wurblet-syntax.md#value-extraction)
- **variables (`$var`, `${var}`)** — [Source Level Syntax: Variables](source-syntax.md#variables)
- **variables, resolution order (`\$`)** — [Source Level Syntax: Resolution Order](source-syntax.md#resolution-order)
- **visualstudio guard type** — [Guard Types](guard-types.md)

## W

- **WurbelContext** — [Writing Wurblets: Thread safety](writing-wurblets.md#thread-safety)
- **wurbelDir** — [Maven: Goals and parameters](maven.md#goals-and-parameters)
- **wurbel** — [Code Levels](code-levels.md), [README: terms](../../README.md#terms-and-definitions)
- **wurbiler** — [Build Tool Integration](integration.md), [README: terms](../../README.md#terms-and-definitions)
- **wurbiler directives** — [Wurblet Level Syntax: Wurbiler Directives](wurblet-syntax.md#wurbiler-directives)
- **wurbler** — [Code Levels](code-levels.md), [README: terms](../../README.md#terms-and-definitions)
- **wurblet** — [Writing Wurblets](writing-wurblets.md), [README: terms](../../README.md#terms-and-definitions)
- **wurblet anchor (`@wurblet`)** — [Source Level Syntax: Wurblet Anchor](source-syntax.md#wurblet-anchor)
- **wurbler container (`Wurbler`)** — [Writing Wurblets: The Container](writing-wurblets.md#the-container)
- **wurblet level** — [Code Levels](code-levels.md), [Wurblet Level Syntax](wurblet-syntax.md)
- **wurbletDependencies (Maven)** — [Maven Integration: real-world configuration](maven.md#a-real-world-configuration),
  [Parallel builds](maven.md#parallel-builds)
- **wurbletPaths** — [Source Level Syntax](source-syntax.md#wurblet-anchor),
  [Wurblet Level Syntax: package](wurblet-syntax.md#package)
- **`.wurb` property file** — [Ant Integration](ant.md#wurbel),
  [Writing Wurblets: Wurbler Properties](writing-wurblets.md#wurbler-properties),
  [example](source-syntax.md#example-variables-in-a-wurb-file)

---

**Navigation:** [Table of Contents](toc.md) | [Index](index.md) | [Start reading →](quickstart.md)
