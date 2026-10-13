# Contributing to the Wurbelizer

Thanks for your interest in improving the Wurbelizer! Bug reports, documentation
fixes and code contributions are all welcome.

## Reporting Bugs and Requesting Features

Please use the [GitHub issue tracker](https://github.com/krake-com/wurbelizer/issues).

A good bug report contains:

- the Wurbelizer version, the Java version and the build tool (Maven, Ant or console)
  including its version,
- the smallest wurblet (`.wrbl`) and source file that reproduce the problem,
- what you expected and what happened instead, including the build output or stack trace.

For larger changes, please open an issue first to discuss the idea before investing
time in an implementation.

## Building

You need Java 25+ and Maven 3.9.10+ (Maven 4 works as well). The build enforces
`file.encoding=UTF-8`:

```bash
# build all modules and run the tests
mvn install -Dfile.encoding=UTF-8

# run the tests of a single module
mvn test -pl wurbelizer -Dfile.encoding=UTF-8

# run a single test class
mvn test -pl wurbelizer -Dfile.encoding=UTF-8 -Dtest=ArgScannerTest
```

On Maven 4, `-Dfile.encoding=UTF-8` causes a harmless warning that it cannot be set as
a system property. The enforcer check still passes.

## Project Layout

| Path                      | Contents                                                         |
|---------------------------|------------------------------------------------------------------|
| `wurbelizer`              | core library (`org.wurbelizer.*`)                                |
| `wurbelizer-maven-plugin` | Maven plugin (`org.wurbelizer.maven.*`)                          |
| `ant-wurbelizer`          | Ant integration (`org.wurbelizer.ant.*`)                         |
| `console-wurbelizer`      | command-line wrapper (illustrative)                              |
| `src/docs`                | the documentation, see the [Table of Contents](src/docs/toc.md)  |
| `src/mkdocs`              | builds the documentation website from `README.md` and `src/docs` |

If you are new to the code base, read [Code Levels](src/docs/code-levels.md) and
[Writing Wurblets](src/docs/writing-wurblets.md) first. The design of the code itself
is described in [ARCHITECTURE.md](ARCHITECTURE.md).

## Submitting Changes

1. Fork the repository and create a topic branch from `develop`.
2. Make your change. Keep each pull request focused on a single topic.
3. Add or update tests and documentation (see below).
4. Make sure `mvn install -Dfile.encoding=UTF-8` succeeds.
5. Open a pull request against `develop` and describe what the change does and why.
   Reference the related issue, if there is one.

### Code Style

- Match the style of the surrounding code: 2 spaces for indentation, no tabs, opening
  braces at the end of the line, `else`/`catch`/`finally` on a new line.
- Use single-class imports, no wildcards.
- Public and protected API needs Javadoc.
- New source files start with the LGPL license header. Copy it from an existing file.
- Use American English spelling in code comments and documentation
  (e.g. "behavior", "serialize", "color").

### Tests

Tests are written with [TestNG](https://testng.org) and live in `src/test/java` of the
module they test. Bug fixes should come with a test that fails without the fix. New
features need tests covering the new behavior.

### Documentation

User-visible changes need a matching update of the documentation in `src/docs`. When
adding or changing a document, keep the [Table of Contents](src/docs/toc.md) and the
[Index](src/docs/index.md) in sync.

The website is generated from `README.md` and `src/docs`, see
[`src/mkdocs/README.md`](src/mkdocs/README.md). Do not edit the generated pages.

## License

The Wurbelizer is distributed under the
[GNU Lesser General Public License v2.1](LICENSE). By submitting a contribution, you
agree that it is licensed under the same terms.
