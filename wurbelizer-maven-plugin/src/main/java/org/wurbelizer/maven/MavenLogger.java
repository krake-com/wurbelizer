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


package org.wurbelizer.maven;

import org.apache.maven.plugin.logging.Log;
import org.wurbelizer.misc.Logger;

/**
 * A simple default logger.
 * Prints to stdout.
 *
 * @author harald
 */
public class MavenLogger implements Logger {

  private Log log;

  public MavenLogger(Log log) {
    this.log = log;
  }

  @Override
  public boolean isDebugLoggable() {
    return log.isDebugEnabled();
  }

  @Override
  public boolean isInfoLoggable() {
    return log.isInfoEnabled();
  }

  @Override
  public boolean isWarningLoggable() {
    return log.isWarnEnabled();
  }

  @Override
  public boolean isErrorLoggable() {
    return log.isErrorEnabled();
  }

  @Override
  public void debug(String message) {
    log.debug(message);
  }

  @Override
  public void info(String message) {
    log.info(message);
  }

  @Override
  public void warning(String message) {
    log.warn(message);
  }

  @Override
  public void error(String message) {
    log.error(message);
  }

  @Override
  public void debug(String message, Throwable t) {
    log.debug(message, t);
  }

  @Override
  public void info(String message, Throwable t) {
    log.info(message, t);
  }

  @Override
  public void warning(String message, Throwable t) {
    log.warn(message, t);
  }

  @Override
  public void error(String message, Throwable t) {
    log.error(message, t);
  }

}
