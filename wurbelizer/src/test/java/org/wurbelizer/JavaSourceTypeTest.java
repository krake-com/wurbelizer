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

package org.wurbelizer;

import org.testng.annotations.Test;
import org.wurbelizer.misc.DefaultLogger;
import org.wurbelizer.misc.Verbosity;
import org.wurbelizer.wurbel.JavaSourceType;
import org.wurbelizer.wurbel.SourceDocument;
import org.wurbelizer.wurbel.SourceException;

import java.util.Properties;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;


public class JavaSourceTypeTest {

  private final JavaSourceType type = new JavaSourceType();

  private SourceDocument createDocument(String text) throws SourceException {
    return new SourceDocument(new StringBuilder(text), "test", new Properties(), new DefaultLogger(), Verbosity.DEFAULT);
  }

  @Test
  public void testJavaClass1() throws SourceException {
    SourceDocument document = createDocument("abstract   \tpublic class MyTest {" +
                                                 "}");
    Properties properties = type.extractProperties(document);
    assertEquals(properties.getProperty(JavaSourceType.CLASS_NAME), "MyTest");
    assertEquals(properties.get(JavaSourceType.MODIFIERS + 0), "abstract");
    assertEquals(properties.get(JavaSourceType.MODIFIERS + 1), "public");
    assertNull(properties.get(JavaSourceType.MODIFIERS + 2));
  }

  @Test
  public void testJavaClass2() throws SourceException {
    SourceDocument document = createDocument("interface MyTest {" +
                                                 "}");
    Properties properties = type.extractProperties(document);
    assertEquals(properties.getProperty(JavaSourceType.CLASS_NAME), "MyTest");
    assertNull(properties.get(JavaSourceType.MODIFIERS + 0));
  }
}
