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


package org.wurbelizer.wurbile;

/**
 * An exception thrown while compiling a wurblet source.
 *
 * @author harald
 */
@SuppressWarnings("serial")
public class WurbileException extends Exception {

  /**
   * Creates a wurbile exception.
   *
   * @param cause the cause
   */
  public WurbileException(Throwable cause) {
    super(cause);
  }

  /**
   * Creates a wurbile exception.
   *
   * @param message the message
   * @param cause the cause
   */
  public WurbileException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Creates a wurbile exception.
   *
   * @param message the message
   */
  public WurbileException(String message) {
    super(message);
  }

  /**
   * Creates a wurbile exception.
   */
  public WurbileException() {
  }


}
