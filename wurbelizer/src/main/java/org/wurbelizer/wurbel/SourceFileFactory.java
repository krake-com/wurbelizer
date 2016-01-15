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

import java.util.HashMap;
import java.util.Map;

/**
 * A factory and cache for sourcefiles.
 *
 * @author harald
 */
public class SourceFileFactory {

  // all sourcefiles <filename>:<file>
  private static final Map<String,SourceFile> FILE_MAP = new HashMap<>();


  /**
   * Opens the file and load into memory.<br>
   * If the file already has been loaded, it will be reused.
   *
   * @param filename the filename
   * @return the open file
   *
   * @throws WurbelException if reading the file did fail
   */
  public static synchronized SourceFile open(String filename) throws WurbelException {
    SourceFile file = FILE_MAP.get(filename);
    if (file == null) {
      file = new SourceFile(filename);
    }
    return file;
  }


  /**
   * Discard all files in list, if any.
   */
  public static synchronized void initialize() {
    FILE_MAP.clear();
  }


  /**
   * Rewinds all output streams (makes them empty again).
   */
  public static synchronized void resetAll()  {
    for (SourceFile file: FILE_MAP.values())  {
      file.reset();
    }
  }


  /**
   * Closes all files and clears the vector
   *
   * @throws WurbelException if closing a file failed
   */
  public static synchronized void closeAll() throws WurbelException {
    for (SourceFile file: FILE_MAP.values())  {
      file.close();
    }
    FILE_MAP.clear();
  }


  private SourceFileFactory() {
  }

}
