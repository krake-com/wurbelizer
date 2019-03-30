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


package org.wurbelizer.wurblet;

import org.wurbelizer.wurbel.WurbelException;
import org.wurbelizer.wurbel.Wurbler;

import java.io.PrintStream;


/**
 * Provides basic functionality of a wurblet.
 * <p>
 * A wurblet is a code generator that is invoked from within a container,
 * the so-called wurbler. Wurblets generate code to one or more output streams.
 * The default output stream and the fixed source list is provided by the wurbler.
 * <p>
 * The general form of a wurblet anchor is:
 * <p>
 * {@code @wurblet <tag> <name> [options/arguments]}
 * <p>
 * It must be part of a line- or block-comment.
 * <p>
 * Whereas:
 * <ul>
 * <li><em>@wurblet</em> defines a wurblet anchor.</li>
 * <li><em>tag</em> is a tag-name to uniquely identify the generated code within the file.</li>
 * <li><em>name</em> is the name of the wurblet that generates the code.</li>
 * <li><em>options/arguments</em> are any number of options and/or arguments. Options begin with -- and may be
 * followed by = and a value.</li>
 * </ul>
 *
 * @author harald
 */
public abstract class AbstractWurblet implements Wurblet {

  protected Wurbler container;      // the container
  protected int phase = 1;          // the execution phase (default is 1)
  protected String configuration;   // the generic wurblet configuration
  protected PrintStream out;        // the default output stream
  protected String[] source;        // the fixed source


  @Override
  public void run() throws WurbelException {
    out    = container.getPrintStream();
    source = container.getSource();
  }


  @Override
  public void cleanup() throws WurbelException {
    // do nothing
  }

  @Override
  public void setContainer(Wurbler container) {
    this.container = container;
  }

  @Override
  public Wurbler getContainer() {
    return container;
  }

  @Override
  public int getPhase() {
    return phase;
  }

  @Override
  public void setPhase(int phase) {
    if (phase < 1) {
      phase = 1;
    }
    this.phase = phase;
  }

  @Override
  public String getConfiguration() {
    return configuration;
  }

  @Override
  public void setConfiguration(String config) {
    this.configuration = config;
  }


  /**
   * Gets the guardname
   * @return the guardname
   * @throws WurbelException if guardname could not be determined
   */
  public String getGuardName() throws WurbelException {
    String guardName = getContainer().getProperty(Wurbler.PROPSPACE_WURBLET, Wurbler.WURBLET_GUARDNAME);
    if (guardName == null) {
      throw new WurbelException("can't determine guardName");
    }
    return guardName;
  }


  @Override
  public String toString() {
    String name = getClass().getName();
    int ndx = name.lastIndexOf('.');
    if (ndx >= 0) {
      name = name.substring(ndx + 1);
    }
    StringBuilder buf = new StringBuilder();
    buf.append(name);
    try {
      String guardName = getGuardName();
      buf.append(" generating ").append(guardName);
    }
    catch (WurbelException wex) {
      // ignore
    }
    return buf.toString();
  }

}
