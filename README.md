# Wurbelizer

The Wurbelizer is a code generator, written in Java, using Java as its
template language and embedded within source files, usually Java. Rather than creating entire
source files, it modifies well-defined regions of code. It is lightweight, easy to
integrate, easy to learn — and yet flexible and powerful.

This document covers the usage and integration. New here? Start with the
[5-minute quickstart](./src/docs/quickstart.md), or visit
[wurbelizer.org](https://wurbelizer.org) for the full overview.

## Key Features

- **Java is the meta language.** The logic that decides *what* to generate is written
  in ordinary Java, not in a bespoke template language. A `.wrbl` file is just output
  text with Java escaped into it via the level-switching markers (`@[ … ]@` for
  statements, `@( … )@` for expressions), and the wurbiler translates that template
  into a real Java class that is compiled and run. The practical consequences:
  - **No second language to learn.** Loops, conditionals, method calls and types are
    plain Java — if you know the target language, you already know the meta language.
  - **The full Java ecosystem is available at generation time.** Wurblets can use any
    library on the classpath to read models (databases, XML/JSON, annotations,
    reflection) and drive the output.
  - **Full tooling support.** Because wurblets compile to normal classes, you get the
    compiler's type checking, your IDE's completion and debugging, and ordinary stack
    traces when something goes wrong — no opaque template engine in between.
  - **One language across all three levels.** The generator logic, the generated
    code, and the application being modified are all Java, so there is no impedance
    mismatch between writing a generator and reading its result.

- **Three code levels instead of two.** Unlike typical generators that only know
  a generator level and a generated-code level, the Wurbelizer adds a third: the
  *source level*. A wurblet's own source mixes the *wurblet level* (the generator's
  Java logic) with the *output level* (the text it emits), while the *source level*
  is your application's code that the wurbler reads and rewrites. This separation
  keeps templates expressive without ever leaving plain Java.

- **In-place generation into guarded blocks.** Generated code does not live in
  separate, throw-away files. The wurbler inserts and updates it directly inside
  your hand-written source files, confined to clearly delimited *guarded blocks*.
  You edit around the generated regions freely; regeneration only touches what it
  owns, so machine-written and human-written code coexist in the same file.

- **Iterative generation until stable.** The wurbler re-runs the wurblets until the
  output stops changing, because generated code can itself contain new wurblet
  anchors. This makes multi-stage and self-referential generation possible, and a
  file is only written back to disk when something actually changed.

- **Lightweight and easy to integrate.** A small, LGPL-2.1 library that is quick to
  learn yet flexible and powerful. It drops into existing builds through first-class
  [Maven](./src/docs/maven.md) and [Ant](./src/docs/ant.md) integration, or a
  [console](./src/docs/console.md) runner, with no heavyweight runtime or framework
  to adopt.

- **Pluggable guard types and source-level tooling.** Guarded blocks are recognized
  through swappable [guard types](./src/docs/guard-types.md) — NetBeans/IntelliJ,
  Eclipse and Visual Studio styles ship in the box, and custom ones register via the
  Java service-provider mechanism. Combined with here-documents and variables at the
  source level, the same wurblet adapts across IDEs and project conventions.

## Used in the Wild: Tentackle

The Wurbelizer did not start out as a standalone project. It emerged from practical
development needs, originally created alongside the Java framework
[Tentackle](https://bitbucket.org/krake-oss/tentackle/src/master/), which heavily
relies on generative programming, and was later opened up for anyone to use.

Tentackle remains a real-world, large-scale user of the Wurbelizer. It is a Java
framework for building multi-tier enterprise applications around *Persistent Domain
Objects* (PDOs), where a single domain model spans everything from the database to the
desktop client.

Tentackle keeps that model as the single source of truth and lets the Wurbelizer turn
it into code at build time. The model lives in the PDO interfaces, and wurblets read it
to generate the persistence layer, the DDL and the database migrations directly into the
project's own sources. Because the generated code is merged into guarded blocks of
hand-written classes, application developers extend the PDOs freely while regeneration
keeps the boilerplate in sync — a concrete demonstration of the model-driven, in-place
generation the Wurbelizer is designed for.

## Terms and Definitions

- **wurbelizer**: the code generator project as a whole.
- **wurbiler**: translates the template source code into Java code and serialized text files.
- **wurbler**: parses the application's source files and invokes the wurblets to insert
  or update generated code in the source files within well-defined regions.
- **wurblet**: a code generator that is executed by the wurbler.
- **guarded block**: a well-defined region within a source file, generated by a wurblet.
- **guard type**: a pattern describing the bounds of a guarded block.
- **wurblet anchor**: a Java comment that describes a guarded block by its name,
  the wurblet generating its contents and optional arguments (usually to locate some
  model or other parameters further specifying the code generation process).
- **wurbile**: verb describing the translation and compilation of a wurblet
- **wurbel**: verb describing the code generation


## Contents

[0. Quickstart](./src/docs/quickstart.md)

[1. Code Levels](./src/docs/code-levels.md)

[2. Build Tool Integration](./src/docs/integration.md)

[3. Wurblet Level Syntax](./src/docs/wurblet-syntax.md)

[4. Source Level Syntax](./src/docs/source-syntax.md)

[5. Guard Types](./src/docs/guard-types.md)

[6. Writing Wurblets](./src/docs/writing-wurblets.md)
