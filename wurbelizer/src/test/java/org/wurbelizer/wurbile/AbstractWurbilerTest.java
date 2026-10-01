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
import org.wurbelizer.misc.Verbosity;

import java.io.BufferedReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

/**
 * Tests escape handling in {@link AbstractWurbiler}.
 */
public class AbstractWurbilerTest {

  private static class Result {
    final String java;
    final List<String> source;
    Result(String java, List<String> source) {
      this.java = java;
      this.source = source;
    }
  }

  private static Result compile(String wrbl) throws Exception {
    StringWriter out = new StringWriter();
    List<String> source = new ArrayList<>();
    AbstractWurbiler wurbiler = createWurbiler(wrbl, out, source);
    assertEquals(wurbiler.compile(), 0);
    return new Result(out.toString(), source);
  }

  private static AbstractWurbiler createWurbiler(String wrbl, StringWriter out, List<String> source) {
    AbstractWurbiler wurbiler = new AbstractWurbiler(
        new BufferedReader(new StringReader(wrbl)), "test", out, source) {};
    wurbiler.setVerbosity(Verbosity.DEFAULT);
    return wurbiler;
  }

  @Test
  public void testEscapedBlankInOutputLevel() throws Exception {
    assertEquals(compile("a\\ b").source, List.of("a b\n"));
  }

  @Test
  public void testEscapedBlankInWurbletBlock() throws Exception {
    assertTrue(compile("@[int\\ x = 1;]@").java.contains("int x = 1;"));
  }

  @Test
  public void testEscapedBlankInWurbletExpression() throws Exception {
    assertTrue(compile("@(a +\\ b)@").java.contains(".print(a + b);"));
  }

  @Test
  public void testEscapedAt() throws Exception {
    assertEquals(compile("\\@Override").source, List.of("@Override\n"));
  }

  @Test
  public void testArgsDirectiveKeepsAllArguments() throws Exception {
    AbstractWurbiler wurbiler = createWurbiler("@{args a b c}@", new StringWriter(), new ArrayList<>());
    assertEquals(wurbiler.compile(), 0);
    assertEquals(wurbiler.getArgs(), new String[] { "a", "b", "c" });
  }

  @Test
  public void testEmptyArgsDirectiveResetsArguments() throws Exception {
    AbstractWurbiler wurbiler = createWurbiler("@{args a b}@@{args}@", new StringWriter(), new ArrayList<>());
    assertEquals(wurbiler.compile(), 0);
    assertNull(wurbiler.getArgs());
  }

  @Test
  public void testUnescapedAtInDirectiveStaysInDirective() throws Exception {
    StringWriter out = new StringWriter();
    AbstractWurbiler wurbiler = createWurbiler(
        "@{code\n  @Override\n  public String toString() { return \"x\"; }\n}@", out, new ArrayList<>());
    assertEquals(wurbiler.compile(), 0);
    assertTrue(wurbiler.getExtraCode().contains("@Override"), wurbiler.getExtraCode());
    assertFalse(out.toString().contains("@O"), out.toString());
  }

  @Test
  public void testUnescapedAtInExpressionStaysInExpression() throws Exception {
    assertTrue(compile("@(\"@x\")@").java.contains(".print(\"@x\");"));
  }
}
