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

package org.wurbelizer.wurblet;

import org.wurbelizer.wurbel.JavaSourceType;
import org.wurbelizer.wurbel.WurbelException;
import org.wurbelizer.wurbel.Wurbler;

import java.util.HashSet;
import java.util.Set;

/**
 * Base class for java wurblets.<br>
 * Extends {@link AbstractWurblet} for wurblets embedded in java classes or interfaces.
 */
public abstract class AbstractJavaWurblet extends AbstractWurblet {

  private Set<String> modifiers;    // != null if java-modifiers processed

  /**
   * Gets the classname (from container).<br>
   * This is the baseclassname (not containing the package!)
   * @return the class basename
   * @throws WurbelException if classname could not be determined
   */
  public String getClassName() throws WurbelException {
    String className = getContainer().getProperty(Wurbler.PROPSPACE_WURBLET, JavaSourceType.CLASS_NAME);
    if (className == null) {
      throw new WurbelException("can't determine classname");
    }
    return className;
  }

  /**
   * Gets the super-classname (from container).<br>
   *
   * @return the super class
   * @throws WurbelException if classname could not be determined
   */
  public String getSuperClassName() throws WurbelException {
    String superClassName = getContainer().getProperty(Wurbler.PROPSPACE_WURBLET, JavaSourceType.EXTENDS);
    if (superClassName == null) {
      // try interface: they are numbered starting at 0
      superClassName = getContainer().getProperty(Wurbler.PROPSPACE_WURBLET, JavaSourceType.EXTENDS + "0");
    }
    if (superClassName == null) {
      throw new WurbelException("can't determine super-classname");
    }
    return superClassName;
  }

  /**
   * Gets the packagename (from container).
   * @return the package name
   * @throws WurbelException if packagename could not be determined
   */
  public String getPackageName() throws WurbelException {
    String packageName = getContainer().getProperty(Wurbler.PROPSPACE_WURBLET, JavaSourceType.PACKAGE_NAME);
    if (packageName == null) {
      throw new WurbelException("can't determine packagename");
    }
    return packageName;
  }

  /**
   * Returns whether the java file is an interface.
   *
   * @return true if interface
   */
  public boolean isInterface() {
    return getContainer().getProperty(Wurbler.PROPSPACE_WURBLET, JavaSourceType.INTERFACE_NAME) != null;
  }

  /**
   * Returns whether the java file is a record.
   *
   * @return true if record
   */
  public boolean isRecord() {
    return getContainer().getProperty(Wurbler.PROPSPACE_WURBLET, JavaSourceType.RECORD_NAME) != null;
  }

  /**
   * Returns whether the java file is a real class and not an interface or a record.
   *
   * @return true if real class
   */
  public boolean isClass() {
    return getContainer().getProperty(Wurbler.PROPSPACE_WURBLET, JavaSourceType.CLASS_NAME) != null &&
           !isRecord() && !isInterface();
  }

  /**
   * Returns whether the abstract modifier is set.
   *
   * @return true if abstract class
   */
  public boolean isAbstract() {
    return isModifierPresent("abstract");
  }

  /**
   * Returns whether the final modifier is set.
   *
   * @return true if final class
   */
  public boolean isFinal() {
    return isModifierPresent("final");
  }

  /**
   * Returns whether the private modifier is set.
   *
   * @return true if private class
   */
  public boolean isPrivate() {
    return isModifierPresent("private");
  }

  /**
   * Returns whether the protected modifier is set.
   *
   * @return true if protected class
   */
  public boolean isProtected() {
    return isModifierPresent("protected");
  }

  /**
   * Returns whether the public modifier is set.
   *
   * @return true if public class
   */
  public boolean isPublic() {
    return isModifierPresent("public");
  }


  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder();
    buf.append(super.toString());
    try {
      String className = getClassName();
      buf.append(" in ").append(className);
    }
    catch (WurbelException wex) {
      // ignore
    }
    return buf.toString();
  }


  private boolean isModifierPresent(String modifier) {
    if (modifiers == null) {
      modifiers = new HashSet<>();
      String m;
      int i = 0;
      while ((m = getContainer().getProperty(Wurbler.PROPSPACE_WURBLET, JavaSourceType.MODIFIERS + i++)) != null) {
        modifiers.add(m);
      }
    }
    return modifiers.contains(modifier);
  }

}
