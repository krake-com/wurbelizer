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


package org.wurbelizer.misc;

import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.testng.Assert.assertTrue;

/**
 * Tests the {@link DefaultLogger}.
 */
public class DefaultLoggerTest {

  @Test
  public void testStackTraceIsLogged() {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    PrintStream oldOut = System.out;
    System.setOut(new PrintStream(bos, true, StandardCharsets.UTF_8));
    try {
      new DefaultLogger().error("failed", new RuntimeException("boom"));
    }
    finally {
      System.setOut(oldOut);
    }
    String output = bos.toString(StandardCharsets.UTF_8);
    assertTrue(output.contains("[ERROR] failed"), output);
    assertTrue(output.contains("java.lang.RuntimeException: boom"), output);
    assertTrue(output.contains("at org.wurbelizer.misc.DefaultLoggerTest"), output);
  }

}
