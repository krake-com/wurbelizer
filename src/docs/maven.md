# Maven Integration

The easiest way to use the wurbelizer is via Maven.

For a detailed description please visit the plugin documentation for
the  [wurbelizer-maven-plugin](https://wurbelizer.org/wurbelizer/wurbelizer-maven-plugin/plugin-info.html).

Examples can be found here:

- [Wurbelizer Tutorial](https://github.com/harrharr/wurbelizer-tutorial)
- [Tentackle Tutorial](https://github.com/harrharr/tentackle-tutorial)

## Goals and parameters

The plugin provides three goals:

| Goal          | Default phase          | Purpose                                                       |
|---------------|------------------------|---------------------------------------------------------------|
| `wurbile`     | `generate-sources`     | compiles the `.wrbl` sources to Java sources and `.ser` files |
| `wurbel`      | `process-sources`      | runs the wurbler on the main sources                          |
| `test-wurbel` | `process-test-sources` | runs the wurbler on the test sources                          |

Parameters common to all goals:

| Parameter   | Default                             | Description                                              |
|-------------|-------------------------------------|----------------------------------------------------------|
| `skip`      | `true` for `pom` packaging          | skips the execution                                      |
| `verbosity` | `${wurbel.verbosity}`               | `default`, `info` or `debug`                             |
| `charset`   | `${project.build.sourceEncoding}`   | the encoding of the source files                         |
| `filesets`  | see below                           | the files to process, in the given order                 |

Parameters of the `wurbile` goal:

| Parameter                                  | Default                             | Description                                                |
|--------------------------------------------|-------------------------------------|------------------------------------------------------------|
| `wurbletSourceDir` (`wurbile.sourceDir`)   | `src/main/wurblets`                 | the `.wrbl` sources, if no `filesets` given (`**/*.wrbl`)  |
| `wurbletTargetJavaDir` (`wurbile.javaDir`) | `target/generated-sources/wurblets` | the generated Java sources, added as a compile source root |
| `wurbletTargetTextDir` (`wurbile.textDir`) | `target/classes`                    | the generated `.ser` files                                 |
| `indent`                                   | `-1` (the wurbiler's default)       | the indentation of the generated Java source               |

Parameters of the `wurbel` and `test-wurbel` goals:

| Parameter                                                   | Description                                                                                                      |
|-------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------|
| `wurbletPaths` (required)                                   | the packages prepended to wurblet names that are not fully qualified                                             |
| `wurbletProperties`                                         | extra properties for the wurblets, see below                                                                     |
| `wurbletDependencies`                                       | the artifacts containing the wurblets, loaded in a separate classloader, see [Parallel builds](#parallel-builds) |
| `sourceDir` (`wurbel.sourceDir`, `wurbel.testSourceDir`)    | the sources, if no `filesets` given (`**/*.java`)                                                                |
| `analyzeDir` (`wurbel.analyzeDir`, `wurbel.testAnalyzeDir`) | `target/analyze` or `target/test-analyze`: extra information for the wurblets, see `Wurbler.getAnalyzeFile()`    |
| `wurbelDir` (`wurbel.wurbelDir`)                            | `target/wurbel`: the directory for files created by the wurblets, available as `$wurbelDir`                      |
| `dryRun`                                                    | only parses the sources, no wurblets are executed                                                                |
| `includeAnchors`, `excludeAnchors`                          | lists of regular expressions matching the tags of the anchors to process or to skip                              |
| `includeWurblets`, `excludeWurblets`                        | lists of regular expressions matching the wurblet names (as written in the anchors) to process or to skip        |
| `firstPhase`, `lastPhase`                                   | restrict the [phases](source-syntax.md#processing-order-and-phases) to process (default: all, starting with 0)   |

The include and exclude lists are applied together: an anchor is processed only if it
matches all given include lists and none of the exclude lists. Skipped anchors keep
their guarded blocks. Together with `firstPhase` and `lastPhase`, this is mainly useful
for tracking down a problem in a single wurblet.

The following extra properties are predefined and can be used in the values of the
`wurbletProperties` as well as `$`-variables in the sources:

| Property                                  | Value                                                                          |
|-------------------------------------------|--------------------------------------------------------------------------------|
| `project`                                 | the base directory of the current Maven module                                 |
| `parentProject`, `parentParentProject`, … | the base directories of the parent modules                                     |
| `projectRoot`                             | the base directory of the topmost parent module                                |
| `wurbelDir`                               | the value of the `wurbelDir` parameter                                         |
| `currentDir`, `currentFile`               | the directory and the name of the file being processed (also set by `wurbile`) |

## A real-world configuration

The following is the plugin configuration of the `tentackle-persistence` module of the
[Tentackle](https://tentackle.org) framework, which generates its entire persistence
layer with the Wurbelizer. It shows how the pieces fit together in a non-trivial project:

```xml
<plugin>
  <groupId>org.wurbelizer</groupId>
  <artifactId>wurbelizer-maven-plugin</artifactId>

  <executions>
    <execution>
      <goals>
        <goal>wurbel</goal>
      </goals>
    </execution>
  </executions>

  <configuration>
    <wurbletDependencies>
      <wurbletDependency>
        <groupId>${project.groupId}</groupId>
        <artifactId>tentackle-persistence-wurblets</artifactId>
        <version>${project.version}</version>
      </wurbletDependency>
    </wurbletDependencies>

    <verbosity>info</verbosity>

    <wurbletPaths>
      <param>org.tentackle.wurblet</param>
      <param>org.tentackle.persist.wurblet</param>
    </wurbletPaths>

    <wurbletProperties>
      <property>
        <name>guardtype</name>
        <value>netbeans</value>
      </property>
      <property>
        <name>foldtype</name>
        <value>collapsed</value>
      </property>
      <property>
        <name>model</name>
        <value>${wurbel.wurbelDir}/model</value>
      </property>
      <property>
        <name>backends</name>
        <value>all</value>
      </property>
    </wurbletProperties>

    <filesets>
      <!-- wurbel PDOs first -->
      <fileset>
        <includes>
          <include>**/*.wurb</include>
          <include>**/*.java</include>
        </includes>
        <followSymlinks>false</followSymlinks>
      </fileset>
      <!-- wurbel trip delegates next -->
      <fileset>
        <includes>
          <include>**/trip/*.java</include>
        </includes>
        <followSymlinks>false</followSymlinks>
      </fileset>
    </filesets>
  </configuration>
</plugin>
```

Reading it section by section:

- **`wurbletDependencies`** pulls in the artifact containing the compiled wurblets. The
  wurblets themselves are built in a separate module with the `wurbile` goal, so that
  they are available as an ordinary Maven dependency to every module that uses them.
  Each execution also loads them in its own classloader, see [Parallel builds](#parallel-builds).
- **`wurbletPaths`** lists the packages the wurbler prepends to a wurblet name found in
  an anchor. Because both Tentackle wurblet packages are listed, an anchor may simply
  read `// @wurblet methods MethodsImpl` instead of naming the fully qualified class.
  See the [package directive](wurblet-syntax.md#package).
- **`wurbletProperties`** land in the `extra` namespace and are visible to every wurblet
  as `$`-variables. `guardtype` and `foldtype` set the defaults for the generated blocks
  (an individual anchor can still override the fold, see
  [Editor Fold](source-syntax.md#editor-fold)); `model` points to the directory holding
  the model files created by the here-documents, which is why sources can refer to
  `$model/$tablename.map`; `backends` is a Tentackle-specific setting telling its
  wurblets which SQL dialects to generate for.
- **`filesets`** determine *which* sources are wurbeled and, importantly, *in which
  order*. Here the entities are processed first and the remote delegates under `trip/`
  afterwards, because the delegates consume heap files that the entity wurblets produce.
  See [Cross-file generation with heap files](source-syntax.md#cross-file-generation-with-heap-files)
  and [Processing Order and Phases](source-syntax.md#processing-order-and-phases).

Note that `**/*.wurb` is included alongside `**/*.java`. When a fileset matches a
`.wurb` file, the plugin does not wurbel the property file itself but substitutes the
corresponding `.java` file, remembering the `.wurb` path as that source's property file.
This is what makes it possible to keep the variables in a resource directory instead of
next to the source, and a file already reached through another pattern is never
processed twice.

## Parallel builds

All goals (`wurbile`, `wurbel` and `test-wurbel`) are thread-safe and can be used in
parallel builds (`mvn -T ...`). Each execution runs in its own context, so the
following are isolated per module:

- the file encoding (`project.build.sourceEncoding`),
- the extra properties, including `wurbletProperties`, `currentDir` and `currentFile`,
- the heap files: modules may use the same heap-file names without clashing,
- the classloader for the `wurbletDependencies`. Each execution gets its own child realm
  of the plugin's classrealm, so different modules may even use different versions of
  the same wurblet artifact.

Wurblets are executed sequentially across all modules unless they declare themselves
thread-safe by overriding `isThreadSafe()`, see [Thread safety](writing-wurblets.md#thread-safety).
Only thread-safe wurblets run in parallel.

This per-execution isolation applies only to wurblets declared as `wurbletDependencies`. If
the wurblets are added to the plugin's own `<dependencies>` instead, they are loaded by the
plugin's classrealm, which all executions of the build share, together with any static state.
Wurblets that rely on the isolation must check which classloader loaded them, see
[Thread safety](writing-wurblets.md#thread-safety). The Tentackle wurblets, for example, run
in parallel only if they are declared as `wurbletDependencies`, as in the configuration above.

If the wurblets are built in a module of the same reactor, keep the build order in mind. Maven
determines the order of the modules from their dependencies, including plugin `<dependencies>`, but
it does not look into the plugin's `<configuration>`. So a `wurbletDependency` alone does not make the
consuming module depend on the wurblet module. In a sequential build, the order of the `<modules>`
usually hides this. In a parallel build, however, the consuming module may start before the wurblet
module is built, and then picks up an outdated artifact from the local repository or fails. To enforce
the build order, also declare a regular dependency on the wurblet module. With type `pom` and scope
`provided`, it only adds the edge to the build order, without putting the wurblets on the compile
classpath or passing them on to consumers of the module. Example:

```xml
<dependency>
  <groupId>${project.groupId}</groupId>
  <artifactId>myapp-wurblets</artifactId>
  <version>${project.version}</version>
  <type>pom</type>
  <scope>provided</scope>
</dependency>
```

## Maven 4

The plugin requires Maven 3.9.10 or newer and also runs on Maven 4. It is built against the
Maven 3 plugin API, so Maven 4 runs it through its compatibility layer. Projects don't need
any change to their POMs.

Maven 4 is supported in the following ways:

- The custom configurator `set-classrealm`, which gives the mojos access to the plugin's
  classrealm, keeps working. So wurblets compiled by `wurbile` in one module can still be
  used by `wurbel` in other modules of the same reactor.
- `wurbletDependencies` are resolved through the Maven Resolver API that Maven 4 still
  provides to Maven 3 plugins. Each execution keeps its own child realm, as described in
  [Parallel builds](#parallel-builds).
- All goals are thread-safe on Maven 4 as well, including `mvn -T ...`.
- The plugin passes Maven 4's plugin validation with no reported issues.

---

**Navigation:** [← Previous: Build Tool Integration](integration.md) | [Contents](toc.md) · [Index](index.md) | [Next: Ant Integration →](ant.md)
