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

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.apache.tools.ant.Project;

import static org.apache.tools.ant.Project.MSG_DEBUG;
import static org.apache.tools.ant.Project.MSG_ERR;
import static org.apache.tools.ant.Project.MSG_INFO;
import static org.apache.tools.ant.Project.MSG_WARN;

/**
 * Ant logger.<br>
 * Used if the wurbelizer is invoked from within Ant.
 *
 * @author harald
 */
public class AntLogger implements Logger {

  private final Project project;    // the Ant project


  /**
   * Creates an Ant logger.
   *
   * @param project the Ant project
   */
  public AntLogger(Project project) {
    super();
    this.project = project;
  }

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
    project.log(message, MSG_DEBUG);
  }

  @Override
  public void info(String message) {
    project.log(message, MSG_INFO);
  }

  @Override
  public void warning(String message) {
    project.log(message, MSG_WARN);
  }

  @Override
  public void error(String message) {
    project.log(message, MSG_ERR);
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
