# Maven Integration

The easiest way to use the wurbelizer is via Maven.

For a detailed description please visit the plugin documentation for
the  [wurbelizer-maven-plugin](https://wurbelizer.org/wurbelizer/wurbelizer-maven-plugin/plugin-info.html).

Examples can be found here:

- [Wurbelizer Tutorial](https://github.com/harrharr/wurbelizer-tutorial)
- [Tentackle Tutorial](https://github.com/harrharr/tentackle-tutorial)

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
  See [Cross-file generation with heap files](source-syntax.md#cross-file-generation-with-heap-files).

Note that `**/*.wurb` is included alongside `**/*.java`. When a fileset matches a
`.wurb` file, the plugin does not wurbel the property file itself but substitutes the
corresponding `.java` file, remembering the `.wurb` path as that source's property file.
This is what makes it possible to keep the variables in a resource directory instead of
next to the source, and a file already reached through another pattern is never
processed twice.

---

**Navigation:** [← Previous: Build Tool Integration](integration.md) | [Contents](toc.md) · [Index](index.md) | [Next: Ant Integration →](ant.md)
