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

import java.util.Arrays;
import java.util.Properties;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Tests {@link SourceElement} sub-element scanning (wurblet anchors,
 * here-documents, and property-documents) via {@link SourceDocument}.
 */
public class SourceElementTest {

  private SourceDocument doc(String text) throws SourceException {
    return doc(text, new Properties());
  }

  private SourceDocument doc(String text, Properties props) throws SourceException {
    return new SourceDocument(text, "Test.java", props, new DefaultLogger(), Verbosity.DEFAULT);
  }

  // --- wurblet anchors ---

  @Test
  public void testWurbletAnchor() throws SourceException {
    SourceDocument d = doc("// @wurblet mytag com.foo.Bar arg1 arg2\nint x;");
    SourceElement elem = d.getElements().getFirst();
    assertTrue(elem.containsWurblets());
    SourceWurbletSubElement wurblet = elem.getWurblets().getFirst();
    assertEquals(wurblet.getName(), "mytag");
    assertEquals(wurblet.getWurbletName(), "com.foo.Bar");
    assertEquals(Arrays.asList(wurblet.getWurbletArgs()), Arrays.asList("arg1", "arg2"));
  }

  @Test
  public void testWurbletWurblerArgs() throws SourceException {
    SourceDocument d = doc("// @wurblet(indent=2) mytag com.foo.Bar\nint x;");
    SourceWurbletSubElement wurblet = d.getElements().getFirst().getWurblets().getFirst();
    assertEquals(Arrays.asList(wurblet.getWurblerArgs()), Arrays.asList("indent=2"));
    assertEquals(wurblet.getName(), "mytag");
    assertEquals(wurblet.getWurbletName(), "com.foo.Bar");
  }

  @Test
  public void testWurbletInlineClassification() throws SourceException {
    SourceWurbletSubElement next = doc("// @wurblet >tag com.foo.Bar\nx;")
                                     .getElements().getFirst().getWurblets().getFirst();
    assertTrue(next.isInlineNextBlock());
    assertTrue(next.isInline());
    assertFalse(next.isInlinePreviousBlock());

    SourceWurbletSubElement prev = doc("x;\n// @wurblet -tag com.foo.Bar\n")
        .getElements().get(1).getWurblets().getFirst();
    assertTrue(prev.isInlinePreviousBlock());
    assertTrue(prev.isInline());

    SourceWurbletSubElement normal = doc("// @wurblet tag com.foo.Bar\nx;")
                                       .getElements().getFirst().getWurblets().getFirst();
    assertFalse(normal.isInline());
  }

  @Test
  public void testEscapedWurbletIgnored() throws SourceException {
    // "\@wurblet" is escaped and must not be recognized as an anchor
    SourceDocument d = doc("// \\@wurblet mytag com.foo.Bar\nint x;");
    assertFalse(d.getElements().getFirst().containsWurblets());
  }

  @Test(expectedExceptions = SourceException.class)
  public void testMissingWurbletNameThrows() throws SourceException {
    doc("// @wurblet onlytag\nint x;");
  }

  // --- here-documents ---

  @Test
  public void testHereDocument() throws SourceException {
    SourceDocument d = doc("/*\n@> .myheap\nhello world\n@<\n*/");
    SourceElement elem = d.getElements().getFirst();
    assertTrue(elem.containsHereDocuments());
    SourceHereSubElement here = elem.getHereDocuments().getFirst();
    assertEquals(here.getName(), ".myheap");
    assertTrue(here.getText().contains("hello world"));
    // by default, an origin info line is prepended
    assertTrue(here.getText().startsWith("#@"));
  }

  @Test
  public void testHereDocumentNoSource() throws SourceException {
    SourceDocument d = doc("/*\n@> .myheap --nosource\nhello world\n@<\n*/");
    SourceHereSubElement here = d.getElements().getFirst().getHereDocuments().getFirst();
    assertEquals(here.getText(), "hello world");
  }

  @Test(expectedExceptions = SourceException.class)
  public void testHereDocumentMissingEndThrows() throws SourceException {
    doc("/*\n@> .myheap\nhello world\n*/");
  }

  // --- property-documents ---

  @Test
  public void testPropertyDocument() throws SourceException {
    Properties props = new Properties();
    doc("/*\n@{\nkey1 = value1\nkey2 = value2\n@}\n*/", props);
    assertEquals(props.getProperty("key1"), "value1");
    assertEquals(props.getProperty("key2"), "value2");
  }

  // --- misc element helpers ---

  @Test
  public void testIsWhiteEmpty() throws SourceException {
    assertTrue(doc("   \n  ").getElements().getFirst().isWhiteEmpty());
    assertFalse(doc("code").getElements().getFirst().isWhiteEmpty());
  }

  @Test
  public void testGetFirstLineLead() throws SourceException {
    SourceDocument d = doc("abc/* comment */");
    SourceElement comment = d.getElements().get(1);
    assertEquals(comment.getType(), SourceElement.Type.BLOCK_COMMENT);
    assertEquals(comment.getFirstLineLead(), "abc");
  }

  @Test
  public void testGetText() throws SourceException {
    SourceDocument d = doc("int x;/* c */");
    assertEquals(d.getElements().get(1).getText(), "/* c */");
  }
}
