/*
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

/**
 * Result from wurbler.<br>
 * For a single file.
 *
 * @author harald
 */
public class WurbelResult {

  /**
   * tells if and why file is updated.
   */
  public enum UpdateType {
    /**
     * file was not updated and no errors.
     */
    UNCHANGED,
    /**
     * file was updated without errors.
     */
    UPDATED,
    /**
     * heredocs in file found and written to disk, file is not updated.
     */
    HEREDOCS
  }

  private final int wurbletCount;
  private final int errors;
  private final UpdateType updateType;

  /**
   * Creates a wurbel result.
   *
   * @param wurbletCount the number of executed wurbletCount
   * @param errors the number of errors
   * @param updateType the kind of update of the source file
   */
  public WurbelResult(int wurbletCount, int errors, UpdateType updateType) {
    this.wurbletCount = wurbletCount;
    this.errors = errors;
    this.updateType = updateType;
  }

  /**
   * Gets the number of executed wurbletCount.
   *
   * @return the wurblet count
   */
  public int getWurbletCount() {
    return wurbletCount;
  }

  /**
   * Gets the number of errors.
   *
   * @return the error count
   */
  public int getErrors() {
    return errors;
  }

  /**
   * Gets the kind of update to the source file.
   *
   * @return the update type
   */
  public UpdateType getUpdateType() {
    return updateType;
  }

}
