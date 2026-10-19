# Console Integration

The wurbiler and wurbler can be invoked from the commandline as well. 
However, this is not recommended. The module `console-wurbelizer` is mainly provided
to illustrate the usage via the wurbelizer's API.

## Wurbile

```
java org.wurbelizer.console.wurbile.ConsoleWurbiler [--verbosity=default|info|debug]
     [--indent=<n>] [--extends=<parentclass>] [--package=<package>] <wurblet>.wrbl
```

Compiles a single `.wrbl` file. The generated Java source and the `.ser` file are
written below the directory of the `.wrbl` file, in the directory of the wurblet's package,
and must be compiled separately.
`--extends` and `--package` set the defaults for wurblets without an
[`@{extends}@`](wurblet-syntax.md#extends) or [`@{package}@`](wurblet-syntax.md#package)
directive. The exit code is 2 if the wurblet contains errors and 1 on other failures.

## Wurbel

```
java org.wurbelizer.console.wurbel.ConsoleWurbler [--out=<file>|++out=<file>]
     [--analyzedir=<dir>] [--verbosity=default|info|debug] [--glob=<regex>]
     <wurblet> [<wurblet args>...]
```

Unlike the Ant and Maven integrations, the console wurbler does not process source
files. It runs a single wurblet, given by its fully qualified class name, with the
arguments from the command line and prints the generated code to standard output, or to
the file given by `--out` (`++out` appends to the file). `--analyzedir` sets the
directory for [`getAnalyzeFile()`](writing-wurblets.md#the-container). The wurblet runs once, in its
[phase](wurblet-syntax.md#phase); errors are printed as a comment to standard error.

With `--glob`, the wurblet is invoked once for each file in a directory whose name
matches the regular expression, for example `--glob="model/.*\.map$"`. An argument
consisting of a single `@` is replaced by the pathname of the file.

---

**Navigation:** [← Previous: Ant Integration](ant.md) | [Contents](toc.md) · [Index](index.md) | [Next: Wurblet Level Syntax →](wurblet-syntax.md)
