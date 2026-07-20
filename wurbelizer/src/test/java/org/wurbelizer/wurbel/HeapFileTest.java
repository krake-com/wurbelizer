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

import org.testng.annotations.Test;

import java.io.BufferedReader;
import java.io.IOException;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Tests the in-memory {@link HeapFile}.
 */
public class HeapFileTest {

  @Test
  public void testInitialContents() {
    HeapFile hf = new HeapFile("f", "hello");
    assertEquals(hf.getText(), "hello");
    assertEquals(hf.getName(), "f");
    assertEquals(hf.toString(), "f");
  }

  @Test
  public void testNullInitialContents() {
    HeapFile hf = new HeapFile("f", null);
    assertEquals(hf.getText(), "");
  }

  @Test
  public void testPrintStreamAppends() {
    HeapFile hf = new HeapFile("f", "a");
    hf.getPrintStream().print("bc");
    assertEquals(hf.getText(), "abc");
  }

  @Test
  public void testReset() {
    HeapFile hf = new HeapFile("f", "content");
    hf.reset();
    assertEquals(hf.getText(), "");
  }

  @Test
  public void testReaderReturnsContents() throws IOException {
    HeapFile hf = new HeapFile("f", "line1\nline2");
    try (BufferedReader reader = new BufferedReader(hf.getReader())) {
      assertEquals(reader.readLine(), "line1");
      assertEquals(reader.readLine(), "line2");
    }
  }

  @Test
  public void testDiscard() {
    HeapFile hf = new HeapFile("f", "x");
    assertFalse(hf.isDiscarded());
    hf.discard();
    assertTrue(hf.isDiscarded());
  }

  @Test
  public void testSavePointAndRevert() throws WurbelException {
    HeapFile hf = new HeapFile("f", "original");
    hf.createSavePoint();
    hf.getPrintStream().print(" plus more");
    assertEquals(hf.getText(), "original plus more");
    hf.revertToSavePoint();
    assertEquals(hf.getText(), "original");
  }

  @Test
  public void testRevertWithoutSavePointResets() throws WurbelException {
    HeapFile hf = new HeapFile("f", "something");
    // no savepoint: revert falls back to a plain reset
    hf.revertToSavePoint();
    assertEquals(hf.getText(), "");
  }

  @Test
  public void testInvocationCount() {
    HeapFile hf = new HeapFile("f", null);
    assertEquals(hf.getInvocationCount(), 0);
    hf.setInvocationCount(7);
    assertEquals(hf.getInvocationCount(), 7);
  }

  @Test
  public void testSourceName() {
    HeapFile hf = new HeapFile("f", null);
    hf.setSourceName("Foo.java");
    assertEquals(hf.getSourceName(), "Foo.java");
  }
}
