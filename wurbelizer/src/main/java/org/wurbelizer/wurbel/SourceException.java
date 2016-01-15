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
 * Exception thrown whenever scanning of the sourcefile failed.<br>
 *
 * @author harald
 */

@SuppressWarnings("serial")
public class SourceException extends Exception {

  private SourceElement elem;
  private SourceDocument doc;


  /**
   * Constructs an {@code SourceException} with the specified detail message.
   *
   * @param elem the SourceElement containing the error, null if unknown.
   * @param message the detail message
   */
  public SourceException(SourceElement elem, String message) {
    super(message);
    this.elem = elem;
  }


  /**
   * Constructs an {@code SourceException} with the specified detail message.
   *
   * @param doc the SourceDocument containing the error, null if unknown.
   * @param message the detail message
   */
  public SourceException(SourceDocument doc, String message) {
    super(message);
    this.doc = doc;
  }


  @Override
  public String getMessage() {
    if (elem != null) {
      return elem + ": " + super.getMessage();
    }
    else if (doc != null) {
      return doc + ": " + super.getMessage();
    }
    else  {
      return super.getMessage();
    }
  }

}
