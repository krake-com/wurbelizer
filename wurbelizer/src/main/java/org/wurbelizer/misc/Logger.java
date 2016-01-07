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


package org.wurbelizer.misc;

/**
 * An implementation independent logger.<br>
 * The wurbelizer always uses the logger of the container,
 * for example Ant or Maven.
 *
 * @author harald
 * @version $Revision: 97 $
 */
public interface Logger {

  /**
   * Checks if logger logs level DEBUG.
   *
   * @return true if logger will log this level
   */
  boolean isDebugLoggable();

  /**
   * Checks if logger logs level INFO.
   *
   * @return true if logger will log this level
   */
  boolean isInfoLoggable();

  /**
   * Checks if logger logs level WARNING.
   *
   * @return true if logger will log this level
   */
  boolean isWarningLoggable();

  /**
   * Checks if logger logs level ERROR.
   *
   * @return true if logger will log this level
   */
  boolean isErrorLoggable();


  /**
   * Logs a message with level DEBUG.
   *
   * @param message the message
   */
  void debug(String message);

  /**
   * Logs a message with level INFO.
   *
   * @param message the message
   */
  void info(String message);

  /**
   * Logs a message with level WARNING.
   *
   * @param message the message
   */
  void warning(String message);

  /**
   * Logs a message with level ERROR.
   *
   * @param message the message
   */
  void error(String message);


  /**
   * Logs a message with level DEBUG.
   *
   * @param message the message
   * @param t exception thrown
   */
  void debug(String message, Throwable t);

  /**
   * Logs a message with level INFO.
   *
   * @param message the message
   * @param t exception thrown
   */
  void info(String message, Throwable t);

  /**
   * Logs a message with level WARNING.
   *
   * @param message the message
   * @param t exception thrown
   */
  void warning(String message, Throwable t);

  /**
   * Logs a message with level ERROR.
   *
   * @param message the message
   * @param t exception thrown
   */
  void error(String message, Throwable t);

}
