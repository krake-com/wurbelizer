# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

The Wurbelizer is a lightweight Java code generator distributed under LGPL 2.1. It operates at three code levels:

1. **Wurblet level** — the generator's own Java code (`.wrbl` source files)
2. **Output level** — the text/code the wurblet produces
3. **Source level** — the application's source files that the wurbler scans and modifies in-place

The core pipeline:
- A **wurbiler** compiles `.wrbl` files into a Java class + a `.ser` file (serialized fixed-source strings)
- A **wurbler** scans application sources for `@wurblet` anchors, instantiates the compiled wurblets, and merges generated code into guarded blocks within those source files
- The wurbler repeats until no more changes occur (generated code can itself contain new anchors)

## Documentation

This repository ships a documentation set:

- [`README.md`](README.md) — the public-facing overview.
- Project-wide docs live in `src/docs/`, in this reading order:
  - [`quickstart.md`](src/docs/quickstart.md) — 5-minute DTO-generating tutorial
  - [`code-levels.md`](src/docs/code-levels.md) — the three code levels (wurblet / output / source)
  - [`integration.md`](src/docs/integration.md) — build-tool integration, linking [`maven.md`](src/docs/maven.md), [`ant.md`](src/docs/ant.md), [`console.md`](src/docs/console.md)
  - [`wurblet-syntax.md`](src/docs/wurblet-syntax.md) — `@`-based `.wrbl` syntax and wurbiler directives
  - [`source-syntax.md`](src/docs/source-syntax.md) — anchors, here-documents and variables at the source level
  - [`guard-types.md`](src/docs/guard-types.md) — the pluggable guarded-block patterns
  - [`writing-wurblets.md`](src/docs/writing-wurblets.md) — the Wurbelizer API for building wurblets
- [`toc.md`](src/docs/toc.md) is the reading-order index; [`index.md`](src/docs/index.md) is the alphabetical term index.

When adding or changing a document, keep it in sync with `toc.md` and `index.md`.

Use American English spelling for all code comments and documentation (e.g. "behavior", "serialize", "color").

## Build Commands

Requires Java 25+ and Maven 3.9.10+. The build and the plugin also work with Maven 4 (verified with 4.0.0-rc-7).
On Maven 4, `-Dfile.encoding=UTF-8` causes a harmless warning that it cannot be set as a system property. The enforcer check still passes.
The enforcer requires `file.encoding=UTF-8`:

```bash
# Build all modules
mvn install

# Build with UTF-8 enforced (required)
mvn install -Dfile.encoding=UTF-8

# Run tests for a single module
mvn test -pl wurbelizer

# Run a single test class
mvn test -pl wurbelizer -Dtest=ArgScannerTest

# Skip tests
mvn install -DskipTests

# Release build (adds sources + javadoc jars)
mvn install -Prelease

# Sign artifacts (requires GPG)
mvn verify -Psign

# Publish to Maven Central
mvn deploy -Ppublish
```

## Module Structure

```
wurbelizer-parent/          (root POM, version 25.0-SNAPSHOT)
├── wurbelizer/             core library (org.wurbelizer.*)
├── wurbelizer-maven-plugin/ Maven plugin (org.wurbelizer.maven.*)
├── ant-wurbelizer/         Ant integration (org.wurbelizer.ant.*)
└── console-wurbelizer/     CLI wrapper (illustrative, not recommended for production)
```

## Core Architecture

### Key Interfaces

| Interface      | Package                  | Role                                                                                                                                                                       |
|----------------|--------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `Wurbler`      | `org.wurbelizer.wurbel`  | Container that loads and runs a wurblet against a source file                                                                                                              |
| `Wurblet`      | `org.wurbelizer.wurblet` | A code generator; implements `run()`, `process()`, `cleanup()`; `isThreadSafe()` (default `false` in `AbstractWurblet`) — non-thread-safe wurblets are serialized JVM-wide |
| `Wurbiler`     | `org.wurbelizer.wurbile` | Compiler that translates `.wrbl` source to Java + `.ser`                                                                                                                   |
| `SourceRegion` | `org.wurbelizer.wurbel`  | Guard-type strategy; registered via `META-INF/services` SPI                                                                                                                |

### Key Classes

- **`SourceDocument`** — Holds a source file as a `StringBuilder` partitioned into typed `SourceElement`s: `CODE`, `SINGLE_COMMENT`, `BLOCK_COMMENT`, `GUARDED`. Parses on construction; exposes `insertGuarded`, `replaceGuarded`, `deleteGuarded`, `replaceInline`.
- **`AbstractWurbler`** — Base `Wurbler` implementation. Loads a wurblet class + its `.ser` fixed-source, runs it repeatedly until output stabilises.
- **`AbstractWurbiler`** — Base `Wurbiler` implementation. Translates `@[...]@` / `@(...)@` / `@{...}@` syntax from a `.wrbl` reader into a Java writer and a `List<String>` (fixed source).
- **`HeapFile`** / **`HeapFileFactory`** — In-memory files for here-documents. Files whose name starts with `.` stay on the heap; others are written to disk. Heap files persist for the whole wurbel lifecycle phase across all source files.
- **`AbstractWurblet`** / **`AbstractJavaWurblet`** — Base classes for wurblets; extend via `@{extends}@` directive in `.wrbl` source.
- **`WurbelContext`** — Per-run state (encoding charset, extra properties, heap files, source files, wurblet classloader), bound via `ScopedValue`. The static accessors in `Settings`, `WurbelHelper`, `HeapFileFactory` and `SourceFileFactory` delegate to `WurbelContext.current()`; unbound callers (Ant, console, tests) share a global context. The Maven mojos bind a fresh context per execution and are `threadSafe`.
- **`Settings`** — File encoding charset (default UTF-8), kept in the current `WurbelContext`.

### Guard Types (SPI)

Implemented via `SourceRegion`; registered in `META-INF/services/org.wurbelizer.wurbel.SourceRegion`:
- `netbeans` (default) — NetBeans-style; also recognized by IntelliJ
- `coffeebytes` — former Eclipse plugin style
- `visualstudio` — VS / VSCode style

Custom guard types implement `SourceRegion` and self-register.

### Maven Plugin Goals

| Goal          | Phase                  | Purpose                                         |
|---------------|------------------------|-------------------------------------------------|
| `wurbile`     | `generate-sources`     | Compile wurblet `.wrbl` sources → Java + `.ser` |
| `wurbel`      | `process-sources`      | Run wurbler on main sources                     |
| `test-wurbel` | `process-test-sources` | Run wurbler on test sources                     |

The plugin uses a custom `ClassRealmConfigurator` (`set-classrealm`) so that wurblet classes compiled in earlier phases are visible at wurbel time. `WurbelMojo` and `TestWurbelMojo` both extend `AbstractWurbelMojo`; `WurbileMojo` extends `AbstractWurbelizerMojo`.

## Wurblet Source Syntax (`.wrbl` files)

Files start at output level. Level-switching markers:

| Marker              | Meaning                                  |
|---------------------|------------------------------------------|
| `@[` … `]@`         | Switch to wurblet (Java) level           |
| `@(` … `)@`         | Embed wurblet-level expression in output |
| `@{directive ...}@` | Wurbiler directive (compile-time)        |

Escapes: `\@` → `@`, `\ ` → blank; other backslashes are copied verbatim (no continuation lines at wurblet level). Directives are only recognized outside `@[ ]@`.

Common directives: `@{package}@`, `@{import}@`, `@{extends}@`, `@{implements}@`, `@{include}@`, `@{args}@`, `@{to <stream>}@`, `@{phase N}@`, `@{indent N|auto}@`, `@{comment ...}@`, `@{code ...}@`, `@{config ...}@`.

## Source-Level Syntax (inside application source files)

| Construct      | Syntax                                                         | Purpose                                     |
|----------------|----------------------------------------------------------------|---------------------------------------------|
| Wurblet anchor | `@wurblet[(wurbler-directive)] <tag> <WurbletClass> [args...]` | Bind generated code to a wurblet            |
| Here-document  | `@> <filename>` … `@<`                                         | Inline data file; `.filename` stays in heap |
| Variable block | `@{` … `@}` inside a comment                                   | Declare `key = value` variables             |
| Variable ref   | `$var` or `${var}`                                             | Used in args and variable values            |

Anchors live in Java comments (single-line or block); generated code can contain new anchors, so the wurbler re-parses until stable. Args are whitespace-separated (quote to include whitespace) and may span lines via a trailing `\`. Variables resolve from inline `@{ @}` blocks, `.wurb` property files, Maven/Ant properties and environment variables (recursively).

Optional wurbler directives in the `(...)`:
- `test:$var==value` / `!=` — conditional anchor; when unmet, the wurblet is skipped and an existing guarded block is kept
- `indent=N` — extra indentation; negative means *smart indent* (align to the anchor comment), the Java default
- `fold=collapsed|expanded|none` — editor fold type for the guarded block (NetBeans only)
- `phase=N` — overrides the wurblet's `@{phase}@` for this anchor

Inline (unnamed) wurblets have a `<tag>` starting with `-`, `<`, `+` or `>` and inject code between two empty block comments `/**/` (or `/*@*/`) in adjacent code. `-`/`<` targets the preceding block, `+`/`>` the following block. Any other tag is a regular named anchor.

## Testing

Uses TestNG. Tests live in `src/test/java` alongside the module they test. Run the full suite with `mvn test` or a single class with `-Dtest=ClassName`.
