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

import org.wurbelizer.misc.Settings;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.Reader;
import java.io.Writer;


/**
 * In-memory file to check for changes before flush/flush.
 * <p>
 * Manages a static set of such SourceFiles that can be closed
 * and thus released at one (meant to be attached to a single
 * SourceDocument).<br>
 * Handles LF/CRLF mapping according to the original file contents.
 *
 * @author harald
 */
public class SourceFile {

  private static final int BUFSIZE = 1024;

  private final String filename;              // filename
  private String orgText;                     // original text
  private ByteArrayOutputStream outStream;    // output stream
  private PrintStream printStream;            // the print output stream
  private Boolean usingCRLF;                  // true if windoze line endings, false if unix, null if unknown


  /**
   * Creates a source file from a filename.
   *
   * @param filename is the name of the file
   * @throws WurbelException if reading the file did fail
   */
  public SourceFile(String filename) throws WurbelException {
    this.filename = filename;

    StringBuilder orgBuf = new StringBuilder(BUFSIZE);
    char[] chars = new char[BUFSIZE];
    int numRead;
    try (Reader in = new InputStreamReader(new FileInputStream(filename), Settings.getEncodingCharset())) {
      // read the source file according to the systems encoding
      while((numRead = in.read(chars)) > -1) {
        orgBuf.append(String.valueOf(chars, 0, numRead));
      }
      orgText = orgBuf.toString();
      /*
       * We cannot just check whether we are on windows or not because VCS like git or subversion
       * can be configured how to treat line endings. We must determine that from the file contents!
       */
      usingCRLF = orgText.contains("\r\n");
    }
    catch (FileNotFoundException e) {
      // not an error
    }
    catch (IOException ex) {
      throw new WurbelException("reading file " + filename + " failed", ex);
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
   * Returns whether the file is using CR+LF as line endings.
   *
   * @return true if windoze, false if unix
   */
  public Boolean isUsingCRLF() {
    return usingCRLF;
  }


  /**
   * Gets a stream for the file to be rewritten.<br>
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
   * Converts the line-endings if necessary.
   *
   * @param str the text
   * @return the possibly converted text
   */
  public String convertLineEndings(String str) {
    if (usingCRLF != null) {
      str = usingCRLF ? WurbelHelper.translateLFtoCRLF(str) : WurbelHelper.translateCRLFtoLF(str);
    }
    return str;
  }


  /**
   * Gets the new text/contents of the file.
   *
   * @return the new (changed) contents, null = no getStream() invoked.
   */
  public String getNewText()  {
    return outStream == null ? null : convertLineEndings(outStream.toString());
  }


  /**
   * Updates the file.<br>
   * Overrides the file's contents with given text.
   *
   * @param text the file's contents
   * @throws WurbelException if file IO failed
   */
  public void update(String text) throws WurbelException {
    File file = new File(filename);
    File parentFile = file.getParentFile();
    if (parentFile != null) {
      parentFile.mkdirs();  // create any missing directories
    }
    try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), Settings.getEncodingCharset())) {
      writer.write(text);
    }
    catch (IOException ex) {
      throw new WurbelException("flushing " + filename + " failed", ex);
    }
  }


  /**
   * Closes the file.<br>
   * Updates the file in the filesystem only if changes were detected.
   *
   * @return true if file was changed
   * @throws WurbelException if flush failed
   */
  public boolean flush() throws WurbelException {
    String text = getNewText();
    if (text != null && (orgText == null || !orgText.equals(text))) {
      update(text);
      return true;
    }
    return false;
  }


  /**
   * Clears all output.
   */
  public void reset() {
    if (outStream != null) {
      outStream.reset();
    }
  }

}
