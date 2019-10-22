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

package org.wurbelizer.wurbile;

import java.util.StringTokenizer;

/**
 * Helper methods for the wurbiler.
 *
 * @author harald
 */
public class WurbileHelper {

  /**
   * Converts a string to a comment block.
   *
   * @param comment the comment string, null or empty if none
   * @param indent the indent in leading spaces
   * @return the comment block, empty string if no comment
   */
  public static String toCommentBlock(String comment, int indent) {
    StringBuilder buf = new StringBuilder();
    if (comment != null && !comment.isEmpty()) {
      String indentStr = " ".repeat(Math.max(0, indent));
      buf.append(indentStr).append("/**");

      StringTokenizer stok = new StringTokenizer(comment, "\n\r");
      while (stok.hasMoreTokens()) {
        String line = stok.nextToken();
        buf.append('\n').append(indentStr).append(" * ").append(line);
      }
      buf.append('\n').append(indentStr).append(" */\n");
    }
    return buf.toString();
  }

  private WurbileHelper() {
  }

}
