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

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

/**
 * Tests the {@link SourceRegion} implementations and the SPI lookup.
 */
public class SourceRegionTest {

  @DataProvider(name = "regions")
  public Object[][] regions() {
    return new Object[][] {
        {new NetbeansRegion()},
        {new CoffeeBytesRegion()},
        {new VisualStudioRegion()}
    };
  }

  @Test(dataProvider = "regions")
  public void testBeginRoundTrip(SourceRegion region) {
    String begin = region.createBegin(null, "myGuard", null);
    assertEquals(region.getBeginName(begin), "myGuard");
  }

  @Test(dataProvider = "regions")
  public void testEndRoundTrip(SourceRegion region) {
    String end = region.createEnd(null, "myGuard", null);
    assertEquals(region.getEndName(end), "myGuard");
    assertTrue(region.isEnd(end, "myGuard"));
  }

  @Test(dataProvider = "regions")
  public void testBeginIsNotEnd(SourceRegion region) {
    String begin = region.createBegin(null, "myGuard", null);
    assertNull(region.getEndName(begin));
    assertFalse(region.isEnd(begin, "myGuard"));
  }

  @Test(dataProvider = "regions")
  public void testEndIsNotBegin(SourceRegion region) {
    String end = region.createEnd(null, "myGuard", null);
    assertNull(region.getBeginName(end));
  }

  @Test(dataProvider = "regions")
  public void testIsEndOnlyForMatchingName(SourceRegion region) {
    String end = region.createEnd(null, "myGuard", null);
    assertFalse(region.isEnd(end, "otherGuard"));
  }

  @Test(dataProvider = "regions")
  public void testUnrelatedCommentHasNoNames(SourceRegion region) {
    String comment = "// just an ordinary comment";
    assertNull(region.getBeginName(comment));
    assertNull(region.getEndName(comment));
  }

  @Test(dataProvider = "regions")
  public void testToStringIsType(SourceRegion region) {
    assertEquals(region.toString(), region.type());
  }

  @Test
  public void testNetbeansFoldTypes() {
    NetbeansRegion region = new NetbeansRegion();
    assertTrue(region.isDefault());

    String collapsed = region.createBegin(SourceRegion.FoldType.COLLAPSED, "g", null);
    assertTrue(collapsed.contains("collapsed"));
    assertEquals(region.getBeginName(collapsed), "g");

    String expanded = region.createBegin(SourceRegion.FoldType.EXPANDED, "g", null);
    assertTrue(expanded.contains("expanded"));
    assertEquals(region.getBeginName(expanded), "g");
  }

  @Test
  public void testNonDefaultRegions() {
    assertFalse(new CoffeeBytesRegion().isDefault());
    assertFalse(new VisualStudioRegion().isDefault());
  }

  @Test
  public void testQuotedMarkerIsNotTreatedAsRegion() {
    // a marker mentioned as a quoted region name (preceded by a single quote) must not match
    NetbeansRegion region = new NetbeansRegion();
    assertNull(region.getEndName("// code '</editor-fold>' referenced by name"));
  }

  // --- SPI lookup via SourceRegion.get / getAll ---

  @Test
  public void testGetByType() throws WurbelException {
    assertEquals(SourceRegion.get("netbeans").type(), "netbeans");
    assertEquals(SourceRegion.get("coffeebytes").type(), "coffeebytes");
  }

  @Test
  public void testGetDefault() throws WurbelException {
    assertEquals(SourceRegion.get(null).type(), "netbeans");
  }

  @Test(expectedExceptions = WurbelException.class)
  public void testGetUnknownTypeThrows() throws WurbelException {
    SourceRegion.get("nosuchregion");
  }

  @Test
  public void testGetAllContainsThree() {
    assertTrue(SourceRegion.getAll().size() >= 3);
  }
}
