# Writing Wurblets

As the wurblet level code is Java, you can use all of its features and eco system.
Especially an object oriented design helps a lot to keep the `.wrbl` files small 
and readable.

Remember that wurblets can generate any kind of code, not only Java code.

When designing your own wurblets, always keep in mind that wurblets should generate only
a certain _aspect_ or _code fragment_, for example a single method or a few methods that
semantically belong together. You can have as much wurblet anchors in your
source files as needed. For larger contexts it's better to use different wurblets
than a single one doing the whole job. Furthermore, it's perfectly possible to
combine the wurbelizer with traditional template engines such as [Velocity](https://velocity.apache.org/)
or [Freemarker](https://freemarker.apache.org/).
For example, the [Tentackle Wizard](https://tentackle.org/static-content/sitedocs/tentackle/latest/tentackle-wizard-maven-plugin/plugin-info.html) first generates the source files which contain the
wurblet anchors and the wurbelizer later generates the implementation according to the
model. See the [Tentackle tutorial](https://tentackle.org/tutorial/) for a sample project.

## Wurbelizer API

All wurblets must implement the interface `org.wurbelizer.wurblet.Wurblet`. 
The most important method for the wurblet to access its runtime environment is 
`getContainer()` which returns an instance of the interface `org.wurbelizer.wurbel.Wurbler`.

There are 2 abstract classes that already provide some basic implementations and
that can easily be extended via the `@{extends}@` directive.

1. `org.wurbelizer.wurblet.AbstractWurblet`: base class for all kinds of wurblets.
2. `org.wurbelizer.wurblet.AbstractJavaWurblet`: base class for all wurblets that
generate Java code.

### Wurbler Properties

The wurbler provides a set of properties for the wurblet. Properties are
categorized into namespaces as follows:

- `Wurbler.PROPSPACE_ENV` (`"env"`): namespace holding the environment variables
- `Wurbler.PROPSPACE_WURBLET` (`"wurblet"`): properties defined by the wurbler
- `Wurbler.PROPSPACE_EXTRA` (`"extra"`): extra namespace usually provided by the
build tool (maven, ant, ...). The `maven-wurbelizer-plugin` uses
the [wurbletProperties](https://wurbelizer.org/static-content/sitedocs/wurbelizer/latest/wurbelizer-maven-plugin/wurbel-mojo.html#wurbletProperties).
The `org.wurbelizer.ant.wurbel.AntWurbler` will store all ant-properties here.

#### Wurblet Namespace

The wurblet namespace applies to the current wurblet and defines
the following properties:

- `WURBPROP_FILENAME` (`"filename"`): the pathname of the source file.
- `WURBLET_DIRNAME` (`"dirname"`): the directory of the source file.
- `WURBLET_WURBNAME` (`"wurbname"`): the optional pathname of the `.wurb`-file 
(additional properties), null if none.
- `WURBLET_GUARDNAME` (`"guardname"`): the name of the guarded block.
- `WURBLET_WURBLETNAME` (`"wurbletname"`): the name of the wurblet.

## Skeleton extending AbstractJavaWurblet

The following code skeleton is a good starting point for writing your own wurblet.

```
@{package com.example.wurblets}@
@{import java.util.*}@
@{import java.io.*}@
@{import org.wurbelizer.wurbel.*}@
@{import org.wurbelizer.wurblet.*}@
@{import org.wurbelizer.misc.*}@
@{extends AbstractJavaWurblet}@
@{args}@
@{comment
Describes what's generated, the wurblet usage and access to the model.
This is javadoc!
}@
@[
  private void wurbel() throws WurbelException {

    ... template code goes here ...

  }
]@
```

## Errorhandling

There wurblet should throw one of the following exceptions if code generation failed:

- `org.wurbelizer.wurbel.WurbelException`: is counted as a build error and terminates the
build process at the end of the corresponding build phase. The generated output is replaced
by the stacktrace enclosed in a comment block.
- `org.wurbelizer.wurbel.WurbelDiscardException`: same as `WurbelException` but instructs the wurbler to discard the generated source.
The error is counted as a warning and is treated as if there was no wurblet at all.
Useful if a wurblet fails, but it's smarter to leave the existing code unchanged and continue
with the build process.
- `org.wurbelizer.wurbel.WurbelTerminationException`: terminates the build process immediately!
Wurblets should throw this exception if the error is severe enough that it doesn't
make sense to execute any further wurblets, because they would fail as well.

## Logging

The wurbler provides access to the logger of the build tool (Maven, Ant, etc...) via
`Wurbler.getLogger()`.

It provides the following logging levels:

- debug
- info
- warning
- error

Example:
```
    getContainer().getLogger().error("didoedeldu failed", ex);
```

## Examples

A lot of real-world examples can be found in the Tentackle framework:

[Tentackle wurblets](https://tentackle.org/static-content/sitedocs/tentackle/latest/tentackle-wurblets/apidocs/org/tentackle/wurblet/package-summary.html)

[Tentackle persistence wurblets](https://tentackle.org/static-content/sitedocs/tentackle/latest/tentackle-persistence-wurblets/apidocs/org/tentackle/persist/wurblet/package-summary.html)

---

**Navigation:** [← Previous: Guard Types](guard-types.md) | [Contents](toc.md) · [Index](index.md) | [README ↑](../../README.md)
