/**
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
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;


/**
 * In-memory file to check for changes before close/flush.
 * <p>
 * Manages a static set of such SourceFiles that can be closed
 * and thus released at one (meant to be attached to a single
 * SourceDocument).
 *
 * @author harald
 */

public class SourceFile {

  private static final int BUFSIZE = 1024;

  private String filename;                    // filename
  private String orgText;                     // original text
  private ByteArrayOutputStream outStream;    // output stream
  private PrintStream printStream;            // the print output stream


  /**
   * Creates a source file from a filename.
   *
   * @param filename is the name of the file
   * @throws WurbelException if reading the file did fail
   */
  public SourceFile(String filename) throws WurbelException {
    this.filename = filename;

    StringBuilder orgBuf = new StringBuilder(BUFSIZE);
    BufferedReader in = null;
    char[] chars = new char[BUFSIZE];
    int numRead;
    try {
      // read the source file according to the systems encoding
      in = new BufferedReader(new FileReader(filename));
      while((numRead = in.read(chars)) > -1) {
        orgBuf.append(String.valueOf(chars, 0, numRead));
      }
      orgText = orgBuf.toString();
    }
    catch (FileNotFoundException e) {
      // not an error
    }
    catch (IOException ex) {
      throw new WurbelException("reading file " + filename + " failed", ex);
    }
    finally {
      if (in != null) {
        try {
          in.close();
        }
        catch (Exception ex) {}
      }
    }
  }


  /**
   * Gets the filename.
   *
   * @return the filename
   */
  public String getFilename() {
    return filename;
  }


  /**
   * Gets the original text (contents) of the file.
   *
   * @return the original text, null = no such file
   */
  public String getOrgText()  {
    return orgText;
  }


  /**
   * Gets a stream for the file to be rewritten.
   * This will only create a memory stream for appending data.
   * It does not physically write to the file!
   *
   * @return the open stream
   */
  public PrintStream getStream()  {
    if (outStream == null)  {
      outStream = new ByteArrayOutputStream(); // holds the data
      printStream = new PrintStream(outStream);
    }
    return printStream;
  }


  /**
   * Gets the new text/contents of the file.
   *
   * @return the new (changed) contents, null = no getStream() invoked.
   */
  public String getNewText()  {
    return outStream == null ? null : outStream.toString();
  }


  /**
   * Checks whether the file is changed and needs a flush for persistance.
   *
   * @return true if newText differs from oldText
   */
  public boolean needsFlush() {
    if (orgText == null)  {
      return outStream != null;
    }
    else  {
      String newText = getNewText();
      return newText != null && orgText.equals(newText) == false;
    }
  }


  /**
   * Flushs the file (overwrite existing file or create a new one).
   *
   * @throws WurbelException if flush failed
   */
  public void flush() throws WurbelException {
    FileWriter fw = null;
    try {
      File file = new File(filename);
      File parentFile = file.getParentFile();
      if (parentFile != null) {
        parentFile.mkdirs();  // create any missing directories
      }
      fw = new FileWriter(file);
      if (outStream != null)  {
        fw.write(outStream.toString());
      }
    }
    catch (IOException ex) {
      throw new WurbelException("flushing " + filename + " failed", ex);
    }
    finally {
      try {
        fw.close();
      }
      catch (Exception ex) { }
    }
  }


  /**
   * Clears all output.
   */
  public void reset() {
    if (outStream != null) {
      outStream.reset();
    }
  }


  /**
   * Closes the file.
   * Will flush if necessary.
   *
   * @throws WurbelException if close failed
   */
  public void close() throws WurbelException {
    if (needsFlush()) {
      flush();
    }
  }

}
