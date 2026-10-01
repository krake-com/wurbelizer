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

import org.wurbelizer.wurbel.WurbelException;
import org.wurbelizer.wurbel.Wurbler;

/**
 * A code generator.
 * Wurblets are executed by a container, the {@link Wurbler}.
 */
public interface Wurblet {

  /**
   * Sets the container.<br>
   * The wurbler attaches the wurblet before invoking the run-method
   * of the wurblet.
   *
   * @param container the wurbler the wurblet is attached to, null to detach
   */
  void setContainer(Wurbler container);

  /**
   * Gets the current container.<br>
   * Wurblets use this method to access the container's utility methods.
   *
   * @return the container, null if detached
   */
  Wurbler getContainer();

  /**
   * Invokes the wurblet from within the container.
   *
   * @throws WurbelException if something fails.
   */
  void run() throws WurbelException;

  /**
   * Processes the generated code.<br>
   * Used to filter, translate, etc... before being applied to the file.
   *
   * @param generatedCode the generated code
   * @return the processed generated code
   */
  String process(String generatedCode);

  /**
   * Performs any cleanup necessary after the last invocation of a wurblet.
   *
   * @throws WurbelException if cleanup failed
   */
  void cleanup() throws WurbelException;

  /**
   * Returns the phase in which the wurblet runs.<br>
   * The first and default phase is 1.<br>
   *
   * @return the phase
   */
  int getPhase();

  /**
   * Sets the execution phase.
   *
   * @param phase the phase &ge; 1
   */
  void setPhase(int phase);

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
   * Returns whether this wurblet is thread-safe.<br>
   * Build tools may wurbel several sources concurrently, for example, the Maven plugin in parallel builds.
   * Only thread-safe wurblets are executed concurrently. All others are executed sequentially,
   * i.e., no two non-thread-safe wurblets run at the same time within the same JVM.
   * <p>
   * A wurblet is thread-safe if it doesn't share any mutable state with other wurblet instances,
   * for example, in static fields or in non-thread-safe libraries.
   *
   * @return true if thread-safe, false if it must be executed sequentially
   */
  boolean isThreadSafe();

}
