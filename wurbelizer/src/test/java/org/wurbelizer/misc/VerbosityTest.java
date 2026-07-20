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

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Tests the {@link Verbosity} level predicates.
 */
public class VerbosityTest {

  @Test
  public void testDefault() {
    Verbosity v = Verbosity.DEFAULT;
    assertTrue(v.isDefault());
    assertFalse(v.isInfo());
    assertFalse(v.isDebug());
  }

  @Test
  public void testInfo() {
    Verbosity v = Verbosity.INFO;
    assertFalse(v.isDefault());
    assertTrue(v.isInfo());
    assertFalse(v.isDebug());
  }

  @Test
  public void testDebug() {
    Verbosity v = Verbosity.DEBUG;
    assertFalse(v.isDefault());
    // debug implies info
    assertTrue(v.isInfo());
    assertTrue(v.isDebug());
  }
}
