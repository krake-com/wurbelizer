# Wurblet Level Syntax

The Wurbelizer’s extensions to the Java syntax are all defined with the
help of the character `@`. There are two kinds of such extensions that apply
to the wurblet source code:

1. switching between wurblet- and output-level
2. wurbiler directives

Special significance of a character can be turned off by preceding it with
a backslash. A line terminated by a backslash is concatenated with the
following line (continuation line). The backslash itself is expressed by a
double backslash.

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

## Wurbiler Directives

The wurbiler accepts directives at compile time. Directives are enveloped
by a leading @{ and a closing }@ and are allowed on the wurblet and the
output level. They take the form:
```
@{<directive> <arg1> <arg2> ... <argN>}@
```
The following directives are supported:

### include

`@{include <filename>}@` includes the source file given by `<filename>`. 
Includes may be nested to any depth, and the wurbiler detects circular includes.
The filename may contain $-variables, which are replaced at compile time.

Example:

```
@{include $wurblets/wrbl/processargs.inc}@
```

### args

`@{args <arg1> ... <argN>}@` presets the wurblet arguments. 

The wurblet retrieves its arguments by `getContainer().getArgs()` at the wurblet
level. By default, the arguments are provided by the wurbler which usually
gets them from the wurblet-anchor within the source. `@{args}@` without
any arguments switches back to the default, i.e. wurbler args. 

This is especially useful after including wurblet sources to make sure that arguments
are processed.

### to

A wurblet may generate code into more than one stream. The default stream, named
`out`, is already provided by the container, so it does not need to be explicitly
opened or closed by the wurblet. Any other stream, however, must be managed by the
wurblet itself. `@{to <outputstream>}@` selects the stream that the generated code
is written to. `@{to out}@` switches back to the default stream, which is equivalent
to `@{to}@`.

Example:
```
@{to remoteOut}@
    public TrackedList<@(type)@> @(methodName)@(DomainContext context, ...) throws RemoteException;
@{to implOut}@
    public TrackedList<@(type)@> @(methodName)@(DomainContext context, ...) throws RemoteException {
        ...
    }
@{to}@
    TrackedList<@(type)@> list = on(@<type)@.class)@(methodName)@(...);
    ...
```

### package

`@{package <packagename>}@` sets the package name of the wurblet. 
By default wurblets are not created into a particular package. It is good practice,
however, to assign a package to wurblets.

Notice that the package name should be defined in the `<configuration>` of the
maven wurbelizer plugin to load the wurblet without the need to specify its FQCN
in the wurblet anchors.
See [wurbletPaths](https://wurbelizer.org/static-content/sitedocs/wurbelizer/latest/wurbelizer-maven-plugin/wurbel-mojo.html#wurbletPaths).

### import

`@{import <importpath>}@` adds import statements to the wurblet source.

Example:
```
@{import java.util.*}@
```

### extends

`@{extends <parentclass>}@` sets the parent class the wurblet extends.
Extending a parent class that in turn extends `org.wurbelizer.wurblet.AbstractWurblet` 
is a common design pattern for wurblets. 
The parent class usually implements all helper code, i.e. to access the model, while the
wurblet contains only wurblet code.

### implements

`@{implements <interface1> ... <interfaceN>}@` adds interfaces the
wurblet will implement. Multiple `@{implements}@` directives are
concatenated. 

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
code. However, the indentation can be changed manually to improve readability.

`@{indent <columns>}@` will set the indentation fixed to <columns>. 

To switch back to the automatic indentation (which is the default), use `@{indent auto}@`.

### phase

The sources are wurbeled in phases. In phase 0 the variables and here-documents
are parsed. In the next phase, the wurblets are executed in the order of the
anchors in the source file. The execution order can be changed by specifying
an explicit phase.

`@{phase N}@` sets the execution phase of the wurblet. Default is 1.

### comment

`@{comment ..... }@` adds the text to the wurblet javadoc comment.

### code

`@{code ...}@` adds the code to the class level code. This code is appended to the
 end of the wurblet's java code and can be used to override or implement wurblet methods.
 This is an alternative to subclassing.

## Further reading

- [Source Level Syntax](source-syntax.md) — the anchors and here-documents that drive wurblets
- [Writing Wurblets](writing-wurblets.md) — designing your own wurblets with the Wurbelizer API

---

**Navigation:** [← Previous: Console Integration](console.md) | [Contents](toc.md) · [Index](index.md) | [Next: Source Level Syntax →](source-syntax.md)
