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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Reader;
import java.io.StringReader;

/**
 * Utility class implementing in-memory files.<br>
 * Nice for here-documents that don't need to be stored on disk.
 *
 * @see HeapFileFactory
 * @author harald
 */
public class HeapFile {

  private final String name;                // name of the heapfile
  private final ByteArrayOutputStream data; // data for output stream
  private final PrintStream out;            // in memory output stream
  private int invocationCount;              // the last invocationCounter from wurbler-container (for optional use in wurblets)
  private String sourceName;                // the last name of the wurbeled sourcefile
  private byte[] savePoint;                 // last savepoint, null if none
  private boolean discarded;                // true if wurblets reading this heapfile should throw a WurbelDiscardException


  /**
   * Creates a heapfile.
   *
   * @param name the name of the heapfile
   * @param text the initial contents, null = empty.
   */
  public HeapFile(String name, String text) {

    this.name = name;

    data = new ByteArrayOutputStream();
    out = new PrintStream(data);
    if (text != null) {
      out.print(text);
    }
  }


  /**
   * Marks this heapfile as discarded.<br>
   * Such files should not be used to generate code because their contents are incomplete
   * because of former errors. Wurblets reading this file should throw a {@link WurbelDiscardException}.
   */
  public void discard() {
    discarded = true;
  }

  /**
   * Returns whether this heapfile is ok to use or should be discarded.
   * @return true wurblets reading this file should throw a {@link WurbelDiscardException}
   */
  public boolean isDiscarded() {
    return discarded;
  }

  /**
   * Resets (empties) the heapfile.
   */
  public void reset() {
    out.flush();
    data.reset();
  }

  /**
   * Gets the contents of a heapfile.
   *
   * @return the contents
   */
  public String getText() {
    out.flush();
    return data.toString();
  }

  /**
   * Gets the name of the heapfile.
   *
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * Creates a savepoint.
   */
  public void createSavePoint() {
    savePoint = data.toByteArray();
  }

  /**
   * Reverts to last savepoint.<br>
   * Does a reset if there is no savepoint.
   *
   * @throws WurbelException if revert failed
   */
  public void revertToSavePoint() throws WurbelException {
    reset();
    if (savePoint != null) {
      try {
        data.write(savePoint);
        savePoint = null;
      }
      catch (IOException ex) {
        throw new WurbelException("cannot rollback transaction for heapfile " + this, ex);
      }
    }
  }

  /**
   * Gets a reader for the contents.
   *
   * @return the reader
   */
  public Reader getReader() {
    return new StringReader(getText());
  }

  /**
   * Get the print stream to print into the heapfile.
   *
   * @return the print stream
   */
  public PrintStream getPrintStream()  {
    return out;
  }

  /**
   * Gets the last invocationCounter from wurbler-container (for optional use in wurblets).
   *
   * @return the last invocation count
   * @see HeapStream
   */
  public int getInvocationCount() {
    return invocationCount;
  }

  /**
   * Sets the last invocation count.
   * Wurblets may use this to save their invocation count to
   * check for redundant creation of heap files.
   *
   * @param invocationCount the invocation count.
   * @see HeapStream
   */
  public void setInvocationCount(int invocationCount) {
    this.invocationCount = invocationCount;
  }

  /**
   * Gets the remembered sourcename.
   *
   * @return the sourcename
   * @see HeapStream
   */
  public String getSourceName() {
    return sourceName;
  }

  /**
   * Sets the sourcename.
   *
   * @param sourceName the sourcename
   * @see HeapStream
   */
  public void setSourceName(String sourceName) {
    this.sourceName = sourceName;
  }

  @Override
  public String toString() {
    return name;
  }

}
