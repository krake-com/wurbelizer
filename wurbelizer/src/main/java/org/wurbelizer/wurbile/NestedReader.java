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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

/**
 *
 * @author harald
 */
public class NestedReader {

  private final Reader reader;      // reader
  private final String name;        // the name
  private File file;                // != null if included file reader
  private int line = 1;             // line-number, starting at 1
  private int pos;                  // position in line

  /**
   * Creates a reader with a given name.
   *
   * @param reader the original reader
   * @param name the reader's name
   */
  public NestedReader(Reader reader, String name) {
    this.reader = reader;
    this.name = name;
  }

  /**
   * Creates a reader from a file.
   *
   * @param path the file's pathname
   * @throws FileNotFoundException
   */
  public NestedReader(String path) throws FileNotFoundException {
    this.file = new File(path);
    this.name = file.getName();
    this.reader = new BufferedReader(new FileReader(file));
  }

  /**
   * Reads the next character.
   *
   * @return the character, -1 if end of reader
   * @throws IOException
   */
  public int read() throws IOException {
    int c = reader.read();
    if (c == -1 && file != null) {
      reader.close();  // close include readers
    }
    if (c == '\n') {
      line++;
      pos = 0;
    }
    else {
      pos++;
    }
    return c;
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
  public int getLine() {
    return line;
  }

  /**
   * Gets the current position in line.
   *
   * @return the position
   */
  public int getPos() {
    return pos;
  }

  @Override
  public String toString() {
    return name;
  }
}
