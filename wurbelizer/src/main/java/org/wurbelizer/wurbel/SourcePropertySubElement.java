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


package org.wurbelizer.wurbel;

import java.util.Properties;
import java.util.StringTokenizer;


/**
 * Property-Document: all text in between &#64;{ and &#64;} within a comment block.<br>
 * Property documents are unnamed.
 *
 * @author harald
 */
public class SourcePropertySubElement extends SourceSubElement {

  /**
   * Creates a property document.
   *
   * @param element the element containing the property document
   * @param text the contents of the property document
   */
  public SourcePropertySubElement(SourceElement element, String text) {
    super(element, null, text);
  }


  /**
   * Scan for properties and append them to given property list.
   *
   * @param props the properties to add the contents of this sub element to
   */
  public void scanTo(Properties props) {
    StringTokenizer stok = new StringTokenizer(getText(), "\n");
    while (stok.hasMoreTokens())  {
      WurbelHelper.scanPropertyLine(stok.nextToken(), props);
    }
  }

}
