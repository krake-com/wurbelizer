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
 */


package org.wurbelizer.wurbel;

import java.util.Properties;

/**
 * Utility class to scan for arguments and to do variable translation.<p>
 *
 * Arguments usually are separated by whitespaces.
 * However, sometimes arguments must include whitespaces, i.e.
 * must be quoted. There are two kinds quotes:<p>
 *
 * double-quotes like @{code "test: $remote == --remote"}. The $remote will be translated.
 * A single-quote in a double-quoted string is allowed.<p>
 *
 * single-quotes like @{code --orderby='" ORDER BY " + CN_ID + " DESC"'}.
 * No translation will take place and such args may contain double-quotes.<p>
 *
 * Double-quotes in a double-quoted string must be quoted with a backslash.
 * Same is true for single-quotes in a single quoted string.
 * A backslash is written as \\.<p>
 *
 * Notice that variable translation takes place <strong>after</strong> the arguments have been separated.
 * Thus, a variable may contain quotes and these quotes need <strong>not</strong> be escaped.
 *
 * @author harald
 */

public class ArgScanner {

  private final String str;               // the source string
  private final Properties otherProps;    // extra properties
  private final int len;                  // length of str
  private int ndx;                        // index in str (starting at 0)


  /**
   * Creates an instance of an arg scanner.
   *
   * @param str the text containing the args
   * @param otherProps extra properties if != null, else default properties only.
   */
  public ArgScanner(String str, Properties otherProps) {
    this.str = str;
    this.otherProps = otherProps;
    len = str == null ? 0 : str.length();
  }


  /**
   * Gets the next argument.
   *
   * @return the next argument or null if no more args found
   */
  public String next()  {

    StringBuilder part    = null;     // current part in argument
    StringBuilder arg     = null;     // != null if start of argument found
    boolean inDoubleQuote = false;    // true if in double quote string
    boolean inSingleQuote = false;    // true if in single quote string

    // find start of (next) argument: skip whitespaces
    while (ndx < len) {
      char c = str.charAt(ndx);
      if (arg == null)  {
        if (!Character.isWhitespace(c) && c != '\\') {
          // start found
          arg = new StringBuilder();
          continue;
        }
      }
      else  { // within arg
        if (part == null) {
          // new part begun
          part = new StringBuilder();
        }
        // check for escape
        if (c == '\\')  {
          // take the next character "as is"
          ndx++;
          if (ndx < len)  {
            part.append(str.charAt(ndx));
          }
        }
        else  {
          if (inDoubleQuote) {
            // next double-quote ends quoted string
            if (c == '"') {
              inDoubleQuote = false;
              // translate quoted section and append to arg
              arg.append(WurbelHelper.translateVariables(part.toString(), otherProps));
              part = null;
            }
            else  {
              // any other character is part of argument
              part.append(c);
            }
          }
          else if (inSingleQuote) {
            // next sinlge-quote ends quoted string
            if (c == '\'') {
              inSingleQuote = false;
              // append to arg untranslated
              arg.append(part);
              part = null;
            }
            else  {
              // any other character is part of argument
              part.append(c);
            }
          }
          else {    // not in any quoted string
            // next whitespace ends argument
            if (Character.isWhitespace(c))  {
              break;
            }
            // check for start of quoted string
            if      (c == '"') {
              inDoubleQuote = true;
            }
            else if (c == '\'') {
              inSingleQuote = true;
            }
            else  {
              // standard character: append to arg
              part.append(c);
            }
          }
        }
      }
      ndx++;
    }

    if (part != null) {
      // append last pending part
      if (inSingleQuote)  {
        // append untranslated
        arg.append(part);
      }
      else {
        // append translated
        arg.append(WurbelHelper.translateVariables(part.toString(), otherProps));
      }
    }

    return arg == null ? null : arg.toString();
  }

}
