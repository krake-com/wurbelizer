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

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

/**
 * Tests the {@link HeapFileFactory}.
 */
public class HeapFileFactoryTest {

  @BeforeMethod
  public void clear() {
    HeapFileFactory.initialize();
  }

  @Test
  public void testCreateAndGet() throws WurbelException {
    HeapFile hf = HeapFileFactory.create("a", "text");
    assertSame(HeapFileFactory.get("a"), hf);
    assertEquals(hf.getText(), "text");
  }

  @Test
  public void testGetUnknownReturnsNull() {
    assertNull(HeapFileFactory.get("nope"));
  }

  @Test(expectedExceptions = WurbelException.class)
  public void testCreateDuplicateThrows() throws WurbelException {
    HeapFileFactory.create("dup", "1");
    HeapFileFactory.create("dup", "2");
  }

  @Test
  public void testCreateOrOverwriteCreatesNew() throws WurbelException {
    HeapFile hf = HeapFileFactory.createOrOverwrite("Src.java", "h", "body");
    assertEquals(hf.getText(), "body");
    assertEquals(hf.getSourceName(), "Src.java");
  }

  @Test
  public void testCreateOrOverwriteOverwrites() throws WurbelException {
    HeapFileFactory.createOrOverwrite("Src.java", "h", "old");
    HeapFile hf = HeapFileFactory.createOrOverwrite("Src.java", "h", "new");
    assertEquals(hf.getText(), "new");
    // still the same single instance
    assertSame(HeapFileFactory.get("h"), hf);
  }

  @Test(expectedExceptions = WurbelException.class)
  public void testCreateOrOverwriteFromDifferentSourceThrows() throws WurbelException {
    HeapFileFactory.createOrOverwrite("SrcA.java", "h", "a");
    HeapFileFactory.createOrOverwrite("SrcB.java", "h", "b");
  }

  @Test
  public void testDelete() throws WurbelException {
    HeapFileFactory.create("d", null);
    assertTrue(HeapFileFactory.delete("d"));
    assertFalse(HeapFileFactory.delete("d"));
    assertNull(HeapFileFactory.get("d"));
  }

  @Test
  public void testInitializeClears() throws WurbelException {
    HeapFileFactory.create("x", null);
    HeapFileFactory.initialize();
    assertNull(HeapFileFactory.get("x"));
  }
}
