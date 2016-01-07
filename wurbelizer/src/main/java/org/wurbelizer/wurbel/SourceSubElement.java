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

/**
 * Single- or block-comment-{@link SourceElement}s may contain {@code SubElement}s.<br>
 * Currently, the following SubElements are defined:
 * <ul>
 * <li>{@code SourceWurbletSubElement}: a wurblet anchor</li>
 * <li>{@code SourceHereSubElement}: a here-document</li>
 * <li>{@code SourcePropertySubElement}: a property-document</li>
 * </ul>
 * SubElements may be named or unnamed.
 *
 * @author harald
 * @version $Revision: 97 $
 */
public abstract class SourceSubElement {

  private final SourceElement element;      // the element containing the sub-element
  private final String text;                // contents of here-document
  private String name;                      // name of the sub element


  /**
   * Creates a SourceSubElement.
   *
   * @param element the SourceElement containing the sub element
   * @param name the name of the sub element
   * @param text the contents of the sub element
   */
  public SourceSubElement(SourceElement element, String name, String text) {
    this.element = element;
    this.name    = name;
    this.text    = text;
  }

  /**
   * Creates an unnamed SourceSubElement.
   *
   * @param element the SourceElement containing the sub element
   * @param text the contents of the sub element
   */
  public SourceSubElement(SourceElement element, String text) {
    this.element = element;
    this.text    = text;
  }


  /**
   * Gets the source element.
   *
   * @return the source element
   */
  public SourceElement getElement() {
    return element;
  }


  /**
   * Gets the name.
   *
   * @return the name, null if unnamed
   */
  public String getName() {
    return name;
  }


  /**
   * Sets the name
   *
   * @param name the name of the sub element
   */
  public void setName(String name) {
    this.name = name;
  }


  /**
   * Gets the contents.
   *
   * @return the text comprising the sub element
   */
  public String getText() {
    return text;
  }


  /**
   * String representation of the sub-element
   *
   * @return the string value
   */
  @Override
  public String toString() {
    return element + ": " + (name == null ? "<unnamed>" : name);
  }

}
