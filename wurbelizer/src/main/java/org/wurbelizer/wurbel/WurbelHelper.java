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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import org.wurbelizer.misc.Settings;



/**
 * Helper methods for the Wurbelizer.
 */

public class WurbelHelper {

  private static final String LINE_DELIM = "\n";
  private static Properties envProperties   = null;     // properties from environment
  private static Properties extraProperties = null;     // extra properties (e.g. from ant)


  /**
   * Translates variables using StringBuilder.
   * The translation is recursive, i.e. loops until no more replacements were performed.
   *
   * @param arg the argument to be translated/parsed
   * @param getenv true if check environment, false if system property
   * @param lastLoop true if translate \$ to $ finally
   * @param otherProps != null if use these properties too for translation
   *
   * @return the parsed argument
   */
  public static StringBuilder translateVariables(StringBuilder arg, boolean getenv, boolean lastLoop, Properties otherProps) {

    if (getenv && envProperties == null)  {
      envProperties = new Environment();    // initialize first
    }

    boolean replaced;

    do  {
      StringTokenizer stok = new StringTokenizer(arg.toString(), getenv ? " /.,;:()[]=!\t\r\n'\"" : " /,;:()[]=!\t\r\n'\"");
      replaced = false;
      while (stok.hasMoreTokens())  {
        String token = stok.nextToken();
        if (token.charAt(0) == '$' && token.length() > 1) {
          String key = token.substring(1);
          if (token.charAt(1) == '{') {
            // token embraced as ${key}...
            int curlyNdx = token.indexOf('}');
            if (curlyNdx > 2) {
              token = token.substring(0, curlyNdx + 1); // cut after }
              key = token.substring(2, curlyNdx);
            }
          }

          String value;
          if (getenv) {
            value = envProperties.getProperty(key);
          }
          else  {
            value = System.getProperty(key);
            if (value == null && extraProperties != null) {
              value = extraProperties.getProperty(key);
            }
          }
          if (value == null && otherProps != null)  {
            value = otherProps.getProperty(key);
          }
          if (value != null)  {
            // found a variable
            int ndx = arg.indexOf(token);
            arg.replace(ndx, ndx + token.length(), value);
            replaced = true;
            break;
          }
        }
      }
    } while (replaced);

    if (lastLoop) {
      // translate \$ to $ (this allows \$ to suppress variable substituion)
      int len = arg.length() - 1;
      for (int i=0; i < len; i++) {
        if (arg.charAt(i) == '\\' && arg.charAt(i+1) == '$') {
          arg.deleteCharAt(i);
          len--;
        }
      }
    }

    return arg;
  }


  /**
   * Translates variables in Strings.
   * The translation is recursive, i.e. loops until no more replacements were performed.
   *
   * @param arg the argument to be translated/parsed
   * @param getenv true if check environment, false if system property
   * @param lastLoop true if translate \$ to $ finally
   * @param otherProps != null if use these properties too for translation
   *
   * @return the parsed argument
   */
  public static String translateVariables(String arg, boolean getenv, boolean lastLoop, Properties otherProps) {
    return translateVariables(new StringBuilder(arg), getenv, lastLoop, otherProps).toString();
  }


  /**
   * Translates variables in arguments, first properties, second environmen. Using StringBuilder.
   *
   * @param arg argument to be translated/parsed
   * @param otherProps != null if use these properties too for translation
   *
   * @return the parsed argument
   */
  public static StringBuilder translateVariables(StringBuilder arg, Properties otherProps) {
    arg = translateVariables(arg, false, false, otherProps);
    return translateVariables(arg, true, true, otherProps);
  }


  /**
   * Translates variables in arguments, first properties, second environment
   *
   * @param arg argument to be translated/parsed
   * @param otherProps != null if use these properties too for translation
   *
   * @return the parsed argument
   */
  public static String translateVariables(String arg, Properties otherProps) {
    return translateVariables(new StringBuilder(arg), otherProps).toString();
  }



  /**
   * Gets the environment variables.
   */
  @SuppressWarnings("serial")
  private static class Environment extends Properties {

    public Environment() {

      super();

      // since 1.5 getenv() is back again :-))
      for (Map.Entry<String,String> entry: System.getenv().entrySet()) {
        setProperty(entry.getKey(), entry.getValue());
      }
    }
  }


  /**
   * Set extra properties (valid among all wurblets).
   *
   * @param props the properties
   */
  public static void setExtraProperties(Properties props) {
    extraProperties = props;
  }


  /**
   * Gets the extra properties.
   *
   * @return the properties
   */
  public static Properties getExtraProperties() {
    return extraProperties;
  }


  /**
   * Sets environment properties (valid among all wurblets)
   *
   * @param props the properties
   */
  public static void setEnvProperties(Properties props) {
    envProperties = props;
  }


  /**
   * Gets the environment properties.
   *
   * @return the properties
   */
  public static Properties getEnvProperties() {
    return envProperties;
  }


  /**
   * Perform indentation on generated source code.
   * Implements the indent=... wurbler argumemt
   *
   * @param source is the original string (of lines)
   * @param indent is the number of spaces to insert at start of each line
   *
   * @return the new source string
   */
  public static String indent(String source, int indent) {
    if (source != null && indent > 0) {

      StringBuilder ibuf = new StringBuilder(indent);
      for (int i=0; i < indent; i++)  {
        ibuf.append(' ');
      }

      StringBuilder buf = new StringBuilder(source.length());

      StringTokenizer stok = new StringTokenizer(source, LINE_DELIM, true);
      while (stok.hasMoreTokens())  {
        String line = stok.nextToken();
        if (!line.equals(LINE_DELIM))  {
          buf.append(ibuf);
        }
        buf.append(line);
      }

      return buf.toString();
    }
    return source;
  }



  /**
   * Scans a property line.
   * The line must NOT contain newlines!
   * Everything after a hash '#' is considered as comment.
   *
   * @param line is the source line
   * @param props are the properties to add to
   *
   * @return the key if property added
   */
  public static String scanPropertyLine(String line, Properties props) {
    int commentNdx = line.indexOf('#');
    if (commentNdx >= 0)  {
      line = line.substring(0, commentNdx);
    }
    StringTokenizer stok = new StringTokenizer(line, " =\t");
    if (stok.hasMoreTokens())  {
      String key = stok.nextToken();
      int ndx = line.indexOf('=');
      if (ndx > 0) {
        String value = line.substring(ndx + 1).trim();
        // add key/value to properties
        props.setProperty(key, value);
        return key;
      }
    }
    return null;
  }


  /**
   * Gets the part of a string before a given delimiter.
   *
   * @param str the string
   * @param delimiter the delimiter
   * @return the first part up to but excluding delimiter or the string if no delimiter at all
   */
  public static String firstBefore(String str, char delimiter) {
    if (str != null) {
      int ndx = str.indexOf(delimiter);
      if (ndx >= 0) {
        str = str.substring(0, ndx);
      }
    }
    return str;
  }


  /**
   * Gets the part of a string after a given delimiter.
   *
   * @param str the string
   * @param delimiter the delimiter
   * @return the last part following delimiter or the string if no delimiter at all
   */
  public static String lastAfter(String str, char delimiter) {
    if (str != null) {
      int ndx = str.lastIndexOf(delimiter);
      if (ndx >= 0) {
        str = str.substring(ndx + 1);
      }
    }
    return str;
  }


  /**
   * Gets the first line from a multi-line string.
   * Nice in tables.
   * @param str the multiline string
   * @return the first line, null if str == null
   */
  public static String firstLine(String str) {
    return firstBefore(str, '\n');
  }



  /**
   * Gets the reader for a given filename.<br>
   * By convention, if the name starts with a dot, it is expected
   * to be a HeapFile in memory.
   * Otherwise it's a real file.
   *
   * @param fileName the name of the file
   * @return the reader
   *
   * @throws WurbelException if reading the file failed, cause is {@link FileNotFoundException} if file does not exist
   */
  public static Reader openReader(String fileName) throws WurbelException {
    if (fileName.charAt(0) == '.')  {
      HeapFile hf = HeapFileFactory.get(fileName.substring(1));
      if (hf == null)  {
        throw new WurbelException("no such heapfile '" + fileName + "'",
                                  new FileNotFoundException(fileName));
      }
      if (hf.isDiscarded()) {
        throw new WurbelDiscardException("heapfile '" + fileName + "' discarded");
      }
      return hf.getReader();
    }
    else  {
      try {
        return new BufferedReader(new InputStreamReader(new FileInputStream(fileName), Settings.encodingCharset));
      }
      catch (FileNotFoundException ex) {
        throw new WurbelException("cannot open reader for " + fileName, ex);
      }
    }
  }


  /**
   * Deletes a file.
   *
   * @param fileName the filename, from heap if starts with a dot
   * @return true if deleted, false if no such file
   */
  public static boolean deleteFile(String fileName) {
    if (fileName.charAt(0) == '.')  {
      return HeapFileFactory.delete(fileName.substring(1));
    }
    else  {
      return new File(fileName).delete();
    }
  }


  /**
   * Translates a string to CRLF line endings.<br>
   * Already existing CRLFs are left unchanged.
   *
   * @param str the string to translate
   * @return the translated string
   */
  public static String translateLFtoCRLF(String str) {
    if (str != null && !str.isEmpty()) {
      StringBuilder buf = new StringBuilder(str.length() << 1);
      char lastC = 0;
      for (int i=0; i < str.length(); i++) {
        char c = str.charAt(i);
        if (c == '\n' && lastC != '\r') {
          buf.append('\r');
        }
        buf.append(c);
        lastC = c;
      }
      return buf.toString();
    }
    return str;
  }

  /**
   * Translates a string to LF line endings.
   *
   * @param str the string to translate
   * @return the translated string
   */
  public static String translateCRLFtoLF(String str) {
    if (str != null && !str.isEmpty()) {
      str = str.replace("\r\n", "\n");
    }
    return str;
  }


  private WurbelHelper() {
  }

}
