/*
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
 *
 */

package org.wurbelizer.misc;

/**
 * Constants used by the wurbelizer.
 *
 * @author harald
 */
public class Constants {

  /**
   * file extension for the wurblet source
   */
  public static final String WURBLET_SOURCE_EXTENSION = ".wrbl";

  /**
   * file extension for the generated Java source
   */
  public static final String JAVA_SOURCE_EXTENSION = ".java";

  /**
   * file extension for the generated fixed-text source
   */
  public static final String FILE_SOURCE_EXTENSION = ".ser";

  /**
   * Heredocs may start with this lead followed by the source info.
   */
  public static final String ORIGIN_INFO_LEAD = "#@";


  private Constants() {
  }

}
