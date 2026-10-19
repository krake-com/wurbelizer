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

import org.wurbelizer.misc.Logger;
import org.wurbelizer.misc.Verbosity;

import java.io.File;
import java.io.PrintStream;
import java.util.Properties;

/**
 * The wurblet container.
 */
public interface Wurbler {

  /**
   * File extension for the wurblet property file going with a source file.
   */
  String WURBLET_PROPERTIES_EXTENSION = ".wurb";

  /**
   * Namespace for the runtime environment.
   */
  String PROPSPACE_ENV = "env";

  /**
   * Namespace for the wurblet.
   */
  String PROPSPACE_WURBLET = "wurblet";

  /**
   * Extra namespace.<br>
   * Used for the wurbletProperties in maven poms and the properties of an ant project.
   */
  String PROPSPACE_EXTRA = "extra";


  /**
   * property in wurblet namespace holding the absolute pathname of the source file.
   */
  String WURBLET_FILENAME = "filename";

  /**
   * property in wurblet namespace holding the contents of the source file.
   */
  String WURBLET_FILECONTENTS = "filecontents";

  /**
   * property in wurblet namespace holding the pathname of the *.wurb-file (additional properties), null if none.
   */
  String WURBLET_WURBNAME = "wurbname";

  /**
   * property in wurblet namespace holding the directory of the source file.
   */
  String WURBLET_DIRNAME = "dirname";

  /**
   * property in wurblet namespace holding the guardname of the wurblet anchor.
   * (wurblet tag)
   */
  String WURBLET_GUARDNAME = "guardname";

  /**
   * property in wurblet namespace holding the name of the wurblet.
   */
  String WURBLET_WURBLETNAME = "wurbletname";


  /**
   * Processes the source file, create here-docs, and replace wurblet-guarded sections.
   *
   * @return the wurbel result
   *
   * @throws WurbelException if a file- or heapfile operation failed
   * @throws SourceException if source scanning failed
   */
  WurbelResult wurbel() throws WurbelException, SourceException;

  /**
   * Gets the fixed source texts.
   *
   * @return the source array
   */
  String[] getSource();

  /**
   * Gets the writer where to write generated code to.
   *
   * @return the open writer
   */
  PrintStream getPrintStream();

  /**
   * Gets the wurblet args.
   *
   * @return array of strings representing args for the wurblet
   */
  String[] getArgs();

  /**
   * Gets the invocation count.<br>
   * Wurblets are invoked as long as the data changes.
   * Some wurblets need to know how often (e.g. to allocate HeapFiles only once).
   *
   * @return the invocation count, starting at 1
   */
  int getInvocationCount();

  /**
   * Gets the property value for a given namespace and key.
   *
   * @param nameSpace the namespace, null for the default java runtime properties
   * @param key the property's key
   *
   * @return the property's value, null if no such property
   */
  String getProperty(String nameSpace, String key);

  /**
   * Gets all Properties within a namespace.
   *
   * @param nameSpace the namespace, null for the default java runtime properties
   *
   * @return the properties, null if no such namespace
   */
  Properties getProperties(String nameSpace);

  /**
   * Gets an analyze-information file.<br>
   * Some wurblets need extra information which is provided in files
   * in the so-called analyze-directory (Maven parameter {@code analyzeDir}, Ant attribute {@code analyzedir}).
   *
   * @param name the filename relative to the analyze-directory
   *
   * @return the file
   */
  File getAnalyzeFile(String name);

  /**
   * Gets the verbosity level.<br>
   * @return the verbosity
   */
  Verbosity getVerbosity();

  /**
   * Gets the logger.
   *
   * @return the logger
   */
  Logger getLogger();

}
