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

import java.util.Map;

/**
 * Factory and cache for {@link HeapFile}s.<br>
 * The heapfiles are kept in the {@link WurbelContext#current() current} wurbel context.
 *
 * @author harald
 */
public final class HeapFileFactory {

  /**
   * initializes the heapfiles.
   */
  public static void initialize() {
    Map<String,HeapFile> heapFiles = heapFiles();
    synchronized (heapFiles) {
      heapFiles.clear();
    }
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
  public static HeapFile create(String heapName, String text) throws WurbelException {
    Map<String,HeapFile> heapFiles = heapFiles();
    synchronized (heapFiles) {
      if (heapFiles.get(heapName) != null) {
        throw new WurbelException("HeapFile '" + heapName + "' already exists");
      }
      HeapFile heapFile = new HeapFile(heapName, text);
      heapFiles.put(heapName, heapFile);
      return heapFile;
    }
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
  public static HeapFile createOrOverwrite(String sourceName, String heapName, String text) throws WurbelException {
    Map<String,HeapFile> heapFiles = heapFiles();
    synchronized (heapFiles) {
      HeapFile heapFile = heapFiles.get(heapName);
      if (heapFile != null) {
        if (heapFile.getSourceName() != null && sourceName != null &&
            !heapFile.getSourceName().equals(sourceName)) {
          throw new WurbelException("heapfile '" + heapName + "' created from more than one sources:\n" +
                                     sourceName + " !=\n" + heapFile.getSourceName());
        }
        heapFile.reset();
        if (text != null) {
          heapFile.getPrintStream().print(text);
        }
      }
      else  {
        heapFile = new HeapFile(heapName, text);
        heapFile.setSourceName(sourceName);
        heapFiles.put(heapName, heapFile);
      }
      return heapFile;
    }
  }

  /**
   * Gets the heapfile of a given name.
   *
   * @param heapName the name of the heapfile.
   * @return the heapfile, null if no such file.
   */
  public static HeapFile get(String heapName) {
    Map<String,HeapFile> heapFiles = heapFiles();
    synchronized (heapFiles) {
      return heapFiles.get(heapName);
    }
  }


  /**
   * Removes a heapfile.
   *
   * @param heapName the of heapfile
   * @return true if removed, false if no such file.
   */
  public static boolean delete(String heapName) {
    Map<String,HeapFile> heapFiles = heapFiles();
    synchronized (heapFiles) {
      return heapFiles.remove(heapName) != null;
    }
  }


  /**
   * Gets the heapfiles of the current wurbel context.
   *
   * @return the heapfiles
   */
  private static Map<String,HeapFile> heapFiles() {
    return WurbelContext.current().heapFiles();
  }


  private HeapFileFactory() {}

}
