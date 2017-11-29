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


package org.wurbelizer.wurbile;

import java.util.List;
import org.wurbelizer.misc.Logger;
import org.wurbelizer.misc.Verbosity;


/**
 * The wurblet compiler.
 */

public interface Wurbiler {

  /**
   * Compiles the wurbelizable.
   *
   * @return number of errors, 0 = no errors (warnings are not counted)
   *
   * @throws WurbileException if compilation failed due to i/o error
   */
  int compile() throws WurbileException;


  /**
   * Gets the list of invariant wurblet strings of the output level.
   *
   * @return a list of strings
   */
  List<String> getSourceList();

  /**
   * Presets the list of invariant wurblet strings of the output level.
   *
   * @param sourceList the source list, null if compile() should initialize it
   */
  void setSourceList(List<String> sourceList);


  /**
   * Gets the source indentation.
   *
   * @return the current indentation
   */
  int getIndent();

  /**
   * Sets the indentation for code generating the source.
   *
   * @param indent the new indentation
   */
  void setIndent(int indent);


  /**
   * Sets the autoindent feature.
   *
   * @param autoIndent true to enable autoindent (default)
   */
  void setAutoIndent(boolean autoIndent);

  /**
   * Gets the autoindent feature.
   *
   * @return true if autoindent is enabled (default)
   */
  boolean isAutoIndent();


  /**
   * Sets the name of the output stream in the generated source.
   *
   * @param outName null sets to the default value "out".
   */
  void setOutName(String outName);

  /**
   * Sets name of the output stream to the default value (out).
   */
  void setOutName();

  /**
   * Gets the name of the output stream
   *
   * @return the name of the output stream
   */
  String getOutName();


  /**
   * Gets the logger for warnings and errors.
   *
   * @return Value of property logger.
   */
  Logger getLogger();

  /**
   * Sets the logger for warnings and errors.
   *
   * @param logger New value of property logger.
   */
  void setLogger(Logger logger);


  /**
   * Gets the verbosity.
   * @return the verbosity
   */
  Verbosity getVerbosity();

  /**
   * Sets the verbosity.
   * @param verbosity the verbosity
   */
  void setVerbosity(Verbosity verbosity);


  /**
   * Gets the package name.
   *
   * @return the package name, null if default package
   */
  String getPackageName();

  /**
   * Sets the package name.
   *
   * @param packageName the package name
   */
  void setPackageName(String packageName);


  /**
   * Gets the parent class.
   *
   * @return the name of the parent class, null if default
   */
  String getParentClass();

  /**
   * Sets the parent class.
   *
   * @param parentClass the name of the parent class, null if default (AbstractWurblet)
   */
  void setParentClass(String parentClass);


  /**
   * Gets the list of imports.
   *
   * @return array of imports
   */
  String[] getImports();

  /**
   * Sets the imports.
   *
   * @param imports is the array of imports
   */
  void setImports(String[] imports);


  /**
   * Gets the preset wurblet args.
   *
   * @return wurblet args, null = default
   */
  String[] getArgs();

  /**
   * Preset wurblet args.
   * The default (null) means that the args
   * are determined at runtime by the wurbler,
   * i.e. command-line, &#64;wurblet-directive.
   *
   * @param args the wurblet args
   */
  void setArgs(String[] args);


  /**
   * Gets the wurblet comment.
   *
   * @return the comment, null if none
   */
  String getComment();

  /**
   * Sets the wurblet comment.
   *
   * @param comment the comment, null if none
   */
  void setComment(String comment);


  /**
   * Gets the wurblet specific configuration.
   *
   * @return the config, null if none
   */
  String getConfiguration();

  /**
   * Sets the wurblet specific configuration.
   *
   * @param config the config, null if none
   */
  void setConfiguration(String config);


  /**
   * Gets the execution phase of the compiled wurblet.
   *
   * @return the execution phase, null if default
   */
  Integer getPhase();

  /**
   * Sets the execution phase of the compiled wurblet.
   *
   * @param phase the execution phase, null or &lt; 0 if default
   */
  void setPhase(Integer phase);

}
