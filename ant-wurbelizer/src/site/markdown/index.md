# Wurbelizer Ant Wrapper

Apache Ant integration for the Wurbelizer code generator. This module provides Ant
tasks that drive the wurbiler and wurbler from an Ant build, for projects that use
Ant instead of Maven. Classes live under `org.wurbelizer.ant.*`.

## What it does

The wrapper exposes the Wurbelizer pipeline as Ant tasks: compile `.wrbl` templates
into wurblet classes, then run the wurbler to weave generated code into the guarded
blocks of your sources — the same two-step flow the Maven plugin performs, driven
from `build.xml`.

## Documentation & reports

- Full user documentation: <https://wurbelizer.org>
- [Ant integration guide](https://wurbelizer.org)
- [API documentation (Javadoc)](apidocs/index.html)
- [Cross-referenced sources](xref/index.html)
- [Project reports](project-info.html) — dependencies, plugins, licenses, team.

This module is part of the [Wurbelizer Parent](../index.html) project.
