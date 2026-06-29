# Wurbelizer — Core Library

The core of the Wurbelizer: a lightweight Java code generator (LGPL 2.1). This
module contains the runtime that compiles wurblets, scans application sources for
`@wurblet` anchors, and weaves generated code into guarded blocks — repeating
until the output stabilizes. Packages live under `org.wurbelizer.*`.

## The pipeline

1. A **wurbiler** compiles `.wrbl` template files into a Java class plus a `.ser`
   file (the serialized fixed-source strings).
2. A **wurbler** scans application sources for `@wurblet` anchors, instantiates the
   compiled wurblets, and merges generated code into guarded blocks.
3. The wurbler repeats until no more changes occur — generated code may itself
   contain new anchors.

## Key types

| Type | Package | Role |
|------|---------|------|
| `Wurbler` | `org.wurbelizer.wurbel` | Loads and runs a wurblet against a source file. |
| `Wurblet` | `org.wurbelizer.wurblet` | A code generator (`run`/`process`/`cleanup`). |
| `Wurbiler` | `org.wurbelizer.wurbile` | Compiles `.wrbl` source to Java + `.ser`. |
| `SourceRegion` | `org.wurbelizer.wurbel` | Guard-type strategy, registered via the `META-INF/services` SPI. |
| `SourceDocument` | `org.wurbelizer.wurbel` | A source file partitioned into typed regions; inserts/replaces guarded blocks. |

## Documentation & reports

- Full user documentation: <https://wurbelizer.org>
- [API documentation (Javadoc)](apidocs/index.html)
- [Cross-referenced sources](xref/index.html)
- [Project reports](project-info.html) — dependencies, plugins, licenses, team.

This module is part of the [Wurbelizer Parent](../index.html) project.
