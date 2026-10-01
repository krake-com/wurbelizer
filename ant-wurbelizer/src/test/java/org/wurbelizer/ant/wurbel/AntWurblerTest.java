/*
 * Wurbelizer - https://wurbelizer.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */


package org.wurbelizer.ant.wurbel;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.Path;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wurbelizer.wurblet.Wurblet;
import org.wurbelizer.wurblet.WurbletData;

import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import static org.testng.Assert.assertThrows;
import static org.testng.Assert.assertTrue;

/**
 * Tests the {@link AntWurbler} task.
 */
public class AntWurblerTest {

  private java.nio.file.Path dir;

  @BeforeClass
  public void createFixedTextObjects() throws Exception {
    // the wurbler expects a <wurblet>.ser next to the wurblet's class
    for (Class<? extends Wurblet> clazz : List.of(GenerateTestWurblet.class, TerminateTestWurblet.class)) {
      java.nio.file.Path classFile = java.nio.file.Path.of(clazz.getResource(clazz.getSimpleName() + ".class").toURI());
      try (ObjectOutputStream os = new ObjectOutputStream(
             Files.newOutputStream(classFile.resolveSibling(clazz.getSimpleName() + ".ser")))) {
        os.writeObject(new WurbletData(new String[0], null));
      }
    }
  }

  @BeforeMethod
  public void createDir() throws IOException {
    dir = Files.createTempDirectory("antwurbler");
  }

  @AfterMethod
  public void deleteDir() throws IOException {
    if (dir != null) {
      try (Stream<java.nio.file.Path> paths = Files.walk(dir)) {
        paths.sorted(Comparator.reverseOrder()).map(java.nio.file.Path::toFile).forEach(File::delete);
      }
      dir = null;
    }
  }

  @Test
  public void testWurbletsRunAfterHereDocRestart() throws Exception {
    // the here-document is written to disk in the first pass, which restarts the phase
    java.nio.file.Path source = createSource(
        "/*\n * @> " + dir.resolve("model.txt") + "\n * some model\n * @<\n */\n" +
        "public class Src {\n\n  // @wurblet gen GenerateTestWurblet\n\n}\n");
    createTask().execute();
    assertTrue(Files.isRegularFile(dir.resolve("model.txt")));
    assertTrue(Files.readString(source, StandardCharsets.UTF_8).contains("// generated"));
  }

  @Test
  public void testSourceErrorFailsBuild() throws Exception {
    createSource("public class Src {\n  //<editor-fold desc=\"x\">//GEN-BEGIN:gen\n}\n");   // missing end of guarded block
    assertThrows(BuildException.class, () -> createTask().execute());
  }

  @Test
  public void testTerminationFailsBuild() throws Exception {
    createSource("public class Src {\n\n  // @wurblet gen TerminateTestWurblet\n\n}\n");
    assertThrows(BuildException.class, () -> createTask().execute());
  }


  private java.nio.file.Path createSource(String text) throws IOException {
    return Files.writeString(dir.resolve("Src.java"), text, StandardCharsets.UTF_8);
  }

  private AntWurbler createTask() {
    Project project = new Project();
    project.init();
    AntWurbler task = new AntWurbler();
    task.setProject(project);
    task.setSrcdir(new Path(project, dir.toString()));
    task.setWurbletpath("org.wurbelizer.ant.wurbel");
    task.setAnalyzeDir(dir.toString());
    return task;
  }

}
