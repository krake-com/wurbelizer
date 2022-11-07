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

package org.wurbelizer.misc;

/**
 * The verbosity level.
 *
 * @author harald
 */
public enum Verbosity {

  /**
   * default verbosity
   */
  DEFAULT,

  /**
   * info level
   */
  INFO,

  /**
   * debug level
   */
  DEBUG;


  /**
   * @return true if default logging
   */
  public boolean isDefault() {
    return this.equals(DEFAULT);
  }

  /**
   * @return true if at least info logging
   */
  public boolean isInfo() {
    return this.equals(INFO) || isDebug();
  }

  /**
   * @return true if debug logging
   */
  public boolean isDebug() {
    return this.equals(DEBUG);
  }

}
