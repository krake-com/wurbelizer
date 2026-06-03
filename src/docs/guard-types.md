# Guard Types

The Wurbelizer provides the following guard types:

- `netbeans`: Netbeans style guarded blocks. Also supported by other IDEs such as IntelliJ.
This is the default.
- `coffeebytes`: style of the former CoffeeBytes Eclipse plugin. Support by current Eclipse
versions is unknown.
- `visualstudio`: Microsoft Visual Studio style. Also supported by VSCode.

The guardtype can be changed with the wurblet property `guardtype`.
See [build tools integration](integration.md).

Custom guard types can be added as well.
A guard type must implement the interface `org.wurbelizer.wurbel.SourceRegion` and is
registered via `META-INF/services`. 
See `org.wurbelizer.wurbel.NetbeansRegion` for an example.
