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

package org.wurbelizer.wurbile;

import org.wurbelizer.misc.Settings;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;

/**
 * Reader to load source files.
 *
 * @author harald
 */
public class NestedReader {

  private final BufferedReader reader;      // reader
  private final String name;                // the name
  private File file;                        // != null if included file reader
  private int lineNo;                       // line-number, starting at 1
  private int linePos;                      // position in line
  private String line;                      // the current line, null if read next

  /**
   * Creates a reader with a given name.
   *
   * @param reader the original reader
   * @param name the reader's name
   */
  public NestedReader(BufferedReader reader, String name) {
    this.reader = reader;
    this.name = name;
  }

  /**
   * Creates a nested reader from a file's pathname.
   *
   * @param path the file's pathname
   * @throws IOException if no such file
   */
  public NestedReader(String path) throws IOException {
    file = new File(path);
    name = file.getName();
    reader = Files.newBufferedReader(file.toPath(), Settings.getEncodingCharset());
  }

  /**
   * Reads the next character.
   *
   * @return the character, -1 if end of reader
   * @throws IOException if reading failed
   */
  public int read() throws IOException {
    if (line != null) {
      if (line.length() > linePos) {
        return line.charAt(linePos++);
      }
      line = null;
      return '\n';
    }

    // next line
    line = reader.readLine();
    if (line == null) {
      if (file != null) {
        reader.close(); // close include readers
      }
      return -1;    // end of file
    }

    lineNo++;
    linePos = 0;
    return read();
  }

  /**
   * Gets the original reader.
   *
   * @return the reader
   */
  public Reader getReader() {
    return reader;
  }

  /**
   * Gets the file if included.
   *
   * @return != null if file
   */
  public File getFile() {
    return file;
  }

  /**
   * Returns whether reader is included.
   *
   * @return true if including file reader
   */
  public boolean isIncluded() {
    return file != null;
  }

  /**
   * Gets the pathname.
   *
   * @return the pathname
   */
  public String getPath() {
    return file != null ? file.getPath() : name;
  }

  /**
   * Gets the current line number.
   *
   * @return the line number
   */
  public int getLineNo() {
    return lineNo;
  }

  /**
   * Gets the current position in line.
   *
   * @return the position
   */
  public int getLinePos() {
    return linePos;
  }

  @Override
  public String toString() {
    return name;
  }
}
