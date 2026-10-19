# Wurbelizer — Project Site

This is the Maven-generated **project site** for the Wurbelizer: API documentation
(Javadoc), cross-referenced sources, plugin documentation and project reports for
every module.

Looking for the **user documentation** — quickstart, syntax reference and guides?
That lives at **[wurbelizer.org](https://wurbelizer.org)**.

## What is the Wurbelizer?

The Wurbelizer is a lightweight Java code generator distributed under LGPL 2.1.
It uses Java itself as its template language and weaves generated code directly
into guarded blocks of your source files, regenerating until the code stabilizes.

## Modules

| Module                                              | Description                                                                |
|-----------------------------------------------------|----------------------------------------------------------------------------|
| [wurbelizer](wurbelizer/)                           | Core library (`org.wurbelizer.*`) — wurbiler, wurbler and wurblet runtime. |
| [wurbelizer-maven-plugin](wurbelizer-maven-plugin/) | Maven integration: `wurbile`, `wurbel` and `test-wurbel` goals.            |
| [ant-wurbelizer](ant-wurbelizer/)                   | Ant task integration (`org.wurbelizer.ant.*`).                             |
| [console-wurbelizer](console-wurbelizer/)           | Illustrative command-line wrapper.                                         |

## Resources

- **Documentation:** <https://wurbelizer.org>
- **Source:** <https://bitbucket.org/krake-oss/wurbelizer/>
- **Published by:** [Krake Softwaretechnik](https://krake.de)
