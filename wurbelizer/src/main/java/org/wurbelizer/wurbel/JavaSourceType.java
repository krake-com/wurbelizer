/**
 * Wurbelizer - http://www.wurbelizer.org
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

import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.wurbelizer.misc.Constants;

/**
 * The Java source type.
 *
 * @author harald
 */
public class JavaSourceType implements SourceType {

  /**
   * property holding the java class-name of the source file (without package).<br>
   * The classname is set for both classes and interfaces.
   */
  public static final String CLASS_NAME = "classname";

  /**
   * property holding the java interface-name of the source file, if it is an interface (without package).<br>
   * For classes this property remains unset.
   */
  public static final String INTERFACE_NAME = "interfacename";

  /**
   * property holding the package name of the class
   */
  public static final String PACKAGE_NAME = "packagename";

  /**
   * property holding the java definition of the class or interface.<br>
   * This is the rest of the definition without the class or interface name.
   */
  public static final String DEFINITION = "definition";

  /**
   * property holding the extended class (or interfaces if this is an interface).<br>
   * The first class starts with 0, i.e. the application
   * has to loop on {@code getProperty(EXTENDS + ndx)}.
   * If it is a class {@code getProperty(EXTENDS)} is sufficient.
   */
  public static final String EXTENDS = "extends_";

  /**
   * property holding for the implementes interfaces.<br>
   * The first interface starts with 0, i.e. the application
   * has to loop on {@code getProperty(IMPLEMENTS + ndx)}.
   */
  public static final String IMPLEMENTS = "implements_";

  /**
   * property holding for the annotations for this class.
   * The first annotation starts with 0, i.e. the application
   * has to loop on {@code getProperty(ANNOTATION + ndx)}.
   */
  public static final String ANNOTATION = "annotation_";




  // some regexes
  private static final Pattern PACKAGE_PATTERN = Pattern.compile("^\\s*package\\s+([\\w\\.]+)\\s*;");
  private static final Pattern ANNOTATION_PATTERN = Pattern.compile("(@\\w+[^\\n\\r]*)");  // only 1-line annos for now
  // the following row patterns work because they are applied only on the first matching code block
  private static final Pattern CLASS_PATTERN = Pattern.compile("\\sclass\\s+(\\w+)");
  private static final Pattern INTERFACE_PATTERN = Pattern.compile("\\sinterface\\s+(\\w+)");
  // the following patterns work because they are applied to the block containing the CLASS or INTERFACE pattern
  private static final Pattern EXTENDS_PATTERN = Pattern.compile("\\s*(<.*>)*\\s*extends\\s+(\\w+((<.*>)*))");
  private static final Pattern IMPLEMENTS_PATTERN = Pattern.compile("\\s*(<.*>)*\\s*implements\\s+(\\w+((<.*>)*))");
  private static final Pattern MORE_PATTERN = Pattern.compile("\\s*,\\s*(\\w+((<.*>)*))");



  @Override
  public String getFileExtension() {
    return Constants.JAVA_SOURCE_EXTENSION;
  }

  @Override
  public Properties extractProperties(SourceDocument document) {
    Properties props = new Properties();
    boolean packageFound = false;

    for (SourceElement elem: document.getElements()) {
      if (elem.isCode()) {
        String code = elem.getText();

        if (!packageFound) {
          Matcher matcher = PACKAGE_PATTERN.matcher(code);
          if (matcher.find()) {
            props.setProperty(PACKAGE_NAME, matcher.group(1));
            packageFound = true;
          }
        }

        Matcher matcher = CLASS_PATTERN.matcher(code);
        if (matcher.find()) {
          props.setProperty(CLASS_NAME, matcher.group(1));
          extractAnnotations(props, code.substring(0, matcher.end()));
          code = code.substring(matcher.end());
          int ndx = code.lastIndexOf('{');
          if (ndx > 0) {
            code = code.substring(0, ndx);    // cut everything after the {
          }
          code = code.trim();
          props.setProperty(DEFINITION, code);
          extractExtends(props, code, false);
          extractImplements(props, code);
          break;
        }

        matcher = INTERFACE_PATTERN.matcher(code);
        if (matcher.find()) {
          String ifName = matcher.group(1);
          props.setProperty(INTERFACE_NAME, ifName);
          props.setProperty(CLASS_NAME, ifName);
          extractAnnotations(props, code.substring(0, matcher.end()));
          code = code.substring(matcher.end());
          int ndx = code.lastIndexOf('{');
          if (ndx > 0) {
            code = code.substring(0, ndx);    // cut everything after the {
          }
          code = code.trim();
          props.setProperty(DEFINITION, code);
          extractExtends(props, code, true);
          break;
        }

        // try annotations as long as we didn't caught the class or interface declaration
        extractAnnotations(props, code);
      }
    }
    return props;
  }


  private void extractAnnotations(Properties props, String code) {
    Matcher matcher = ANNOTATION_PATTERN.matcher(code);
    int ndx = 0;
    while (matcher.find()) {
      props.setProperty(ANNOTATION + (ndx++), matcher.group(1));
    }
  }

  /**
   * extract the superclass it this is a class
   * @param props
   * @param code
   */
  private void extractExtends(Properties props, String code, boolean isInterface) {
    Matcher matcher = EXTENDS_PATTERN.matcher(code);
    if (matcher.find()) {
      // first extends found
      if (isInterface) {
        // check for more (if this is an interface
        int ndx = 0;
        props.setProperty(EXTENDS + (ndx++), matcher.group(2));
        matcher = MORE_PATTERN.matcher(code.substring(matcher.end()));
        int end = 0;  // must immediately follow
        while (matcher.find() && end == matcher.start()) {
          props.setProperty(EXTENDS + (ndx++), matcher.group(1));
          end = matcher.end();
        }
      }
      else  {
         props.setProperty(EXTENDS, matcher.group(2));
      }
    }
  }

  /**
   * extract the interfaces this class implements
   * @param props
   * @param code
   */
  private void extractImplements(Properties props, String code) {
    Matcher matcher = IMPLEMENTS_PATTERN.matcher(code);
    if (matcher.find()) {
      // check for more (if this is an interface
      int ndx = 0;
      props.setProperty(IMPLEMENTS + (ndx++), matcher.group(2));
      matcher = MORE_PATTERN.matcher(code.substring(matcher.end()));
      int end = 0; // must immediately follow
      while (matcher.find() && end == matcher.start()) {
        props.setProperty(IMPLEMENTS + (ndx++), matcher.group(1));
        end = matcher.end();
      }
    }
  }

}
