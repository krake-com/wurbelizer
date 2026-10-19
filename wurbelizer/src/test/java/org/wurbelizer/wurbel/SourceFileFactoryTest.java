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
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wurbelizer.misc.DefaultLogger;
import org.wurbelizer.misc.Verbosity;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.HashSet;
import java.util.stream.Stream;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotSame;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

/**
 * Tests the {@link SourceFileFactory} and the caching semantics of {@link SourceFile}.
 */
public class SourceFileFactoryTest {

  private Path dir;

  @BeforeMethod
  public void setUp() throws IOException {
    SourceFileFactory.initialize();
    dir = Files.createTempDirectory("sourcefiles");
  }

  @AfterMethod
  public void tearDown() throws IOException {
    SourceFileFactory.initialize();
    try (Stream<Path> paths = Files.walk(dir)) {
      paths.sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
    }
  }

  @Test
  public void testOpenIsCached() throws Exception {
    Path path = write("a.txt", "abc");
    SourceFile file = SourceFileFactory.open(path.toString());
    assertSame(SourceFileFactory.open(path.toString()), file);
    // same file, other name
    assertSame(SourceFileFactory.open(dir.resolve(".").resolve("a.txt").toString()), file);
    assertEquals(file.getOrgText(), "abc");
  }

  @Test
  public void testInitializeClearsCache() throws Exception {
    Path path = write("a.txt", "abc");
    SourceFile file = SourceFileFactory.open(path.toString());
    SourceFileFactory.initialize();
    assertNotSame(SourceFileFactory.open(path.toString()), file);
  }

  @Test
  public void testFlushUpdatesOrgTextAndDiscardsStream() throws Exception {
    Path path = write("a.txt", "old");
    SourceFile file = SourceFileFactory.open(path.toString());
    file.getStream().print("new");
    assertTrue(file.flush());
    assertEquals(read(path), "new");
    assertEquals(file.getOrgText(), "new");
    assertNull(file.getNewText());

    // same contents again: no update, no concatenation
    file.getStream().print("new");
    assertFalse(file.flush());
    assertEquals(read(path), "new");
  }

  @Test
  public void testFlushAllWritesChangedFilesOnly() throws Exception {
    Path changed = write("changed.txt", "old");
    Path unchanged = write("unchanged.txt", "same");
    Path created = dir.resolve("sub").resolve("created.txt");
    SourceFileFactory.open(changed.toString()).getStream().print("new");
    SourceFileFactory.open(unchanged.toString()).getStream().print("same");
    SourceFileFactory.open(created.toString()).getStream().print("created");
    SourceFileFactory.open(dir.resolve("untouched.txt").toString());   // no stream: not written

    assertEquals(SourceFileFactory.flushAll(), 2);
    assertEquals(read(changed), "new");
    assertEquals(read(unchanged), "same");
    assertEquals(read(created), "created");
    assertFalse(Files.exists(dir.resolve("untouched.txt")));
  }

  @Test
  public void testResetAllClearsStreams() throws Exception {
    Path path = write("a.txt", "old");
    SourceFileFactory.open(path.toString()).getStream().print("first loop ");
    SourceFileFactory.resetAll();
    SourceFileFactory.open(path.toString()).getStream().print("last loop");
    assertEquals(SourceFileFactory.flushAll(), 1);
    assertEquals(read(path), "last loop");
  }

  @Test
  public void testHereDocumentToFile() throws Exception {
    Path target = dir.resolve("here.txt");
    Path source = write("Src.java",
                        "/*\n" +
                        " * @> " + target + "\n" +
                        " * some text\n" +
                        " * @<\n" +
                        " */\n" +
                        "public class Src {}\n");

    assertEquals(wurbel(source).updateType(), WurbelResult.UpdateType.HEREDOCS);
    String text = read(target);
    assertTrue(text.contains("some text"), text);

    // second run: heredoc unchanged
    assertEquals(wurbel(source).updateType(), WurbelResult.UpdateType.UNCHANGED);
    assertEquals(read(target), text);
  }

  @Test(expectedExceptions = WurbelException.class)
  public void testConflictingHereDocumentsToSameFile() throws Exception {
    Path target = dir.resolve("here.txt");
    Path source = write("Src.java",
                        "/*\n" +
                        " * @> " + target + "\n" +
                        " * first\n" +
                        " * @<\n" +
                        " */\n" +
                        "public class Src {\n" +
                        "  /*\n" +
                        "   * @> " + target + "\n" +
                        "   * second\n" +
                        "   * @<\n" +
                        "   */\n" +
                        "}\n");
    wurbel(source);
  }


  private WurbelResult wurbel(Path source) throws Exception {
    return new SourceWurbler(source.toString(), null, null, dir.toString(), 0, null, false,
                             new DefaultLogger(), Verbosity.DEFAULT, new HashSet<>(), new HashSet<>()).wurbel();
  }

  private Path write(String name, String text) throws IOException {
    return Files.writeString(dir.resolve(name), text, StandardCharsets.UTF_8);
  }

  private String read(Path path) throws IOException {
    return Files.readString(path, StandardCharsets.UTF_8);
  }

}
