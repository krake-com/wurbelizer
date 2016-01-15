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
 * Factory and cache for {@link HeapFile}s.
 *
 * @author harald
 */
public class HeapFileFactory {

  // all heapfiles
  private static final Map<String,HeapFile> HEAP_FILES = new HashMap<>();


  /**
   * initializes the heapfiles.
   */
  public static synchronized void initialize() {
    HEAP_FILES.clear();
  }

  /**
   * Creates a heapfile.
   *
   * @param heapName the unique name of the heapfile
   * @param text the initial contents, null = empty.
   * @return the heapfile
   *
   * @throws WurbelException if the file already exists
   */
  public static synchronized HeapFile create(String heapName, String text) throws WurbelException {
    if (HEAP_FILES.get(heapName) != null) {
      throw new WurbelException("HeapFile '" + heapName + "' already exists");
    }
    HeapFile heapFile = new HeapFile(heapName, text);
    HEAP_FILES.put(heapName, heapFile);
    return heapFile;
  }

  /**
   * Creates a heapfile if it does not exist.<br>
   * If it exists, its contents will be overwritten.
   *
   * @param sourceName the name of the source file this heapfile is associated to
   * @param heapName the unique name of the heapfile
   * @param text the initial contents, null = empty.
   * @return the heapfile
   * @throws WurbelException if heapname is not unique
   */
  public static synchronized HeapFile createOrOverwrite(String sourceName, String heapName, String text) throws WurbelException {
    HeapFile heapFile = HEAP_FILES.get(heapName);
    if (heapFile != null) {
      if (heapFile.getSourceName() != null && sourceName != null &&
          !heapFile.getSourceName().equals(sourceName)) {
        throw new WurbelException("heapfile '" + heapName + "' created from more than one sources: " +
                                   sourceName + " != " + heapFile.getSourceName());
      }
      heapFile.reset();
      if (text != null) {
        heapFile.getPrintStream().print(text);
      }
    }
    else  {
      heapFile = new HeapFile(heapName, text);
      heapFile.setSourceName(sourceName);
      HEAP_FILES.put(heapName, heapFile);
    }
    return heapFile;
  }

  /**
   * Gets the heapfile of a given name.
   *
   * @param heapName the name of the heapfile.
   * @return the heapfile, null if no such file.
   */
  public static synchronized HeapFile get(String heapName) {
    return HEAP_FILES.get(heapName);
  }


  /**
   * Removes a heapfile.
   *
   * @param heapName the of heapfile
   * @return true if removed, false if no such file.
   */
  public static synchronized boolean delete(String heapName) {
    return HEAP_FILES.remove(heapName) != null;
  }


  private HeapFileFactory() {
  }

}
