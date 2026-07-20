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
import org.wurbelizer.misc.DefaultLogger;
import org.wurbelizer.misc.Verbosity;

import java.util.List;
import java.util.Properties;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * Tests the {@link SourceDocument} parsing and guarded-block manipulation.
 */
public class SourceDocumentTest {

  private final NetbeansRegion region = new NetbeansRegion();

  private SourceDocument doc(String text) throws SourceException {
    return new SourceDocument(text, "Test.java", new Properties(), new DefaultLogger(), Verbosity.DEFAULT);
  }

  // --- parsing into elements ---

  @Test
  public void testPlainCodeIsSingleElement() throws SourceException {
    SourceDocument d = doc("int x = 1;");
    List<SourceElement> elems = d.getElements();
    assertEquals(elems.size(), 1);
    assertTrue(elems.getFirst().isCode());
  }

  @Test
  public void testSingleLineComment() throws SourceException {
    SourceDocument d = doc("// a comment\nint x;");
    List<SourceElement> elems = d.getElements();
    assertEquals(elems.size(), 2);
    assertEquals(elems.get(0).getType(), SourceElement.Type.SINGLE_COMMENT);
    assertTrue(elems.get(0).isComment());
    assertTrue(elems.get(1).isCode());
  }

  @Test
  public void testBlockComment() throws SourceException {
    SourceDocument d = doc("int x;/* block */int y;");
    List<SourceElement> elems = d.getElements();
    assertEquals(elems.size(), 3);
    assertTrue(elems.get(0).isCode());
    assertEquals(elems.get(1).getType(), SourceElement.Type.BLOCK_COMMENT);
    assertTrue(elems.get(2).isCode());
  }

  @Test
  public void testInlineCommentIsTreatedAsCode() throws SourceException {
    // "/**/" is deliberately NOT a block comment but part of the code
    SourceDocument d = doc("int x = /**/0/**/;");
    List<SourceElement> elems = d.getElements();
    assertEquals(elems.size(), 1);
    assertTrue(elems.getFirst().isCode());
  }

  @Test
  public void testGetLineNumber() throws SourceException {
    SourceDocument d = doc("a\nb\nc");
    assertEquals(d.getLineNumber(0), 1);   // 'a'
    assertEquals(d.getLineNumber(2), 2);   // 'b'
    assertEquals(d.getLineNumber(4), 3);   // 'c'
  }

  @Test
  public void testToStringStripsPath() throws SourceException {
    SourceDocument d = new SourceDocument("code", "some" + java.io.File.separator + "pkg" + java.io.File.separator + "Foo.java",
                                          new Properties(), new DefaultLogger(), Verbosity.DEFAULT);
    assertEquals(d.toString(), "Foo.java");
  }

  // --- guarded blocks ---

  private String guarded(String name, String body) {
    return region.createBegin(null, name, null) + "\n" + body + "\n" + region.createEnd(null, name, null);
  }

  @Test
  public void testParseExistingGuardedBlock() throws SourceException {
    SourceDocument d = doc("class A {\n" + guarded("mytag", "  int generated;") + "\n}\n");
    SourceElement guardElem = null;
    for (SourceElement elem : d.getElements()) {
      if (elem.isGuarded()) {
        guardElem = elem;
      }
    }
    assertNotNull(guardElem, "expected a guarded element");
    assertEquals(guardElem.getGuardedName(), "mytag");
  }

  @Test
  public void testInsertGuarded() throws SourceException {
    SourceDocument d = doc("class A {\n}\n");
    int before = d.getElements().size();
    d.insertGuarded(region, null, "tag", "  generated code", d.getElements().size(), null);
    assertTrue(d.getText().toString().contains("GEN-BEGIN:tag"));
    assertTrue(d.getText().toString().contains("GEN-END:tag"));
    assertTrue(d.getElements().size() > before);
    // the freshly inserted block is parseable again
    SourceDocument reparsed = doc(d.getText().toString());
    assertTrue(reparsed.getElements().stream()
                       .anyMatch(e -> e.isGuarded() && "tag".equals(e.getGuardedName())));
  }

  @Test
  public void testDeleteGuarded() throws SourceException {
    SourceDocument d = doc("class A {\n}\n");
    d.insertGuarded(region, null, "tag", "  generated", d.getElements().size(), null);
    assertTrue(d.deleteGuarded("tag") >= 0);
    assertFalse(d.getText().toString().contains("GEN-BEGIN:tag"));
    // deleting again finds nothing
    assertEquals(d.deleteGuarded("tag"), -1);
  }

  @Test
  public void testReplaceGuarded() throws SourceException {
    SourceDocument d = doc("class A {\n}\n");
    d.insertGuarded(region, null, "tag", "  original", d.getElements().size(), null);
    assertTrue(d.replaceGuarded(region, null, "tag", "  replacement", null) >= 0);
    assertTrue(d.getText().toString().contains("replacement"));
    assertFalse(d.getText().toString().contains("original"));
  }

  @Test
  public void testReplaceUnknownGuardedReturnsMinusOne() throws SourceException {
    SourceDocument d = doc("class A {\n}\n");
    assertEquals(d.replaceGuarded(region, null, "nope", "x", null), -1);
  }

  @Test(expectedExceptions = SourceException.class)
  public void testDuplicateGuardedThrows() throws SourceException {
    doc(guarded("dup", "a") + "\n" + guarded("dup", "b") + "\n");
  }

  @Test(expectedExceptions = SourceException.class)
  public void testOrphanStartThrows() throws SourceException {
    doc(region.createBegin(null, "orphan", null) + "\nsome code\n");
  }

  @Test(expectedExceptions = SourceException.class)
  public void testOrphanEndThrows() throws SourceException {
    doc(region.createEnd(null, "orphan", null) + "\n");
  }

  // --- inline replacement (previous block) ---

  @Test
  public void testReplaceInlinePreviousBlock() throws SourceException {
    SourceDocument d = doc("int x = /**/0/**/;\n// @wurblet <inl com.foo.Bar\n");
    // element 0 = code, element 1 = comment carrying the inline wurblet anchor
    SourceElement comment = d.getElements().get(1);
    SourceWurbletSubElement wurblet = comment.getWurblets().getFirst();
    assertTrue(wurblet.isInlinePreviousBlock());

    d.replaceInline("42", 1, wurblet);
    assertTrue(d.getText().toString().contains("/**/42/**/"),
               "expected inline code to be replaced, got: " + d.getText());
  }
}
