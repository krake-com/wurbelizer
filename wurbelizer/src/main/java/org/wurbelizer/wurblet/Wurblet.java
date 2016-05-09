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
   * Performs any cleanup necessary after last invocation of a wurblet.
   *
   * @throws WurbelException if cleanup failed
   */
  void cleanup() throws WurbelException;

}
