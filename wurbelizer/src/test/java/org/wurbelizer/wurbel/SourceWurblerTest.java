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


package org.wurbelizer.wurbel;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wurbelizer.misc.DefaultLogger;
import org.wurbelizer.misc.Verbosity;
import org.wurbelizer.wurblet.WurbletData;

import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Tests the {@link SourceWurbler}.
 */
public class SourceWurblerTest {

  private Path dir;

  @BeforeClass
  public void createFixedTextObject() throws Exception {
    // the wurbler expects a <wurblet>.ser next to the wurblet's class
    Path classFile = Path.of(SerialTestWurblet.class.getResource("SerialTestWurblet.class").toURI());
    try (ObjectOutputStream os = new ObjectOutputStream(Files.newOutputStream(classFile.resolveSibling("SerialTestWurblet.ser")))) {
      os.writeObject(new WurbletData(new String[0], null));
    }
  }

  @AfterMethod
  public void deleteDir() throws IOException {
    if (dir != null) {
      try (Stream<Path> paths = Files.walk(dir)) {
        paths.sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
      }
      dir = null;
    }
  }

  @Test
  public void testAnchorPhaseIsCollected() throws Exception {
    Path source = createSource("Src.java", "public class Src {\n\n  // @wurblet(phase=3) gen SerialTestWurblet\n\n}\n");
    Set<Integer> collectedPhases = new TreeSet<>();

    // phase 1: the anchor is skipped, but its phase must be collected
    WurbelResult result = wurbel(source, 1, collectedPhases);
    assertEquals(result.wurbletCount(), 0);
    assertTrue(collectedPhases.contains(3), "collected phases " + collectedPhases);
    assertFalse(Files.readString(source, StandardCharsets.UTF_8).contains("// generated"));

    // phase 3: the anchor is executed
    result = wurbel(source, 3, collectedPhases);
    assertTrue(result.wurbletCount() > 0);    // counted for each pass over the source
    assertTrue(Files.readString(source, StandardCharsets.UTF_8).contains("// generated"));
  }

  @Test
  public void testUnregisteredExtensionUsesDefaultSourceType() throws Exception {
    Path source = createSource("model.xml", "<model>\n<!--\n// @wurblet gen SerialTestWurblet\n-->\n</model>\n");
    WurbelResult result = wurbel(source, 1, new TreeSet<>());
    assertEquals(result.errors(), 0);
    assertEquals(result.updateType(), WurbelResult.UpdateType.UPDATED);
    assertTrue(Files.readString(source, StandardCharsets.UTF_8).contains("// generated"));
  }


  private Path createSource(String name, String text) throws IOException {
    dir = Files.createTempDirectory("wurbler");
    return Files.writeString(dir.resolve(name), text, StandardCharsets.UTF_8);
  }

  private WurbelResult wurbel(Path source, int phase, Set<Integer> collectedPhases) throws Exception {
    return WurbelContext.call(new WurbelContext(), () ->
        new SourceWurbler(source.toString(), null, new String[] { "org.wurbelizer.wurbel" }, dir.toString(),
                          phase, null, false, new DefaultLogger(), Verbosity.DEFAULT,
                          new HashSet<>(), collectedPhases).wurbel());
  }

}
