/*
 * Copyright (C) 2015 by:
 *
 * Hydro Aluminium Rolled Products GmbH
 * Koblenzer Strasse 122
 * D-41468 Neuss
 * Germany
 * Tel. +49 2131 382 0
 * http://www.hydro.com/Deutschland
 *
 * All Rights Reserved.
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
