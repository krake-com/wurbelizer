# Changelog

All notable changes to the Wurbelizer are documented in this file.
The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

## Versioning

Up to 2.2.0, releases used plain `major.minor.patch` versions.

Since 2019, versions follow the scheme `<java>.<major>.<minor>.<patch>`:

- the **first number** is the Java version the release is built for (`8`, `11`, `17`, `21`, `25`),
- the **remaining three numbers** form a semantic version of the feature set, shared across all Java lines.

For example, `8.2.2.0` and `11.2.3.0` were released on the same day and provide (almost) the same features
for Java 8 and Java 11. Maintenance releases for older Java lines usually backport fixes from the
current line. Such releases are listed briefly and refer to the release that introduced the change.

Issue numbers (`#n`) in releases prior to 2.1 refer to the former Bitbucket issue tracker.

## [25.7.0.0] - 2026-10-19

### Added

- `WurbelContext`: per-run state (encoding, extra properties, heap files, source files, wurblet classloader)
  bound via `ScopedValue`. The Maven mojos are now `threadSafe` and support parallel builds.
- Support for Maven 4 and reproducible builds (Maven 3.10.0 and 4).
- Automatic module names in the jar manifests.
- Enforcer rule for `file.encoding=UTF-8`.
- Unit tests.
- Full documentation set in `src/docs/` (migrated from the old wiki), built with MkDocs,
  including table of contents, index and a Maven 4 section.
- `CONTRIBUTING.md`, `ARCHITECTURE.md` and the LGPL 2.1 license file.

### Changed

- Requires Java 25 and Maven 3.9.10 or newer.
- Wurblet dependencies are resolved through the Maven Resolver API.
- Replaced Plexus `@Component` with JSR 330 annotations.
- Moved the project from Bitbucket to GitHub.
- Updated dependencies and plugins.

### Fixed

- The `\ ` escape (backslash blank) in wurblet sources.
- Site documentation no longer loads third-party resources (self-hosted ResizeObserver polyfill,
  disabled source repository widget for GDPR compliance).

## [25.6.5.0] - 2026-01-21

### Changed

- Built for Java 25.
- Added `deploymentName` to the Central publishing plugin configuration.
- Updated dependencies and plugins.

## [21.6.4.0] - 2025-06-16

### Changed

- Switched to the new Sonatype Central publishing service.
- Code analysis cleanups.

## [21.6.3.0] - 2025-02-12

### Added

- Wurbel mojo: optional parameters `firstPhase` and `lastPhase`.
- Inline comment tag `/*@*/` as an alternative to `/**/`. It avoids the warning
  "documentation comment is not attached to any declaration" since Java 23.

### Changed

- Updated dependencies (including ASM for Java 24). All versions are now defined as properties.
- Downgraded the plugin report plugin due to compatibility errors.

## [21.6.2.0] - 2024-04-24

### Added

- `AbstractWurbelizerMojo`: property `projectRoot`.
- `Wurbler.WURBLET_FILECONTENTS`.

### Changed

- Updated dependencies.

## [21.6.1.0] - 2023-10-11

### Added

- Maven plugin: skips the automatic configuration for `pom` artifacts.

## [21.6.0.0] - 2023-09-25

First release for Java 21. Includes all changes of 17.5.1.0.

### Changed

- Built for Java 21.
- Switched to the Maven 3.9.x core API.
- Updated dependencies and plugins.

## [17.5.1.0] - 2023-05-20

### Changed

- Requires Maven 3.9.1 or newer.
- Uses `Locale.ROOT` for upper/lowercase conversions.
- Updated dependencies, TestNG 7.8.0, maven-plugin-plugin 3.8.2.

### Removed

- Deprecated default parameter `${localRepository}` (deprecated since Maven 3.9.1).

## [11.3.1.2] - 2023-02-27

Backport of the check for unbalanced guarded sections (see [17.5.0.0]).
Updated dependencies.

## [17.5.0.0] - 2023-02-21

### Added

- Checks for unbalanced guarded sections: a missing begin or end of a guarded block is reported as an error.

### Changed

- TestNG 7.7.1, Ant 1.10.13.

## [8.2.2.5] - 2023-02-21

Backport of the check for unbalanced guarded sections (see [17.5.0.0]).

## [17.4.0.0] - 2022-12-12

### Changed

- Switched to the recent Maven API (3.8.5 or newer).
- TestNG 7.7.0.

## [17.3.2.2] - 2022-09-09

### Fixed

- Reverted the maven-deploy-plugin to 2.8.2 to fix deployment.

## [17.3.2.1] - 2021-11-10

### Fixed

- Fold type `none` for guarded blocks.

### Changed

- Java 17 migrations and updated dependencies.

## [11.3.1.1] - 2021-09-21

### Fixed

- Backslashes are replaced with forward slashes in stack trace comments (see [8.2.2.4]).
- Javadoc generation (the stylesheet had changed).

### Changed

- TestNG 7.3.0, updated dependencies.

### Added

- `ArgScannerTest`.

## [8.2.2.4] - 2021-09-16

### Fixed

- `SourceWurbler`: backslashes are replaced with forward slashes in stack trace comments
  (Windows paths broke the generated comments).

## [17.3.2.0] - 2021-09-15

First release for Java 17. Includes all changes of the 11.x line up to 11.3.1.0.

### Added

- Support for Java records.

### Changed

- Built for Java 17.

## [11.3.1.0] - 2020-07-27

### Added

- Smart indent: generated code aligns to the indentation of the anchor comment
  (`indent=N` with a negative value, the default for Java).

### Changed

- Improved checks for wurblet dependency loading errors.
- Updated dependencies.

## [8.2.2.3] - 2020-07-27

Backport of the remote loading of missing wurblet artifacts (see [11.3.0.2]).

## [11.3.0.2] - 2020-07-10

### Changed

- Missing wurblet artifacts are loaded from the remote repository.
- The wurbler terminates immediately if a wurblet cannot be found instead of looping.

### Added

- `README.md`.

## [11.3.0.1] - 2019-12-10

### Fixed

- `ClassRealmConfigurator` for the `test-wurbel` goal.

## [11.3.0.0] - 2019-10-24

### Added

- Wurblet filter (`WurbelFilter`) for the Maven plugin.

### Changed

- Streamlined the `Wurbler` API.

## [11.2.3.3] - 2019-09-03

### Added

- Check for duplicate names of guarded blocks.
- `.editorconfig`.

## [11.2.3.2] - 2019-07-23

### Changed

- Source types are registered via `META-INF/services`. Refactored source type detection.
- Phase 0 is not run again for files containing multiple here-documents (see [8.2.2.2]).

## [8.2.2.2] - 2019-07-21

### Changed

- Phase 0 is not run again for files containing multiple here-documents.

## [11.2.3.1] - 2019-05-20

Same fix as [8.2.2.1]. Updated dependencies.

## [8.2.2.1] - 2019-05-20

### Fixed

- Wurbelizing failed if the parent project was not available in the file system.

## [11.2.3.0] - 2019-03-30

Same features as [8.2.2.0], for Java 11.

## [8.2.2.0] - 2019-03-30

### Added

- Support for Java modifiers.

### Changed

- Extracted the Java-specific parts of `AbstractWurblet` into `AbstractJavaWurblet`.

## [11.2.2.1] - 2019-01-07

Same features as [8.2.1.2], for Java 11. Includes all changes of 2.2.0.

## [8.2.1.2] - 2019-01-05

First release with the new versioning scheme: the first number is the Java (LTS) version.

### Added

- Continuation lines for `@wurblet` anchors in single-line comments.

### Changed

- No error is reported when a here-document just moved within the same source file
  (for example, because the IDE added or removed imports).

## [2.2.0] - 2018-11-05

### Added

- `@{code ...}@` directive for top-level wurblet code.
- `SourceRegion` SPI for pluggable guard types.
- Check for the pathological case where the tag name equals the region token.

### Changed

- Requires Java 11 and Maven 3.6.0.
- ASM 7.0.
- Encapsulated the `Settings` properties.
- Site documentation with new logo, colors and links.
- Updated dependencies.

### Removed

- jgitflow plugin.

## [2.1.1] - 2018-01-16

### Added

- `WurbelDiscardException`.
- Documentation.

## [2.1] - 2017-11-29

### Added

- `@{config ...}@` directive for generic wurblet-specific configuration.
- `@{comment ...}@` directive.
- Wurblet execution phases.
- `test-wurbel` goal.
- `dryRun` switch.
- Configurable encoding.
- File and line number diagnostics if a wurblet fails.
- `WurbelException#isSourceInfoIncluded` to avoid misleading model loading errors.
- Support for the pathological case `...<sourcecode>)@(<wurblet-code>)@`.

### Changed

- Renamed `DefaultWurbiler` to `SourceWurbiler`. Generated code now contains a `wurbel` method.
- Migrated from the deprecated Maven 2 API to Maven 3.
- Updated dependencies.

### Fixed

- Quoting within `@{directive ...}@`.
- Encoding (UTF-8) and LF/CRLF handling on Windows.
- Eclipse lifecycle mapping.
- Defaults for the `test-wurbel` goal.

## [2.0.10] - 2016-05-03

### Fixed

- More Windows path issues (Ant, console, Maven plugin, wurbiler).

## [2.0.9] - 2016-05-03

### Fixed

- Windows path issue in the wurbel mojo.

## [2.0.8] - 2016-05-03

### Fixed

- Parent POM.

## [2.0.7] - 2016-05-03

### Changed

- Moved the Ant and console classes into the packages `org.wurbelizer.ant.*` and `org.wurbelizer.console.*`.
- Configured the Maven site plugin.

## [2.0.6] - 2016-02-26

### Fixed

- Eclipse m2e lifecycle mapping metadata (#4).

## [2.0.5] - 2016-02-17

### Fixed

- Various issues found by code analysis.

## [2.0.4] - 2016-02-10

### Fixed

- Source element parsing (#3).

## [2.0.3] - 2016-02-02

### Changed

- Parent POM updated for OSSRH (Maven Central).

## [2.0.2] - 2016-01-23

### Changed

- Switched to annotation-based Maven mojos (#2).
- Parent POM prepared for OSSRH, with options for source and javadoc jars.

### Fixed

- Javadocs (#1).

## [2.0.1] - 2016-01-19

### Changed

- Source and javadoc jars are built in profiles during the deploy phase.

### Fixed

- Windows compatibility.
- Javadocs and POMs.

## [2.0.0] - 2016-01-09

Initial release in this repository.

[Unreleased]: https://github.com/krake-com/wurbelizer/compare/25.6.5.0...develop
[25.6.5.0]: https://github.com/krake-com/wurbelizer/releases/tag/25.6.5.0
[21.6.4.0]: https://github.com/krake-com/wurbelizer/releases/tag/21.6.4.0
[21.6.3.0]: https://github.com/krake-com/wurbelizer/releases/tag/21.6.3.0
[21.6.2.0]: https://github.com/krake-com/wurbelizer/releases/tag/21.6.2.0
[21.6.1.0]: https://github.com/krake-com/wurbelizer/releases/tag/21.6.1.0
[21.6.0.0]: https://github.com/krake-com/wurbelizer/releases/tag/21.6.0.0
[17.5.1.0]: https://github.com/krake-com/wurbelizer/releases/tag/17.5.1.0
[11.3.1.2]: https://github.com/krake-com/wurbelizer/releases/tag/11.3.1.2
[17.5.0.0]: https://github.com/krake-com/wurbelizer/releases/tag/17.5.0.0
[8.2.2.5]: https://github.com/krake-com/wurbelizer/releases/tag/8.2.2.5
[17.4.0.0]: https://github.com/krake-com/wurbelizer/releases/tag/17.4.0.0
[17.3.2.2]: https://github.com/krake-com/wurbelizer/releases/tag/17.3.2.2
[17.3.2.1]: https://github.com/krake-com/wurbelizer/releases/tag/17.3.2.1
[11.3.1.1]: https://github.com/krake-com/wurbelizer/releases/tag/11.3.1.1
[8.2.2.4]: https://github.com/krake-com/wurbelizer/releases/tag/8.2.2.4
[17.3.2.0]: https://github.com/krake-com/wurbelizer/releases/tag/17.3.2.0
[11.3.1.0]: https://github.com/krake-com/wurbelizer/releases/tag/11.3.1.0
[8.2.2.3]: https://github.com/krake-com/wurbelizer/releases/tag/8.2.2.3
[11.3.0.2]: https://github.com/krake-com/wurbelizer/releases/tag/11.3.0.2
[11.3.0.1]: https://github.com/krake-com/wurbelizer/releases/tag/11.3.0.1
[11.3.0.0]: https://github.com/krake-com/wurbelizer/releases/tag/11.3.0.0
[11.2.3.3]: https://github.com/krake-com/wurbelizer/releases/tag/11.2.3.3
[11.2.3.2]: https://github.com/krake-com/wurbelizer/releases/tag/11.2.3.2
[8.2.2.2]: https://github.com/krake-com/wurbelizer/releases/tag/8.2.2.2
[11.2.3.1]: https://github.com/krake-com/wurbelizer/releases/tag/11.2.3.1
[8.2.2.1]: https://github.com/krake-com/wurbelizer/releases/tag/8.2.2.1
[11.2.3.0]: https://github.com/krake-com/wurbelizer/releases/tag/11.2.3.0
[8.2.2.0]: https://github.com/krake-com/wurbelizer/releases/tag/8.2.2.0
[11.2.2.1]: https://github.com/krake-com/wurbelizer/releases/tag/11.2.2.1
[8.2.1.2]: https://github.com/krake-com/wurbelizer/releases/tag/8.2.1.2
[2.2.0]: https://github.com/krake-com/wurbelizer/releases/tag/2.2.0
[2.1.1]: https://github.com/krake-com/wurbelizer/releases/tag/2.1.1
[2.1]: https://github.com/krake-com/wurbelizer/releases/tag/2.1
[2.0.10]: https://github.com/krake-com/wurbelizer/releases/tag/2.0.10
[2.0.9]: https://github.com/krake-com/wurbelizer/releases/tag/2.0.9
[2.0.8]: https://github.com/krake-com/wurbelizer/releases/tag/2.0.8
[2.0.7]: https://github.com/krake-com/wurbelizer/releases/tag/2.0.7
[2.0.6]: https://github.com/krake-com/wurbelizer/releases/tag/2.0.6
[2.0.5]: https://github.com/krake-com/wurbelizer/releases/tag/2.0.5
[2.0.4]: https://github.com/krake-com/wurbelizer/releases/tag/2.0.4
[2.0.3]: https://github.com/krake-com/wurbelizer/releases/tag/2.0.3
[2.0.2]: https://github.com/krake-com/wurbelizer/releases/tag/2.0.2
[2.0.1]: https://github.com/krake-com/wurbelizer/releases/tag/2.0.1
[2.0.0]: https://github.com/krake-com/wurbelizer/releases/tag/2.0.0
