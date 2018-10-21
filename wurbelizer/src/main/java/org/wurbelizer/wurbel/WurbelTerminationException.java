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
 * Same as {@link WurbelException} but terminates the build process immediately.<br>
 * Wurblets should throw this exception if the error is severe enough that it doesn't
 * make sense to execute any further wurblets, because they would fail as well.
 *
 * @author harald
 * @see WurbelException
 * @see WurbelDiscardException
 */
@SuppressWarnings("serial")
public class WurbelTerminationException extends WurbelException {

  /**
   * Creates a wurbel exception.
   *
   * @param message the error message
   * @param sourceInfoIncluded true if message contains a hint to the source of the error, false if default to file
   * containing the wurblet
   * @param cause the cause
   */
  public WurbelTerminationException(String message, boolean sourceInfoIncluded, Throwable cause) {
    super(message, sourceInfoIncluded, cause);
  }

  /**
   * Creates a wurbel exception.
   *
   * @param message the error message
   * @param sourceInfoIncluded true if message contains a hint to the source of the error, false if default to file
   * containing the wurblet
   */
  public WurbelTerminationException(String message, boolean sourceInfoIncluded) {
    super(message, sourceInfoIncluded);
  }

  /**
   * Creates a wurbel exception.
   *
   * @param message the error message
   * @param cause the cause
   */
  public WurbelTerminationException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Creates a wurbel exception.
   *
   * @param message the error message
   */
  public WurbelTerminationException(String message) {
    super(message);
  }

}
