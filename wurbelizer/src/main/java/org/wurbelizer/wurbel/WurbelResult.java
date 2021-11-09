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

/**
 * Result from wurbler.<br>
 * For a single file.
 *
 * @param wurbletCount the number of executed wurbletCount
 * @param errors       the number of errors
 * @param updateType   the kind of update of the source file
 */
public record WurbelResult(int wurbletCount, int errors, UpdateType updateType) {

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

}
