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
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.assertEquals;
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
    AbstractWurbiler wurbiler = new AbstractWurbiler(
        new BufferedReader(new StringReader(wrbl)), "test", out, source) {};
    assertEquals(wurbiler.compile(), 0);
    return new Result(out.toString(), source);
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
}
