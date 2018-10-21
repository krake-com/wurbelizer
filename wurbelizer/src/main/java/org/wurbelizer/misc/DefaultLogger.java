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

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/**
 * A simple default logger.<br>
 * Prints to stdout.
 *
 * @author harald
 */
public class DefaultLogger implements Logger {

  @Override
  public boolean isDebugLoggable() {
    return true;
  }

  @Override
  public boolean isInfoLoggable() {
    return true;
  }

  @Override
  public boolean isWarningLoggable() {
    return true;
  }

  @Override
  public boolean isErrorLoggable() {
    return true;
  }

  @Override
  public void debug(String message) {
    System.out.println("[DEBUG] " + message);
  }

  @Override
  public void info(String message) {
    System.out.println(message);
  }

  @Override
  public void warning(String message) {
    System.out.println("[WARNING] " + message);
  }

  @Override
  public void error(String message) {
    System.out.println("[ERROR] " + message);
  }

  @Override
  public void debug(String message, Throwable t) {
    debug(message + "\n" + getStackTraceAsString(t));
  }

  @Override
  public void info(String message, Throwable t) {
    info(message + "\n" + getStackTraceAsString(t));
  }

  @Override
  public void warning(String message, Throwable t) {
    warning(message + "\n" + getStackTraceAsString(t));
  }

  @Override
  public void error(String message, Throwable t) {
    error(message + "\n" + getStackTraceAsString(t));
  }


  private String getStackTraceAsString(Throwable cause) {
    try (PrintStream ps = new PrintStream(new ByteArrayOutputStream())) {
      cause.printStackTrace(ps);
      return ps.toString();
    }
  }
}
