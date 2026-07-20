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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Tests {@link WurbileHelper#toCommentBlock(String, int)}.
 */
public class WurbileHelperTest {

  @Test
  public void testNullComment() {
    assertEquals(WurbileHelper.toCommentBlock(null, 0), "");
  }

  @Test
  public void testEmptyComment() {
    assertEquals(WurbileHelper.toCommentBlock("", 4), "");
  }

  @Test
  public void testSingleLineNoIndent() {
    assertEquals(WurbileHelper.toCommentBlock("hello", 0),
                 "/**\n * hello\n */\n");
  }

  @Test
  public void testMultiLine() {
    // both \n and \r act as line separators
    assertEquals(WurbileHelper.toCommentBlock("first\nsecond", 0),
                 "/**\n * first\n * second\n */\n");
  }

  @Test
  public void testWithIndent() {
    String block = WurbileHelper.toCommentBlock("line", 2);
    assertEquals(block, "  /**\n   * line\n   */\n");
  }

  @Test
  public void testNegativeIndentTreatedAsZero() {
    // Math.max(0, indent) guards against negative indent
    assertEquals(WurbileHelper.toCommentBlock("x", -5),
                 "/**\n * x\n */\n");
  }

  @Test
  public void testStructure() {
    String block = WurbileHelper.toCommentBlock("a\nb\nc", 0);
    assertTrue(block.startsWith("/**"));
    assertTrue(block.endsWith(" */\n"));
  }
}
