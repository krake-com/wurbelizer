/*
 * Tentackle - a framework for java desktop applications
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

import java.io.PrintStream;

/**
 * A stream backed up by a heapfile.
 *
 * @author harald
 */
public class HeapStream {

  private final int invocationCount;          // wurbler invocation count
  private final String sourceName;            // name of wurbeled source file
  private final PrintStream codeStream;       // the code stream
  private final HeapFile heapFile;            // the heap file


  /**
   * Create a stream on the heap
   *
   * @param container the wurblet container
   * @param heapName the name of the code stream
   * @throws WurbelException if stream could not be created
   */
  @SuppressWarnings("this-escape")
  public HeapStream(Wurbler container, String heapName) throws WurbelException {
    // remember invocation params
    invocationCount = container.getInvocationCount();
    sourceName = container.getProperty(Wurbler.PROPSPACE_WURBLET, Wurbler.WURBLET_FILENAME);

    // create remote stream and file
    heapFile = createOrGetHeapFile(heapName);
    codeStream = new PrintStream(heapFile.getPrintStream());
  }


  /**
   * Marks the heapfile or this stream as discarded.<br>
   * Such files should not be used to generate code because their contents are incomplete
   * because of former errors. Wurblets reading this file should throw a {@link WurbelDiscardException}.
   */
  public void discard() {
    heapFile.discard();
  }

  /**
   * Returns whether this heapfile is ok to use or should be discarded.
   * @return true wurblets reading this file should throw a {@link WurbelDiscardException}
   */
  public boolean isDiscarded() {
    return heapFile.isDiscarded();
  }


  /**
   * Gets the stream.
   *
   * @return get the remote stream
   */
  public PrintStream getStream()  {
    return codeStream;
  }


  /**
   * Appends a text to the stream (without newline).
   *
   * @param text the text to append
   */
  public void append(String text) {
    codeStream.print(text);
  }


  /**
   * Gets the heapfile or creates it if new.
   *
   * @param heapName the name of the heapfile
   * @return the heapfile
   *
   * @throws WurbelException if heapfile could not be reverted of created
   */
  protected HeapFile createOrGetHeapFile(String heapName) throws WurbelException {
    HeapFile file = HeapFileFactory.get(heapName);
    if (file != null)  {
      // heapfile already exists
      if (sourceName.equals(file.getSourceName())) {
        // same source file
        if (invocationCount > file.getInvocationCount()) {
          // same sourcefile wurbeled again (due to mods):
          // revert contents
          file.revertToSavePoint();
        }
      }
      else {
        // wurblet from another sourcefile initially references this heapfile:
        // remember current state
        file.createSavePoint();
      }
    }
    else  {
      file = HeapFileFactory.create(heapName, null);
    }

    file.setInvocationCount(invocationCount);
    file.setSourceName(sourceName);
    return file;
  }

}
