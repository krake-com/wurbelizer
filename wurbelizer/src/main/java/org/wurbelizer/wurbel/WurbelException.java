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
 * An exception thrown while executing a {@link org.wurbelizer.wurblet.Wurblet}.<br>
 * A {@code WurbelException} is counted as a build error and usually terminates the
 * build process at the end of the corresponding build phase.
 * Subclasses are provided for a different behaviour.
 *
 * @author harald
 * @see WurbelTerminationException
 * @see WurbelDiscardException
 */
public class WurbelException extends Exception {

  @Serial
  private static final long serialVersionUID = 1L;

  /**
   * true if message contains a hint to the source of the error, false if default to file.
   */
  private final boolean sourceInfoIncluded;


  /**
   * Creates a wurbel exception.
   *
   * @param message the error message
   * @param sourceInfoIncluded true if message contains a hint to the source of the error, false if default to file
   * containing the wurblet
   * @param cause the cause
   */
  public WurbelException(String message, boolean sourceInfoIncluded, Throwable cause) {
    super(message, cause);
    this.sourceInfoIncluded = sourceInfoIncluded;
  }

  /**
   * Creates a wurbel exception.
   *
   * @param message the error message
   * @param sourceInfoIncluded true if message contains a hint to the source of the error, false if default to file
   * containing the wurblet
   */
  public WurbelException(String message, boolean sourceInfoIncluded) {
    super(message);
    this.sourceInfoIncluded = sourceInfoIncluded;
  }

  /**
   * Creates a wurbel exception.
   *
   * @param message the error message
   * @param cause the cause
   */
  public WurbelException(String message, Throwable cause) {
    this(message, false, cause);
  }

  /**
   * Creates a wurbel exception.
   *
   * @param message the error message
   */
  public WurbelException(String message) {
    this(message, false);
  }

  /**
   * Returns whether the exception message contains the source info.
   *
   * @return true if source info included
   */
  public boolean isSourceInfoIncluded() {
    return sourceInfoIncluded;
  }

}
