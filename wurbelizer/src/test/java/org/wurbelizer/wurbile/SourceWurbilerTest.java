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


package org.wurbelizer.wurbile;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;
import org.wurbelizer.misc.DefaultLogger;
import org.wurbelizer.misc.Settings;
import org.wurbelizer.misc.Verbosity;
import org.wurbelizer.wurbel.WurbelContext;
import org.wurbelizer.wurblet.WurbletData;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Tests the {@link SourceWurbiler}.
 */
public class SourceWurbilerTest {

  private Path dir;

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
  public void testWurbletSourceIsReadWithConfiguredCharset() throws Exception {
    dir = Files.createTempDirectory("wurbiler");
    Path wrbl = dir.resolve("Umlaut.wrbl");
    Files.writeString(wrbl, "@{package test.wurblets}@\n// Grätsche\n", StandardCharsets.ISO_8859_1);

    int errors = WurbelContext.call(new WurbelContext(), () -> {
      Settings.setEncodingCharset(StandardCharsets.ISO_8859_1);
      return new SourceWurbiler(wrbl.toFile(), dir.toFile(), dir.toFile(), new DefaultLogger(), Verbosity.DEFAULT).compile();
    });
    assertEquals(errors, 0);

    // generated files are placed in the package directory
    Path pkgDir = dir.resolve("test").resolve("wurblets");
    assertTrue(Files.isRegularFile(pkgDir.resolve("Umlaut.java")));
    try (ObjectInputStream is = new ObjectInputStream(Files.newInputStream(pkgDir.resolve("Umlaut.ser")))) {
      WurbletData data = (WurbletData) is.readObject();
      assertTrue(String.join("", data.getSource()).contains("Grätsche"));
    }
  }

}
