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

import java.nio.file.Path;
import java.util.Map;

/**
 * A factory and cache for sourcefiles.<br>
 * The sourcefiles are kept in the {@link WurbelContext#current() current} wurbel context.
 *
 * @author harald
 */
public final class SourceFileFactory {

  /**
   * Opens the file and loads it into memory.<br>
   * If the file already has been loaded, it will be reused.
   *
   * @param filename the filename
   * @return the open file
   *
   * @throws WurbelException if reading the file did fail
   */
  public static SourceFile open(String filename) throws WurbelException {
    String key = Path.of(filename).toAbsolutePath().normalize().toString();   // same file, regardless of how the name is given
    Map<String,SourceFile> sourceFiles = sourceFiles();
    synchronized (sourceFiles) {
      SourceFile file = sourceFiles.get(key);
      if (file == null) {
        file = new SourceFile(filename);
        sourceFiles.put(key, file);
      }
      return file;
    }
  }

  /**
   * Discards all cached files, if any.
   */
  public static void initialize() {
    Map<String,SourceFile> sourceFiles = sourceFiles();
    synchronized (sourceFiles) {
      sourceFiles.clear();
    }
  }

  /**
   * Rewinds all output streams (makes them empty again).
   */
  public static void resetAll()  {
    Map<String,SourceFile> sourceFiles = sourceFiles();
    synchronized (sourceFiles) {
      for (SourceFile file: sourceFiles.values())  {
        file.reset();
      }
    }
  }

  /**
   * Flushes all files and clears the cache.
   *
   * @return the number of updated files
   * @throws WurbelException if closing a file failed
   */
  public static int flushAll() throws WurbelException {
    Map<String,SourceFile> sourceFiles = sourceFiles();
    synchronized (sourceFiles) {
      int updated = 0;
      for (SourceFile file: sourceFiles.values())  {
        if (file.flush()) {
          updated++;
        }
      }
      sourceFiles.clear();
      return updated;
    }
  }


  /**
   * Gets the sourcefiles of the current wurbel context.
   *
   * @return the sourcefiles
   */
  private static Map<String,SourceFile> sourceFiles() {
    return WurbelContext.current().sourceFiles();
  }


  private SourceFileFactory() {}

}
