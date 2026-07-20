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

import org.testng.annotations.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

/**
 * Tests the {@link NestedReader}.
 */
public class NestedReaderTest {

  private String readAll(NestedReader reader) throws IOException {
    StringBuilder buf = new StringBuilder();
    int c;
    while ((c = reader.read()) != -1) {
      buf.append((char) c);
    }
    return buf.toString();
  }

  @Test
  public void testReadFromReader() throws IOException {
    NestedReader reader = new NestedReader(new BufferedReader(new StringReader("ab\ncd")), "mem");
    // readLine() strips line terminators, read() re-adds a single '\n' after each line
    assertEquals(readAll(reader), "ab\ncd\n");
    assertEquals(reader.getLineNo(), 2);
    assertFalse(reader.isIncluded());
    assertNull(reader.getFile());
    assertEquals(reader.getPath(), "mem");
    assertEquals(reader.toString(), "mem");
  }

  @Test
  public void testEmptyReader() throws IOException {
    NestedReader reader = new NestedReader(new BufferedReader(new StringReader("")), "empty");
    assertEquals(readAll(reader), "");
    assertEquals(reader.getLineNo(), 0);
  }

  @Test
  public void testLinePositionAdvances() throws IOException {
    NestedReader reader = new NestedReader(new BufferedReader(new StringReader("xy")), "pos");
    assertEquals(reader.read(), 'x');
    assertEquals(reader.getLinePos(), 1);
    assertEquals(reader.read(), 'y');
    assertEquals(reader.getLinePos(), 2);
  }

  @Test
  public void testFileConstructor() throws IOException {
    File tmp = File.createTempFile("nestedreader", ".txt");
    tmp.deleteOnExit();
    Files.writeString(tmp.toPath(), "line1\nline2\n", StandardCharsets.UTF_8);

    NestedReader reader = new NestedReader(tmp.getPath());
    assertTrue(reader.isIncluded());
    assertEquals(reader.getFile(), tmp);
    assertEquals(reader.getPath(), tmp.getPath());
    assertEquals(reader.toString(), tmp.getName());
    assertEquals(readAll(reader), "line1\nline2\n");
    assertEquals(reader.getLineNo(), 2);
  }

  @Test(expectedExceptions = IOException.class)
  public void testMissingFileThrows() throws IOException {
    new NestedReader("/this/does/not/exist/really.wrbl");
  }
}
