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

import java.util.Properties;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;


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

  @Test
  public void testPackageExtendsAndImplements() throws SourceException {
    SourceDocument document = createDocument(
        "package com.foo.bar;\n" +
        "public class MyClass extends Base implements IFace1, IFace2 {\n" +
        "}");
    Properties props = type.extractProperties(document);
    assertEquals(props.getProperty(JavaSourceType.PACKAGE_NAME), "com.foo.bar");
    assertEquals(props.getProperty(JavaSourceType.CLASS_NAME), "MyClass");
    assertEquals(props.get(JavaSourceType.MODIFIERS + 0), "public");
    assertEquals(props.getProperty(JavaSourceType.EXTENDS), "Base");
    assertEquals(props.getProperty(JavaSourceType.IMPLEMENTS + 0), "IFace1");
    assertEquals(props.getProperty(JavaSourceType.IMPLEMENTS + 1), "IFace2");
  }

  @Test
  public void testInterfaceMultipleExtends() throws SourceException {
    SourceDocument document = createDocument(
        "public interface MyIface extends A, B {\n" +
        "}");
    Properties props = type.extractProperties(document);
    assertEquals(props.getProperty(JavaSourceType.INTERFACE_NAME), "MyIface");
    assertEquals(props.getProperty(JavaSourceType.CLASS_NAME), "MyIface");
    assertEquals(props.getProperty(JavaSourceType.EXTENDS + 0), "A");
    assertEquals(props.getProperty(JavaSourceType.EXTENDS + 1), "B");
  }

  @Test
  public void testRecord() throws SourceException {
    SourceDocument document = createDocument(
        "public record Point(int x, int y) implements Serializable {\n" +
        "}");
    Properties props = type.extractProperties(document);
    assertEquals(props.getProperty(JavaSourceType.RECORD_NAME), "Point");
    assertEquals(props.getProperty(JavaSourceType.CLASS_NAME), "Point");
    assertEquals(props.getProperty(JavaSourceType.IMPLEMENTS + 0), "Serializable");
  }

  @Test
  public void testAnnotations() throws SourceException {
    SourceDocument document = createDocument(
        "@Entity\n" +
        "@Table(name=\"foo\")\n" +
        "public class Annotated {\n" +
        "}");
    Properties props = type.extractProperties(document);
    assertEquals(props.getProperty(JavaSourceType.CLASS_NAME), "Annotated");
    assertEquals(props.getProperty(JavaSourceType.ANNOTATION + 0), "@Entity");
    assertTrue(props.getProperty(JavaSourceType.ANNOTATION + 1).startsWith("@Table"));
  }

  @Test
  public void testFileExtensionAndSmartIndent() {
    assertEquals(type.getFileExtension(), ".java");
    assertTrue(type.isSmartIndent());
  }
}
