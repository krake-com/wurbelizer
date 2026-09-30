# Ant Integration

## Wurbile

The wurbiler is defined as an ant task:

```xml
<taskdef name="wurbile" classname="org.wurbelizer.ant.wurbile.AntWurbiler">
    <classpath>
        <pathelement location="${ext}/ant-wurbelizer.jar"/>
    </classpath>
    <classpath>
        <pathelement location="${ext}/wurbelizer.jar"/>
    </classpath>
</taskdef>
```

Where `${ext}` defines the directory containing wurbelizer jars.

The wurbile target then looks like this:

```xml
<target description="compile the wurblets" name="wurbile" depends="whatevertarget">
    <!-- wurbile the wurblets -->
    <mkdir dir="${wbuild}"/>
    <wurbile srcdir="${wrbl}" destdir="${wbuild}" includes="**/*.wrbl" />
    <!-- compile wurbiled wurblets -->
    <javac debug="on" source="11" deprecation="true"
        destdir="${wbuild}" includes="**" >
        <src path="${wsrc}"/>
        <src path="${wbuild}"/>
        <compilerarg value="-Xlint:unchecked"/>
        <classpath refid="wurb-cp"/>
    </javac>
</target>
```

Where:

- `${wrbl}`: directory holding the wurblet sources (filenames with the
extension `.wrbl`).
- `${wsrc}`: optional directory holding additional wurblet sources in Java
(for example parent classes, interfaces, and so on).
- `${wbuild}`: directory where the wurbiled sources and compiled classes
are placed.

The `wurb-cp` classpath defines all archives necessary to compile
the wurbiled wurblets. An optional step may create a jar archive of all
wurblets from `${wbuild}`.

Additional options for the wurbile task:
- `verbosity` sets the verbosity: `default`, `info` or `debug` (default `default`).
- `failonerror` determines whether to stop the ant build on error (bool,
default `true`).
- `proceed` is the inverse of `failonerror` (bool, default `false`).
- `indent` sets the initial indentation of the generated Java source
(default `-1`, i.e. the wurbiler's default).
- `parentclass` sets the default parent class for wurblets without an
[`@{extends}@`](wurblet-syntax.md#extends) directive (default `AbstractWurblet`).
- `packagename` sets the default package for wurblets without a
[`@{package}@`](wurblet-syntax.md#package) directive.

Of course, all options as defined by `...ant.taskdefs.MatchingTask` can
be applied as well.

## Wurbel

The wurbler is defined as an ant task as follows:

```xml
<taskdef name="wurbelize" classname="org.wurbelizer.ant.wurbel.AntWurbler">
    <classpath refid="wurb-cp"/>
</taskdef>
```

The classpath `wurb-cp` defines all archives necessary to run the wurblets. It
must at least contain the following setup:

```xml
<path id="wurb-cp">
    <!-- application specific wurblets -->
    <pathelement location="${wbuild}"/>
    <!-- the wurbelizer itself -->
    <pathelement location="${ext}/wurbelizer.jar"/>
</path>
```
Additional jar files can be added as required by the wurblets.
A very basic example of an ant target to run the wurbler looks like this:

```xml
<target name="wurbelize" depends="wurbile" description="wurbelize java sources">
    <wurbelize srcdir="${src}" includes="**/*.java"/>
</target>
```
which simply applies the wurbler to all java sources.
An advanced example of the wurbler ant target may look like this:

```xml
<target name="wurbelize" depends="apt" description="wurbelize sources">
    <wurbelize printstacktrace="true"
        srcdir="${src}" includes="**/*.wurb"
        wurbletpath="org.tentackle.wurblets:de.krake.myapp.wurblets"/>
    <wurbelize printstacktrace="true"
        srcdir="${src}/de/krake/myapp/dbms"
        analyzedir="${apt}" includes="*.java"
        wurbletpath="org.tentackle.wurblets:de.krake.myapp.wurblets"/>
    <wurbelize printstacktrace="true"
        srcdir="${src}/de/krake/myapp/dbms/trip" includes="*.java"
        wurbletpath="org.tentackle.wurblets:de.krake.myapp.wurblets"/>
</target>
```
The above example splits the wurbelization into 3 steps:

The first step is an alternative way of selecting the Java sources for wurbilation: the
wurbler locates files with the extension `.wurb`, which are property files that may
optionally define additional wurblet variables. For each `.wurb` file the wurbler
assumes a corresponding `.java` file. In a large project this is a convenient way to
speed up wurbilation, because the wurbler then does not need to scan every Java source,
most of which may not refer to any wurblet. An example of such a property file:
```
# this is MyClass.wurb
scriptpath=$scripts/myclass.scr
```
This declares the wurblet variable `$scriptpath` and sets its value to
`$scripts/myclass.scr`, where `$scripts` is itself an Ant property (or
environment variable).

Steps 2 and 3 reveal some other options:

- `wurbletpath` sets the package names the wurbler uses to load
wurblets that are referenced without an absolute class name. Package names are
separated by colons (semicolons and whitespace work as well). In the example above, the wurbler first tries to prepend
`org.tentackle.wurblets`, and if that fails, `de.krake.myapp.wurblets`.
- `printstacktrace` enables or disables a stacktrace for errors encountered
during the execution of wurblets (boolean, default is `false`). This is not to
be confused with exceptions thrown by the wurblets themselves!
- `analyzedir` defines a directory containing extra data for wurblet processing,
usually to exchange information between wurblets. For example,
a JDK "apt" (annotation processing) run may produce information that other
wurblets rely on (as is the case in Tentackle). Wurblets access files in this
directory via `Wurbler.getAnalyzeFile(name)`.
- `runonce` determines whether wurbelizing a file is allowed more than
once within an ant build (bool, default `true`).
- `dryrun` only parses the source files without running any wurblets
(bool, default `false`).
- `verbosity` sets the verbosity: `default`, `info` or `debug` (default `default`).
With `info`, the names of the modified files are logged; with `debug`, every
processed wurblet is logged together with its filename, which is useful for
verifying the order in which wurblets are invoked.
- `failonerror` determines whether to stop the ant build on error (bool,
default `true`).
- `proceed` is the inverse of `failonerror` (bool, default `false`).

Again, all options from org.apache.tools.ant.taskdefs.MatchingTask
apply as well.

All Ant properties are passed to the wurblets as extra properties (see
[Wurbler Properties](writing-wurblets.md#wurbler-properties)) and can be used as
`$`-variables in the sources. Unlike the Maven plugin, the Ant task starts
directly with phase 1: here-documents are written during the regular passes
(see [Processing Order and Phases](source-syntax.md#processing-order-and-phases)).

The guardtype-property determines the patterns used to define a guarded block.
The wurbelizer provides different guard types. The default is `netbeans`, which
is also supported by other IDEs such as IntelliJ. To change it, simply define
the following property:

```xml
<property name="guardtype" value="netbeans" />
```

See [Guard Types](guard-types.md) for the available guard types.

---

**Navigation:** [← Previous: Maven Integration](maven.md) | [Contents](toc.md) · [Index](index.md) | [Next: Console Integration →](console.md)
