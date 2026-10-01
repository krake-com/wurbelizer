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

import java.io.Serial;

/**
 * Same as {@link WurbelException} but instructs the wurbler to discard the generated source.<br>
 * The error is logged as a warning and is not counted as an error. The existing guarded block,
 * if any, is left unchanged.<br>
 * Useful if a wurblet fails, but it's smarter to leave the existing code unchanged and continue
 * with the build process, for example, because the real cause has already been reported as an error elsewhere.
 *
 * @author harald
 * @see WurbelException
 * @see WurbelTerminationException
 */
public class WurbelDiscardException extends WurbelException {

  @Serial
  private static final long serialVersionUID = 1L;

  /**
   * Creates a wurbel exception.
   *
   * @param message the error message
   * @param sourceInfoIncluded true if message contains a hint to the source of the error, false if default to file
   * containing the wurblet
   * @param cause the cause
   */
  public WurbelDiscardException(String message, boolean sourceInfoIncluded, Throwable cause) {
    super(message, sourceInfoIncluded, cause);
  }

  /**
   * Creates a wurbel exception.
   *
   * @param message the error message
   * @param sourceInfoIncluded true if message contains a hint to the source of the error, false if default to file
   * containing the wurblet
   */
  public WurbelDiscardException(String message, boolean sourceInfoIncluded) {
    super(message, sourceInfoIncluded);
  }

  /**
   * Creates a wurbel exception.
   *
   * @param message the error message
   * @param cause the cause
   */
  public WurbelDiscardException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Creates a wurbel exception.
   *
   * @param message the error message
   */
  public WurbelDiscardException(String message) {
    super(message);
  }

}
