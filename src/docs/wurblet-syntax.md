# Wurblet Level Syntax

The Wurbelizer’s extensions to the Java syntax are all defined with the
help of the character `@`. There are two kinds of such extensions that apply
to the wurblet source code:

1. switching between wurblet- and output-level
2. wurbiler directives

The special significance of `@` can be turned off by preceding it with a backslash,
see [Escaping](#escaping).

> **About the examples**
>
> Besides the small illustrative snippets, this document uses real wurblets from the
> [Tentackle](https://tentackle.org) framework, which drives its whole persistence layer
> with the Wurbelizer. The sources live in the modules
> [`tentackle-wurblets`](https://tentackle.org/tentackle/tentackle-wurblets/apidocs/org/tentackle/wurblet/package-summary.html)
> (model-level, storage-independent code) and
> [`tentackle-persistence-wurblets`](https://tentackle.org/tentackle/tentackle-persistence-wurblets/apidocs/org/tentackle/persist/wurblet/package-summary.html)
> (SQL and remote-delegate code).

## Level Switching

The wurblet source starts at the output level. Thus, if no level switching
occurs, the wurblet’s source will simply be copied to the generated output.
Changing the level can be achieved in two ways:

1. code switching
2. value extraction

Code switching toggles between emitting text at the output level and running the
wurblet’s own control flow at the wurblet level (and back again).
Value extraction lets you insert a value computed at the wurblet level directly
into the generated output stream.

### Code Switching

Code switching is achieved by an @-sign and a square bracket.

- `@[` switches to wurblet level
- `]@` switches to output level

Example:
```
@[
    // generate debug message if debug == true
    if (debug) {
]@
    System.out.println("debug message ...");
@[
    }
]@
```

### Value Extraction

To copy values from the wurblet level to the generated output the @-sign is
used in conjunction with round braces.

- `@(` enters the wurblet level
- `)@` leaves the wurblet level

For example, the wurblet source:
```
@[
    for (int i=0; i < 3; i++) {
]@
        System.out.println("This is line @(i+1)@");
@[
    }
]@
```
Will generate the following code:
```java
System.out.println("This is line 1");
System.out.println("This is line 2");
System.out.println("This is line 3");
```
Any Java expression can be inserted into the generated output this way. 

Within the wurblet level, the markers `@[`, `@(` and `@{` are not interpreted, but copied
together with their closing counterparts. This allows nesting, i.e. a wurblet may generate
the source of another wurblet.

### Escaping

The backslash is the escape character of the wurbiler, on all levels:

- `\@` produces a plain `@`, which is neither a level switch nor a directive.
- `\ ` (backslash blank) produces a plain blank.
- Any other backslash is copied unchanged, together with the character following it. So
  `\n` in a Java string literal keeps working, and `\\` stays a double backslash.
  Unlike the [source level](source-syntax.md), there are no continuation lines: a trailing
  backslash is copied as well.

A closing marker can be escaped by escaping its `@`, e.g. `]\@` produces a literal `]@`.

Escaping matters most when the *generated* code contains annotations, because
`@` is significant at the wurblet level as well. A wurblet that generates an
`@Override` should therefore write `\@Override`, as in this fragment of the
Tentackle `MethodCache` wurblet:
```
  \@Override
  public PdoMethodCache<@(pdoName)@> getPdoMethodCache() {
    return METHOD_CACHE;
  }
```
An unescaped `@` that is not followed by `[`, `(` or `{` is copied as well, but the wurbiler
logs a warning. This also applies to annotations at the wurblet level: write `\@Override`
for a method declared within `@[ ... ]@` to avoid the warning.

### A complete example

Tentackle's `ColumnLengths` wurblet is small enough to show in full and uses
nothing but the two level switches. It walks the attributes of an entity and emits a
constant for every `String` column that has a maximum size:

```
@{include $currentDir/header.incl}@
@{comment
<strong>({\@code \@wurblet})</strong> Generate code to define the column lengths.
...
}@
@[
  private void wurbel() throws ModelException {
    String lead = getOption("noif") == null ? "" : "public static final ";
    for (Attribute attr: getEntity().getAttributes())  {
      if ("String".equals(attr.getEffectiveDataType().getJavaType()) &&
          attr.getSize() != null && attr.getSize() != 0 && !attr.getOptions().isNoConstant() && !attr.getOptions().isFromSuper())  {
]@

  /** maximum number of characters for '@(attr)@'. */
  @(lead)@int CL_@(attr.getName().toUpperCase(Locale.ROOT))@ = @(attr.getSize())@;
@[
      }
    }
  }
]@
```

Note how the whole method body lives at the wurblet level, while the three lines in the
middle are output level: a fixed text with three extracted values, `@(attr)@`,
`@(lead)@` and `@(attr.getSize())@`.

Given a model that declares

```
 * String(30)     name            name              the number pool name [key]
 * String(80)     realm           realm             pool realm, optional [MAPNULL]
```

the wurblet produces:

```java
  /** maximum number of characters for 'name'. */
  int CL_NAME = 30;

  /** maximum number of characters for 'realm'. */
  int CL_REALM = 80;
```

## Wurbiler Directives

The wurbiler accepts directives at compile time. Directives are enveloped
by a leading `@{` and a closing `}@`. They are recognized at the output level, i.e. outside of
`@[ ... ]@` blocks (inside, they are copied, see [Level Switching](#level-switching)).
They take the form:
```
@{<directive> <arg1> <arg2> ... <argN>}@
```
Directive names are case-insensitive. An unknown directive is an error.
The following directives are supported:

### include

`@{include <filename>}@` includes the source file given by `<filename>`. 
Includes may be nested to any depth, and the wurbiler detects circular includes.
The filename may contain $-variables, which are replaced at compile time.
A relative filename is resolved against the working directory of the build tool, not
against the including file.

Example:

```
@{include $wurblets/wrbl/processargs.inc}@
```

When the wurbiler is run from Maven, the variable `$currentDir` holds the directory of
the `.wrbl` file being compiled, which makes includes relative to the wurblet itself.
Tentackle uses this for two distinct purposes.

**A common header.** Every wurblet in `tentackle-persistence-wurblets` starts with the
same line:
```
@{include $currentDir/header.incl}@
```
and `header.incl` holds nothing but the directives shared by all of them:
```
@{package org.tentackle.persist.wurblet}@
@{import java.io.*}@
@{import java.util.*}@
@{import org.tentackle.buildsupport.*}@
@{import org.tentackle.common.*}@
@{import org.tentackle.sql.*}@
@{import org.tentackle.model.*}@
@{import org.tentackle.wurblet.*}@
@{import org.wurbelizer.wurbel.*}@
@{import org.wurbelizer.wurblet.*}@
@{extends DbModelWurblet}@
@{args}@
```
The sibling module `tentackle-wurblets` has its own `header.incl` that extends
`ModelWurblet` instead — the same idiom, a different base class.

**Shared template fragments.** Includes are not limited to directives; they may contain
wurblet *and* output level code. The wurblets that generate SQL statements
(`DbSelectList`, `DbUpdateBy`, `DbDeleteBy`, ...) all need the same `WHERE`-clause
logic, so it is factored out into `genwhere.incl` and pulled in where the clause belongs
in the generated method:
```
    PreparedStatementWrapper st = getPreparedStatement(@(classVar)@@(statementId)@,
      b -> {
        StringBuilder sql = createSqlUpdate();
@{include $currentDir/genupdate.incl}@
        sql.append(Backend.SQL_WHEREALL);
@{include $currentDir/genwhere.incl}@
```
Because inclusion happens at compile time, this is a pure text substitution: the
included fragment becomes part of the wurblet's `wurbel()` method.

### args

`@{args <arg1> ... <argN>}@` presets the wurblet arguments. The arguments are separated by
whitespace and are not quoted or variable-substituted.

The wurblet retrieves its arguments by `getContainer().getArgs()` at the wurblet
level. By default, the arguments are provided by the wurbler which usually
gets them from the wurblet-anchor within the source. `@{args}@` without
any arguments switches back to the default, i.e. wurbler args. 

This is especially useful after including wurblet sources to make sure that arguments
are processed.

This is exactly why the Tentackle `header.incl` shown above ends with `@{args}@`: the
last directive of the shared header resets the argument handling to the wurbler's
arguments, so that every wurblet including it receives the arguments written into its
`@wurblet` anchor, no matter what the included file did before.

### to

A wurblet may generate code into more than one stream. The default stream, named
`out`, is already provided by the container, so it does not need to be explicitly
opened or closed by the wurblet. Any other stream, however, must be managed by the
wurblet itself. `@{to <outputstream>}@` selects the stream that the generated code
is written to. `@{to out}@` switches back to the default stream, which is equivalent
to `@{to}@`.

The stream is an ordinary Java variable at the wurblet level — usually a `PrintStream`
declared right before it is used — so opening and closing it is plain Java.

Tentackle relies on this to generate three artifacts from a single anchor. When an entity
is accessible remotely, a method must exist in three places: in the local implementation,
as a signature in the TRIP delegate interface, and as a forwarding method in the delegate
implementation. The `DbUpdateBy` wurblet writes all three at once:

```
  @(scope)@ int @(methodName)@(@(params)@) {
@[
    if (isRemote()) {
      // create includes
      RemoteIncludes genInc = new RemoteIncludes(this);
      PrintStream implOut   = genInc.getImplementationStream();
      PrintStream remoteOut = genInc.getInterfaceStream();
]@
    if (getSession().isRemote())  {
@{to remoteOut}@
  int @(methodName)@(@(params)@);
@{to implOut}@

  \@Override
  public int @(methodName)@(@(params)@) {
@{to implOut}@
    return dbObject.@(methodName)@(@(iparms)@);
  }
@{to}@
      return getRemoteDelegate().@(methodName)@(@(iparms)@);
    }
    // else: local mode
@[
    } // end if remote
]@
```

Reading the streams in order:

- the default stream (`out`) receives the method being generated into the current source
  file, including the `if (getSession().isRemote())` branch that delegates the call
- `remoteOut` receives the bare signature for the remote interface
- `implOut` receives the forwarding implementation
- `@{to}@` switches back to the default stream so the local branch continues

The two extra streams are *not* files on disk. `RemoteIncludes` creates them as
`HeapStream`s named after the delegate classes (see
[Here Documents](source-syntax.md#here-documents)), and a later wurbel pass over the
delegate sources pulls them in with an `Include` wurblet. The whole round trip is
described in
[Cross-file generation with heap files](source-syntax.md#cross-file-generation-with-heap-files).

### package

`@{package <packagename>}@` sets the package name of the wurblet. 
By default wurblets are not created into a particular package. It is good practice,
however, to assign a package to wurblets. A wurblet can have only one package: a second
`@{package}@` directive with a different name is an error.
The generated Java source and `.ser` file are placed in the corresponding package directory.

Notice that the package name should be defined in the `<configuration>` of the
maven wurbelizer plugin to load the wurblet without the need to specify its FQCN
in the wurblet anchors.
See [wurbletPaths](https://wurbelizer.org/wurbelizer/wurbelizer-maven-plugin/wurbel-mojo.html#wurbletPaths).

Tentackle declares two wurblet packages,
```
@{package org.tentackle.wurblet}@
```
and
```
@{package org.tentackle.persist.wurblet}@
```
and lists both in the plugin configuration of the modules being wurbeled, so that
anchors can simply say `Methods` or `DbSelectList` instead of the fully qualified name.
See [Maven Integration](maven.md#a-real-world-configuration) for the complete
configuration.

### import

`@{import <importpath>}@` adds import statements to the wurblet source.

Example:
```
@{import java.util.*}@
```

### extends

`@{extends <parentclass>}@` sets the parent class the wurblet extends.
Like `@{package}@`, it may be given only once (repeating the same class is allowed).
Extending a parent class that in turn extends `org.wurbelizer.wurblet.AbstractWurblet` 
is a common design pattern for wurblets. 
The parent class usually implements all helper code, i.e. to access the model, while the
wurblet contains only wurblet code.

Tentackle is a good illustration of how far this pattern carries. It maintains a small
hierarchy of base classes written as ordinary Java, not as wurblets:

| Base class | Set by | Adds |
|---|---|---|
| `AbstractJavaWurblet` | the Wurbelizer | class name, super class name of the wurbeled source |
| `ModelWurblet` | `@{extends ModelWurblet}@` | the parsed entity model: `getEntity()`, attributes, relations |
| `DbModelWurblet` | `@{extends DbModelWurblet}@` | SQL specifics: statement IDs, backends, remote handling |

A wurblet such as `AttributeNames` then consists of almost nothing but template code,
because everything it needs is a method on its parent:

```
@{include $currentDir/header.incl}@
@[
  private void wurbel() {
    String lead = getOption("noif") == null ? "" : "public static final ";
    for (Relation rel: getEntity().getRelations()) {
      String var = rel.getVariableName();
]@

  /** relation name for '@(rel)@'. */
  @(lead)@String RN_@(var.toUpperCase(Locale.ROOT))@ = "@(var)@";
@[
    }
    ...
```

Keeping the model access in Java and the templates in `.wrbl` files also means the
helper code can be unit-tested and debugged with the ordinary tool chain.

### implements

`@{implements <interface1> ... <interfaceN>}@` adds interfaces the
wurblet will implement. Multiple `@{implements}@` directives are
concatenated. The interfaces are listed in alphabetical order, and implementing the same
interface twice is an error.

Example:
```
@{implements MyInterface1 MyInterface2}@
@{implements MyInterface3}@
```
will result in
```
... implements MyInterface1, MyInterface2, MyInterface3 {
```

### indent

By default, the wurbiler does its best to produce readable and debuggable
Java code for the wurblet. However, the indentation can be changed manually to improve readability.
This applies to the generated Java source of the wurblet, not to the code the wurblet generates.

`@{indent <columns>}@` will set the indentation fixed to `<columns>`. 

To switch back to the automatic indentation (which is the default), use `@{indent auto}@`.

### phase

The sources are wurbeled in phases. In phase 0 the variables and here-documents
are parsed. In the next phase, the wurblets are executed in the order of the
anchors in the source file. The execution order can be changed by specifying
an explicit phase.

`@{phase N}@` sets the execution phase of the wurblet. Default is 1.

Phases span all source files of a build-tool run: all wurblets of phase 1 in all
files are executed before any wurblet of phase 2. See
[Processing Order and Phases](source-syntax.md#processing-order-and-phases).
The phase can also be overridden for a single anchor with the
[`phase` wurbler directive](source-syntax.md#execution-phase).

Tentackle uses a later phase for wurblets that must observe what the other wurblets in
the same file have done. `ModelComment` and `UniqueDomainKey` both start with
```
@{phase 2}@
```
`ModelComment` documents the relations of an entity as a comment block, so it has to run
after the relations have been resolved, regardless of where its anchor happens to sit in
the source file.

### comment

`@{comment ..... }@` adds the text to the wurblet javadoc comment.

Since the text becomes javadoc of the generated wurblet class, it is written in HTML and
`@` must be escaped as `\@`. Tentackle documents every wurblet this way, which is what
its published
[wurblet API documentation](https://tentackle.org/tentackle/tentackle-wurblets/apidocs/org/tentackle/wurblet/package-summary.html)
is generated from. The `Inject` wurblet, for example:

```
@{comment
<strong>({\@code \@wurblet})</strong> Injects code from the argument list.
<p>
usage:<br>
&#064;wurblet &lt;|&gt; Inject [--string] &lt;text&gt;
<p>
arguments:
<ul>
<li><em>--string:</em> enclose in double quotes.</li>
<li><em>text:</em> the text to inject.</li>
</ul>
...
}@
```

Documenting the accepted arguments right in the wurblet source is a habit worth copying:
the anchor in the application source is the only place a user of the wurblet sees, and
the javadoc is what tells them which options exist.

### code

`@{code ...}@` adds the code to the class level code. This code is appended to the
end of the wurblet's java code and can be used to override or implement wurblet methods.
This is an alternative to subclassing.

Example:
```
@{code
  \@Override
  public String process(String generatedCode) {
    return generatedCode.strip() + "\n";
  }
}@
```

### config

`@{config ...}@` sets the wurblet specific configuration. The text is stored in the
compiled wurblet and is available at runtime via `getConfiguration()`, which returns
null if the wurblet has no configuration.

Unlike the other directives, the Wurbelizer does not interpret the configuration text
at all. Its format and its meaning are entirely up to the wurblet, or more commonly to
the parent class set by `@{extends}@`, which reads the configuration and configures the
generation accordingly. This is the recommended way to parameterize a wurblet with
settings that belong to the wurblet itself rather than to the source file it is
applied to (the latter is what wurblet arguments and source-level variables are for).

Multiple `@{config}@` directives are concatenated, separated by newlines, so a
multiline configuration can be written as:
```
@{config model = org.example.model.Customer}@
@{config nullable = false}@
```
and is retrieved by the wurblet as:
```java
String config = getConfiguration();    // "model = org.example.model.Customer\nnullable = false"
```

#### Example: switching the argument parser

Tentackle's SQL wurblets share one base class, `DbModelWurblet`, but they need its
argument parser to behave differently. `DbUpdateBy` and `PdoUpdateBy` take an update
expression *and* a list of attributes to be updated, separated by a `|`, so for them the
separator is meaningful:
```
@{include $currentDir/header.incl}@
@{config groupArgs}@
```
The select wurblets, on the other hand, accept paths across entity boundaries in their
expressions:
```
@{include $currentDir/header.incl}@
@{config pathAllowed}@
```
and `RemoteMethod` may be applied to sources that have no entity in the model at all:
```
@{include $currentDir/header.incl}@
@{config missingModelOk}@
```

The shared base class reads the configuration once, in `run()`, before any generation
happens:

```java
  @Override
  public void run() throws WurbelException {

    String wurbletOptions = getConfiguration();
    if (wurbletOptions != null) {
      if (wurbletOptions.contains("groupArgs")) {
        argumentGroupingEnabled = true;
      }
      else if (wurbletOptions.contains("pathAllowed")) {
        pathAllowed = true;
      }
    }

    super.run();
    ...
```

This is the distinction the configuration is for: whether `|` groups arguments is a
property of the *wurblet* — it is the same for every anchor that ever invokes
`DbUpdateBy` — whereas which columns to update is a property of the *anchor* and is
therefore passed as a wurblet argument. Note also that the option is written once in the
`.wrbl` file rather than repeated in every anchor, and that it cannot be got wrong by
the user of the wurblet.

## Further reading

- [Source Level Syntax](source-syntax.md) — the anchors and here-documents that drive wurblets
- [Writing Wurblets](writing-wurblets.md) — designing your own wurblets with the Wurbelizer API

---

**Navigation:** [← Previous: Console Integration](console.md) | [Contents](toc.md) · [Index](index.md) | [Next: Source Level Syntax →](source-syntax.md)
