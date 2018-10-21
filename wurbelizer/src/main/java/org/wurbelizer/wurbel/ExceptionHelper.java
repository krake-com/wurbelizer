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


package org.wurbelizer.wurbel;

/**
 * Helper methods dealing with exceptions.<br>
 * Partly copied from Tentackle project.
 *
 * @author harald
 */
public final class ExceptionHelper {

  /**
   * Concatenates the messages of an exception chain.
   * @param headOfChain the head of chained exceptions
   * @return the message string
   */
  public static String concatenateMessages(Throwable headOfChain) {
    StringBuilder buf = new StringBuilder();
    if (headOfChain.getMessage() != null) {
      buf.append(headOfChain.getMessage());
    }
    Throwable cause = headOfChain.getCause();
    while (cause != null) {
      String subMsg = cause.getMessage();
      if (subMsg != null && buf.indexOf(subMsg) < 0) {
        if (buf.length() > 0) {
          buf.append('\n');
        }
        buf.append(subMsg);
      }
      cause = cause.getCause();
    }
    return buf.toString();
  }


  /**
   * prevent instantiation.
   */
  private ExceptionHelper() { }

}
