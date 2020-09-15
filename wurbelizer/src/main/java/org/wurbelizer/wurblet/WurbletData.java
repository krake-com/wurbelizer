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


package org.wurbelizer.wurblet;

import java.io.Serializable;


/**
 * Class holding the  wurblet data.
 * An instance of this class is serialized per wurblet.
 */

public class WurbletData implements Serializable {

  private static final long serialVersionUID = 2L;

  /**
   * The source strings.
   */
  private String[] source;

  /**
   * The wurblet-args.<br>
   * null = get default from container.
   */
  private String[] args;
  
  
  /**
   * Creates an instance of the wurblet data. This is usually
   * done by the wurbiler.
   *
   * @param source the fixed source part.
   * @param args the wurblet args. null = provided by container.
   */
  public WurbletData(String[] source, String[] args) {
    setSource(source);
    setArgs(args);
  }
  
  
  /**
   * Gets the preset arguments for the wurblet.
   *
   * @return preset (fixed) arguments, null = get from container
   */
  public String[] getArgs() {
    return this.args;
  }  
  
  
  /**
   * Sets the wurblet arguments (e.g. by the @{args} wurblet command).
   *
   * @param args fixed args, null = get from container
   */
  public void setArgs(String[] args) {
    this.args = args;
  }  
  
  /**
   * Get the fixed source.
   *
   * @return fixed source (never null!)
   */
  public String[] getSource() {
    return this.source;
  }
  
  
  /**
   * Set the fixed source.
   *
   * @param source fixed source as an array of String.
   */
  public void setSource(String[] source) {
    this.source = source;
  }
  
}
