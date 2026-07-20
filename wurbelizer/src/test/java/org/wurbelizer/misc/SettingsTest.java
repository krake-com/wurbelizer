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

import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static org.testng.Assert.assertEquals;

/**
 * Tests the global {@link Settings}.
 */
public class SettingsTest {

  @AfterMethod
  public void restoreDefault() {
    // do not leak a changed charset into other tests
    Settings.setEncodingCharset(StandardCharsets.UTF_8);
  }

  @Test
  public void testDefaultIsUtf8() {
    assertEquals(Settings.getEncodingCharset(), StandardCharsets.UTF_8);
  }

  @Test
  public void testSetAndGet() {
    Charset latin1 = StandardCharsets.ISO_8859_1;
    Settings.setEncodingCharset(latin1);
    assertEquals(Settings.getEncodingCharset(), latin1);
  }
}
