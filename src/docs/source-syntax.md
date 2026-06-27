# Source Level Syntax

Java comments, whether single line or block comments, are parsed by the
wurbler for wurblet anchors, variables and here-documents. 
This applies to generated comments as well! Consequently, generated code may include new
anchors, variables or documents that in turn are evaluated by the wurbler
and so on. This is an important feature because it allows bootstrapping
from a rudimentary template, similar to a software developer starting with
a sketch (the finished result only makes full sense once you know how it
came to be).
The special significance of a character can be turned off by preceding it with
a backslash. A line terminated by a backslash is concatenated with the
following line (continuation line). The backslash itself is expressed by a
double backslash.

## Wurblet Anchor

All generated code, whether a single line or a large block, must be bound to a
wurblet and optional variables or resources such as model information. The
binding is done by a so-called wurblet anchor. There may be as many
anchors in a comment block as desired. The wurblets are applied by the
wurbler in the order of their anchors in the source file. The generated output
is inserted into the source file right after the comment. Each output block
is embraced by special comment lines that uniquely identify the block and
allow the wurbler to associate it to the anchor in subsequent wurbelization
passes. Once the code is generated, the anchor may be moved in the source
file anywhere without losing the binding. Thus, the source file may be edited
and anchors or the model information changed between wurbelization passes
without getting out of sync.

An anchor is of the form:
```
@wurblet[(wurbler-directive)] <tag> <wurblet> [<arg1> [... <argN>]]
```

where:

- `@wurblet`: starts an anchor
- `(wurbler-directive1 ... N)`: optional directives to the wurbler
- `<tag>`: is a tag uniquely identifying the anchor within the source file
- `<wurblet>`: the class name of the wurblet. This is either a fully
qualified class name or a basename to which the wurbler prepends
the package names provided by 
the [wurbletPaths](https://wurbelizer.org/static-content/sitedocs/wurbelizer/latest/wurbelizer-maven-plugin/wurbel-mojo.html#wurbletPaths)
configuration. 

- `<arg1>...`: optional wurblet arguments. 
Usually these arguments carry model information, or point to where such
information can be found.

Anchors that span more than one line must be concatenated by a trailing
backslash at the end of the lines to be continued. Arguments are separated
by whitespaces. If an argument contains whitespaces, it must be enclosed
in double quotes. Such arguments may span more than one line as well.
Arguments may contain $-signs denoting a variable. Variables may either
be declared in a variable section, can be an ant property, a property from
a wurb-file or an environment variable. The variable substitution process is
recursive, i.e. variables may contain strings referring to other variables and
so on.

Examples:
```java
/**
 * Principals are cached preloaded with a second index ’code’.
 *
 * @wurblet cache PdoCache --preload code
 */

// @wurblet updateByObjectTypeSerial DbUpdateBy --model=$mapfile \
//          processed:=:null objectClassId objectId modType serial:<= | processed

```

### Unnamed Anchor

If the `<tag>` in the wurblet anchor does not start with a letter, 
it is considered as an unnamed wurblet anchor. Unnamed anchors are useful for
so-called inline wurblets, which are commonly used to inject a small piece
of code into the code block directly preceding or following the anchor. The
code is injected between two consecutive empty block comments `/**/`. The
first character of the tag determines whether the preceding or following code
block is targeted by the wurblet.

- The characters `-` or `<` refer to the preceding block
- all others, including `+` or `>` refer to the following block

Example:
```
@TableName(/**/"modlog"/**/)      // @wurblet < Inject --string $tablename
```
The wurblet `Inject` (which is a 
[Tentackle wurblet](https://tentackle.org/static-content/sitedocs/tentackle/latest/tentackle-wurblets/apidocs/org/tentackle/wurblet/Inject.html), btw.)
inserts the string `"modlog"` into the `@TableName` annotation. The value is defined by
the variable `$tablename`.

### Conditional Anchor

Wurblets may be invoked conditionally. 
This is achieved by the `test`-directive.

Example:
```
// @wurblet(test:$planb==yes) runPlanB Didoedeldu
```

Will invoke the wurblet `Didoedeldu` if the variable `$planb` equals the string `yes`. 
Otherwise the comment `// condition not met` will be generated. 
Currently, only the operators `==` and `!=` are supported.

### Extra Indentation

The generated code may be indented by extra spaces with the indent wurbler
directive. 0 means no extra indent, a negative value is interpreted as *smart indent*,
which is the default for Java sources.
Smart indent aligns the generated code with the comment block containing the 
wurblet anchor.

Example:
```
 // @wurblet(indent=4) someMethod DoIt
```

### Editor Fold

An optional editor fold type for the generated code may be specified. 
Not all guard types support a fold type.
Currently, editor folds are only supported by the Netbeans-IDE.
By default, no editor fold will be generated. The following fold types are
supported:

- `collapsed`: the generated code will be collapsed
- `expanded`: the generated code will be expanded
- `none`: the generated code will get no fold-handle (default)

Example:
```
// @wurblet(fold=expanded) modelComment ModelComment
```

Notice that some IDEs automatically fold a guarded block 
(also known as custom code folding region) by default. IntelliJ does so, for example.

## Here Documents

Here-Documents are an elegant way to provide model information to the wurblets
or to create files in general. Whenever the wurbler detects a document
section within a comment block, it creates a file either on heap or in the
filesystem depending on the file’s name. Document sections start with `@>`
and terminate with `@<`.

Example (from the [Wurbelizer tutorial](https://github.com/harrharr/wurbelizer-tutorial/blob/master/sample/src/main/java/com/example/SampleDTO.java)):
```
/*
 * @> .$filename
 * String    name       the name
 * long      id         the unique ID
 * boolean   enabled    whether enabled or not
 * @<
 */
```

Notice that the filename may be a variable again. 
If the filename starts with a dot, it will not be saved to the filesystem
but instead kept in memory only. Furthermore, such heap files must be
unique within the wurbler instance. If the wurbler is invoked from within
maven (which usually is the case), the file is available during the 
whole wurbel lifecycle phase, to all wurblets in all files. This
feature allows running wurblets in distinct phases, each phase creating some
information for the next phase.

## Variables

Maven (or Ant) properties and environment variables are automatically declared 
as variables by the wurbler. Additional variables may be declared either in extra
property-files (extension `.wurb`) or directly at the source level within any
comment block. Such a declaration section starts with `@{` and ends with `@}`.

Example (from the [Tentackle tutorial](https://github.com/harrharr/tentackle-tutorial/blob/master/tracker-pdo/src/main/java/com/example/tracker/pdo/md/OrgUnit.java)):
```
/*
 * @{
 * tablename = md.orgunit
 * mapping   = $model/$tablename.map
 * @}
 */
```

Variables can be used in comments or other variable definitions in two ways:

1. `$<variable>`: e.g. `$tablename.map` where the dot automatically ends the variable name
2. `${<variable>}`: e.g. `${tablename}xx` to add the string `xx` to the tablename's value

## Further reading

- [Guard Types](guard-types.md) — the patterns that delimit the generated blocks
- [Writing Wurblets](writing-wurblets.md) — how a wurblet consumes anchors, here-documents and variables

---

**Navigation:** [← Previous: Wurblet Level Syntax](wurblet-syntax.md) | [Contents](toc.md) · [Index](index.md) | [Next: Guard Types →](guard-types.md)
