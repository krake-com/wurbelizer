# Guard Types

The Wurbelizer provides the following guard types:

- `netbeans`: Netbeans style guarded blocks. Also supported by other IDEs such as IntelliJ.
This is the default.
- `coffeebytes`: style of the former CoffeeBytes Eclipse plugin. Support by current Eclipse
versions is unknown.
- `visualstudio`: Microsoft Visual Studio style. Also supported by VSCode.

The guardtype can be changed with the wurblet property `guardtype`.
See [build tools integration](integration.md).
A single source file may use a different guard type by setting `guardtype` in its
[`.wurb` file](source-syntax.md#example-variables-in-a-wurb-file).

The wurbler recognizes the guarded blocks of all registered guard types, and the
blocks are re-created whenever their wurblet runs. Hence, changing the guard type
converts the existing blocks on the next run.

## Fold Types

The wurblet property `foldtype` sets the default [editor fold](source-syntax.md#editor-fold)
for all guarded blocks:

- `collapsed`: the block is collapsed in the editor
- `expanded`: the block is expanded in the editor
- `none`: the block gets no fold handle (default)

An individual anchor can override the default with the `fold` wurbler directive.
Currently, only the `netbeans` guard type supports fold types.

## Custom Guard Types

Custom guard types can be added as well.
A guard type must implement the interface `org.wurbelizer.wurbel.SourceRegion` and is
registered via `META-INF/services/org.wurbelizer.wurbel.SourceRegion`.
The interface requires the following methods:

- `type()`: the unique name of the guard type, as used in the `guardtype` property.
- `createBegin(foldType, guardName, wurbletElement)` and
  `createEnd(foldType, guardName, wurbletElement)`: create the comment lines that begin
  and end a guarded block. `foldType` is `null` if no fold is requested.
- `getBeginName(comment)` and `getEndName(comment)`: return the tag if the given
  comment begins or ends a guarded block of this type, otherwise `null`.
- `isEnd(comment, name)`: whether the comment ends the guarded block with the given tag.
- `isDefault()`: optional, returns `true` for the default guard type. Only one guard
  type should be the default.

`org.wurbelizer.wurbel.AbstractSourceRegion` provides a base implementation.
See `org.wurbelizer.wurbel.NetbeansRegion` for an example.

## Source Types

The kind of a source file determines how the wurbler treats it. Source types are
pluggable as well: a source type implements the interface
`org.wurbelizer.wurbel.SourceType` and is registered via
`META-INF/services/org.wurbelizer.wurbel.SourceType`. The type is selected by the
extension of the source file. The interface consists of:

- `getFileExtension()`: the file extension including the dot, e.g. `.java`.
- `extractProperties(SourceDocument document)`: extracts variables from the parsed
  source, which become available to the anchors and wurblets of that file.
- `isSmartIndent()`: optional, whether [smart indent](source-syntax.md#extra-indentation)
  is the default. The default is `false`.
- `isDefault()`: optional, returns `true` for the type used for all extensions without
  a dedicated source type.

The Wurbelizer provides two source types:

- `JavaSourceType` for `.java` files: smart indent is enabled, and the
  [built-in variables](source-syntax.md#built-in-variables) such as `classname` and
  `packagename` are extracted from the code.
- `NoneSourceType`: the default for all other files. It extracts no variables and uses
  no smart indent.

---

**Navigation:** [← Previous: Source Level Syntax](source-syntax.md) | [Contents](toc.md) · [Index](index.md) | [Next: Writing Wurblets →](writing-wurblets.md)
