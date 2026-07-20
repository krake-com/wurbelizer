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

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Properties;

public class ArgScannerTest {


  @Test
  public void testArgs() {
    Properties properties = createProperties();

    ArgScanner argScanner = new ArgScanner(
        "$alpha --orderby='\" ORDER BY \" + CN_ID + \" DESC\"' '$alpha ' \" $alpha\\\" \"", properties);
    Assert.assertEquals(argScanner.next(), "this is alpha");
    Assert.assertEquals(argScanner.next(), "--orderby=\" ORDER BY \" + CN_ID + \" DESC\"");
    Assert.assertEquals(argScanner.next(), "$alpha ");
    Assert.assertEquals(argScanner.next(), " this is alpha\" ");
    Assert.assertNull(argScanner.next());
  }

  @Test
  public void testNullInput() {
    Assert.assertNull(new ArgScanner(null, null).next());
  }

  @Test
  public void testEmptyAndBlankInput() {
    Assert.assertNull(new ArgScanner("", null).next());
    Assert.assertNull(new ArgScanner("   \t  ", null).next());
  }

  @Test
  public void testMultipleSimpleArgs() {
    ArgScanner scanner = new ArgScanner("  one   two\tthree ", null);
    Assert.assertEquals(scanner.next(), "one");
    Assert.assertEquals(scanner.next(), "two");
    Assert.assertEquals(scanner.next(), "three");
    Assert.assertNull(scanner.next());
  }

  @Test
  public void testSingleQuoteSuppressesTranslation() {
    // single quotes: no variable translation takes place
    ArgScanner scanner = new ArgScanner("'$alpha'", createProperties());
    Assert.assertEquals(scanner.next(), "$alpha");
    Assert.assertNull(scanner.next());
  }

  @Test
  public void testDoubleQuoteTranslates() {
    // double quotes: variables are translated
    ArgScanner scanner = new ArgScanner("\"$alpha\"", createProperties());
    Assert.assertEquals(scanner.next(), "this is alpha");
    Assert.assertNull(scanner.next());
  }

  @Test
  public void testBackslashEscapedWhitespaceKeepsSingleArg() {
    // an escaped space does not separate arguments
    ArgScanner scanner = new ArgScanner("a\\ b", null);
    Assert.assertEquals(scanner.next(), "a b");
    Assert.assertNull(scanner.next());
  }

  private Properties createProperties() {
    Properties properties = new Properties();
    properties.setProperty("alpha", "this is alpha");
    return properties;
  }
}
