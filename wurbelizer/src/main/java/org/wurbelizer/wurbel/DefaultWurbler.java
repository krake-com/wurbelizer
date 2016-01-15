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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import org.wurbelizer.misc.DefaultLogger;
import org.wurbelizer.misc.Logger;
import org.wurbelizer.misc.Verbosity;


/**
 * The default implementation of a wurblet container.<br>
 * Running standalone from the command line.<p>
 *
 * Because ANT does not provide a foreach "out of the box", the main-method supports
 * file lists as follows:
 * <pre>
 * java org.wurbelizer.DefaultWurbler --glob="blahdir/.*\.map$" &#64;
 * </pre>
 * The &#64; will be replaced by the filename and the wurblet is invoked for each such file.
 * Note that it's not a real globbing, but uses regex.
 * This is not the most generic solution, but it does the job for wurblets like CreateTableSql.
 * <p>
 * Note that this wurbler runs each wurblet only once!
 *
 * @author harald
 */

public class DefaultWurbler extends AbstractWurbler {

  private String analyzeDir;      // wurblet extra analyze dir
  private Logger logger;          // the default logger
  private Verbosity verbosity;    // tell what's going on


  /**
   * Creates an instance of a command line wurbler.
   *
   * @param args the command line arguments
   *
   * @throws WurbelException if creating the wurbler failed
   */
  public DefaultWurbler(String[] args) throws WurbelException {

    super();

    logger = new DefaultLogger();
    verbosity = Verbosity.DEFAULT;

    int     wurbArgCount  = 0;
    boolean wurbletLoaded = false;

    // parse command line args
    for (int argc=0; argc < args.length; argc++)  {
      String arg = args[argc];
      if (arg.startsWith("--out="))  {
        setPrintStream(createOutputStream(arg.substring(6), false));
        args[argc] = null;    // consumed
      }
      else if (arg.startsWith("++out="))  {
        setPrintStream(createOutputStream(arg.substring(6), true));
        args[argc] = null;    // consumed
      }
      else if (arg.startsWith("--analyzedir="))  {
        analyzeDir = arg.substring(10);
        args[argc] = null;    // consumed
      }
      else if (!wurbletLoaded && !arg.startsWith("--")) {
        // wurblet
        loadWurblet(arg, null, null);
        wurbletLoaded = true;
        args[argc] = null;    // consumed
      }
      else if (arg.startsWith("--verbosity=")) {
        try {
          this.verbosity = Verbosity.valueOf(arg.substring(12));
        }
        catch (Exception ex) {
          this.verbosity = Verbosity.DEFAULT;
        }
      }
      else  {
        wurbArgCount++;
      }
    }

    // allocate wurblet args (if container is allowed to)
    if (!isFixedArgs()) {
      String[] wurbletArgs = new String[wurbArgCount];
      wurbArgCount = 0;
      for (String arg : args) {
        if (arg != null)  {
          wurbletArgs[wurbArgCount++] = arg;
        }
      }
      setArgs(wurbletArgs);
    }
  }


  @Override
  public Verbosity getVerbosity() {
    return verbosity;
  }

  @Override
  public Logger getLogger() {
    return logger;
  }


  /**
   * Gets the loop count.
   */
  @Override
  public int getInvocationCount() {
    return 1;
  }


  /**
   * Gets an information file.<br>
   * Some wurblets need extra information which is provided in files
   * in the so-called analyzedir (example in ant: analyzedir=${apt})
   *
   * @param name is the filename relative to the analyzedir.
   */
  @Override
  public File getAnalyzeFile(String name) {
    return new File(analyzeDir, name);
  }



  /**
   * Creates the output stream (from --/++out=...)
   * @param name the name of the output stream
   * @param append true if append to stream if it exists
   * @return the stream
   * @throws WurbelException if creation failed
   */
  private PrintStream createOutputStream(String name, boolean append) throws WurbelException {
    try {
      return new PrintStream(new FileOutputStream(name, append));
    }
    catch (FileNotFoundException ex) {
      throw new WurbelException("cannot create output stream: " + name, ex);
    }
  }

}
