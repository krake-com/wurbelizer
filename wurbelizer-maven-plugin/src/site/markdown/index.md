# Wurbelizer Maven Plugin

Maven integration for the Wurbelizer code generator. The plugin compiles wurblet
templates and runs the wurbler over your sources as part of a normal Maven build,
so generated code stays in sync with every `mvn` invocation. Classes live under
`org.wurbelizer.maven.*`.

## Goals

| Goal | Phase | Purpose |
|------|-------|---------|
| `wurbile` | `generate-sources` | Compile wurblet `.wrbl` sources → Java + `.ser`. |
| `wurbel` | `process-sources` | Run the wurbler on main sources. |
| `test-wurbel` | `process-test-sources` | Run the wurbler on test sources. |

The plugin uses a custom `ClassRealmConfigurator` (`set-classrealm`) so wurblet
classes compiled in earlier phases are visible at wurbel time. `WurbelMojo` and
`TestWurbelMojo` extend `AbstractWurbelMojo`; `WurbileMojo` extends
`AbstractWurbelizerMojo`.

## Requirements

- Java 25 or newer
- Maven 3.9.10 or newer, or Maven 4. On Maven 4 the plugin
  runs as a Maven 3 API plugin through the compatibility layer, with no POM changes
  needed. All goals are thread-safe on both versions.

## Usage

See the [Plugin Documentation](plugin-info.html) for the full goal reference,
parameters, and a sample configuration, and the
[Maven integration guide](https://wurbelizer.org) for a worked example.

## Documentation & reports

- Full user documentation: <https://wurbelizer.org>
- [Plugin goal reference](plugin-info.html)
- [API documentation (Javadoc)](apidocs/index.html)
- [Project reports](project-info.html) — dependencies, plugins, licenses, team.

This module is part of the [Wurbelizer Parent](../index.html) project.
