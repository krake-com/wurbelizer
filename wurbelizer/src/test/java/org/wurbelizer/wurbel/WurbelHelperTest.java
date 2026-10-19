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
import java.util.Map;
import java.util.Properties;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

/**
 * Tests the static helper methods in {@link WurbelHelper}.
 */
public class WurbelHelperTest {

  private Properties props(String... keyValues) {
    Properties p = new Properties();
    for (int i = 0; i < keyValues.length; i += 2) {
      p.setProperty(keyValues[i], keyValues[i + 1]);
    }
    return p;
  }

  // --- translateVariables ---

  @Test
  public void testTranslateSimple() {
    assertEquals(WurbelHelper.translateVariables("$alpha", props("alpha", "A")), "A");
  }

  @Test
  public void testTranslateEmbeddedInText() {
    assertEquals(WurbelHelper.translateVariables("x=$alpha;", props("alpha", "A")), "x=A;");
  }

  @Test
  public void testTranslateCurlyBraces() {
    assertEquals(WurbelHelper.translateVariables("${alpha}Suffix", props("alpha", "A")), "ASuffix");
  }

  @Test
  public void testTranslateRecursive() {
    // alpha resolves to $beta which in turn resolves to X
    assertEquals(WurbelHelper.translateVariables("$alpha", props("alpha", "$beta", "beta", "X")), "X");
  }

  @Test
  public void testTranslateUnknownLeftUnchanged() {
    assertEquals(WurbelHelper.translateVariables("$__nosuchvar__", props()), "$__nosuchvar__");
  }

  @Test
  public void testTranslateEscapedDollar() {
    // a backslash suppresses substitution and is removed in the final pass
    assertEquals(WurbelHelper.translateVariables("\\$alpha", props("alpha", "A")), "$alpha");
  }

  @Test
  public void testTranslateStringBuilderInPlace() {
    StringBuilder buf = new StringBuilder("$alpha");
    WurbelHelper.translateVariables(buf, props("alpha", "A"));
    assertEquals(buf.toString(), "A");
  }

  // --- indent ---

  @Test
  public void testIndentMultiLine() {
    assertEquals(WurbelHelper.indent("a\nb", 2), "  a\n  b");
  }

  @Test
  public void testIndentKeepsBlankLinesUnindented() {
    assertEquals(WurbelHelper.indent("a\n\nb", 1), " a\n\n b");
  }

  @Test
  public void testIndentZeroUnchanged() {
    assertEquals(WurbelHelper.indent("a\nb", 0), "a\nb");
  }

  @Test
  public void testIndentNull() {
    assertNull(WurbelHelper.indent(null, 4));
  }

  // --- scanPropertyLine ---

  @Test
  public void testScanPropertyLine() {
    Properties p = new Properties();
    assertEquals(WurbelHelper.scanPropertyLine("key = value", p), "key");
    assertEquals(p.getProperty("key"), "value");
  }

  @Test
  public void testScanPropertyLineStripsComment() {
    Properties p = new Properties();
    assertEquals(WurbelHelper.scanPropertyLine("key = value  # a comment", p), "key");
    assertEquals(p.getProperty("key"), "value");
  }

  @Test
  public void testScanPropertyLineNoAssignment() {
    Properties p = new Properties();
    assertNull(WurbelHelper.scanPropertyLine("just words", p));
    assertTrue(p.isEmpty());
  }

  @Test
  public void testScanPropertyLineOnlyComment() {
    Properties p = new Properties();
    assertNull(WurbelHelper.scanPropertyLine("# nothing here", p));
  }

  // --- string helpers ---

  @Test
  public void testFirstBefore() {
    assertEquals(WurbelHelper.firstBefore("a.b.c", '.'), "a");
    assertEquals(WurbelHelper.firstBefore("noDelim", '.'), "noDelim");
    assertNull(WurbelHelper.firstBefore(null, '.'));
  }

  @Test
  public void testLastAfter() {
    assertEquals(WurbelHelper.lastAfter("a.b.c", '.'), "c");
    assertEquals(WurbelHelper.lastAfter("noDelim", '.'), "noDelim");
    assertNull(WurbelHelper.lastAfter(null, '.'));
  }

  @Test
  public void testFirstLine() {
    assertEquals(WurbelHelper.firstLine("line1\nline2"), "line1");
    assertEquals(WurbelHelper.firstLine("single"), "single");
  }

  // --- line ending translation ---

  @Test
  public void testLFtoCRLF() {
    assertEquals(WurbelHelper.translateLFtoCRLF("a\nb"), "a\r\nb");
  }

  @Test
  public void testLFtoCRLFKeepsExistingCRLF() {
    assertEquals(WurbelHelper.translateLFtoCRLF("a\r\nb"), "a\r\nb");
  }

  @Test
  public void testCRLFtoLF() {
    assertEquals(WurbelHelper.translateCRLFtoLF("a\r\nb\r\nc"), "a\nb\nc");
  }

  @Test
  public void testLineEndingRoundTrip() {
    String lf = "one\ntwo\nthree";
    assertEquals(WurbelHelper.translateCRLFtoLF(WurbelHelper.translateLFtoCRLF(lf)), lf);
  }

  // --- determineIndent ---

  @Test
  public void testDetermineIndent() {
    assertEquals(WurbelHelper.determineIndent("    x"), 4);
  }

  @Test
  public void testDetermineIndentSkipsLeadingBlankLines() {
    assertEquals(WurbelHelper.determineIndent("\n\n  y"), 2);
  }

  @Test
  public void testDetermineIndentNoLeadingSpace() {
    assertEquals(WurbelHelper.determineIndent("x"), 0);
    assertEquals(WurbelHelper.determineIndent(null), 0);
  }

  // --- heap-file-aware I/O ---

  @Test
  public void testOpenReaderForHeapFile() throws Exception {
    HeapFileFactory.initialize();
    HeapFileFactory.create("myheap", "heap contents");
    try (BufferedReader reader = new BufferedReader(WurbelHelper.openReader(".myheap"))) {
      assertEquals(reader.readLine(), "heap contents");
    }
  }

  @Test(expectedExceptions = WurbelException.class)
  public void testOpenReaderMissingHeapFileThrows() throws Exception {
    HeapFileFactory.initialize();
    WurbelHelper.openReader(".doesNotExist");
  }

  @Test(expectedExceptions = WurbelDiscardException.class)
  public void testOpenReaderDiscardedHeapFileThrows() throws Exception {
    HeapFileFactory.initialize();
    HeapFileFactory.create("discardedHeap", "x").discard();
    WurbelHelper.openReader(".discardedHeap");
  }

  @Test(expectedExceptions = WurbelException.class)
  public void testOpenReaderMissingRealFileThrows() throws Exception {
    WurbelHelper.openReader("/no/such/file/really.txt");
  }

  @Test
  public void testDeleteHeapFile() throws Exception {
    HeapFileFactory.initialize();
    HeapFileFactory.create("todelete", null);
    assertTrue(WurbelHelper.deleteFile(".todelete"));
    assertFalse(WurbelHelper.deleteFile(".todelete"));
  }

  // --- SPI accessors ---

  @Test
  public void testSourceRegionMap() {
    Map<String, SourceRegion> regions = WurbelHelper.getSourceRegionMap();
    assertTrue(regions.containsKey("netbeans"));
    assertTrue(regions.containsKey("coffeebytes"));
    assertTrue(regions.containsKey("visualstudio"));
  }

  @Test
  public void testDefaultSourceRegionIsNetbeans() {
    assertEquals(WurbelHelper.getDefaultSourceRegion().type(), "netbeans");
  }

  @Test
  public void testSourceTypeMap() {
    Map<String, SourceType> types = WurbelHelper.getSourceTypeMap();
    assertTrue(types.containsKey(".java"));
    assertTrue(types.containsKey(""));
  }

  @Test
  public void testDefaultSourceType() {
    // NoneSourceType (extension "") is the registered default
    WurbelHelper.getSourceTypeMap();     // trigger lazy init of the default
    assertEquals(WurbelHelper.getDefaultSourceType().getFileExtension(), "");
  }

  @Test
  public void testUnregisteredExtensionFallsBackToDefaultSourceType() throws WurbelException {
    assertEquals(SourceType.get(".xml").getFileExtension(), "");
    assertEquals(SourceType.get(null).getFileExtension(), "");
    assertEquals(SourceType.get(".java").getFileExtension(), ".java");
  }
}
