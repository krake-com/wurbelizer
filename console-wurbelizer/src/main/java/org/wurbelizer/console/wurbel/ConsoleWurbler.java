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

package org.wurbelizer.console.wurbel;

import org.wurbelizer.misc.DefaultLogger;
import org.wurbelizer.misc.Logger;
import org.wurbelizer.misc.Verbosity;
import org.wurbelizer.wurbel.AbstractWurbler;
import org.wurbelizer.wurbel.SourceException;
import org.wurbelizer.wurbel.WurbelException;
import org.wurbelizer.wurbel.WurbelResult;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

/**
 * Invokes the wurbler for a single wurblet from the console.
 * <p>
 * Because ANT does not provide a foreach "out of the box", the main-method supports
 * file lists as follows:
 * <pre>
 * java org.wurbelizer.console.ConsoleWurbler --glob="blahdir/.*\.map$" &#64;
 * </pre>
 * The &#64; will be replaced by the filename and the wurblet is invoked for each such file.
 * Note that it's not a real globbing, but uses regex.
 * This is not the most generic solution, but it does the job for wurblets like CreateTableSql.
 * <p>
 * Note that this wurbler runs each wurblet only once!
 *
 * @author harald
 */
public class ConsoleWurbler extends AbstractWurbler {


  private String analyzeDir;      // wurblet extra analyze dir
  private final Logger logger;    // the default logger
  private Verbosity verbosity;    // tell what's going on


  /**
   * Creates an instance of a command line wurbler.
   *
   * @param args the command line arguments
   *
   * @throws WurbelException if creating the wurbler failed
   */
  public ConsoleWurbler(String[] args) throws WurbelException {

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
        catch (RuntimeException ex) {
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
  public WurbelResult wurbel(int phase, Set<Integer> collectedPhases) throws WurbelException, SourceException {
    boolean executed = runWurblet(phase, collectedPhases);
    return new WurbelResult(executed ? 1 : 0,
                            0,
                            executed ? WurbelResult.UpdateType.UPDATED : WurbelResult.UpdateType.UNCHANGED);
  }

  @Override
  public Verbosity getVerbosity() {
    return verbosity;
  }

  @Override
  public Logger getLogger() {
    return logger;
  }

  @Override
  public int getInvocationCount() {
    return 1;
  }

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


  /**
   * Prints the usage.
   */
  private static void usage() {
    System.out.println(
      "usage: java org.wurbelizer.ConsoleWurbler [--|++out=<outputfile>] [--analyzedir=<dir>] [--verbosity=default|info|debug] <wurblet> [wurblet options and args...]");
    System.exit(1);
  }


  /**
   * single run
   */
  private static void run(String[] args) {
    TreeSet<Integer> collectedPhases = new TreeSet<>();
    try {
      int phase = 1;
      for (;;) {
        new ConsoleWurbler(args).wurbel(phase, collectedPhases);
        Integer nextPhase = collectedPhases.higher(phase);
        if (nextPhase == null) {
          break;
        }
        phase = nextPhase;
      }
    }
    catch (SourceException | WurbelException | RuntimeException ex)  {
      System.err.println("/*");
      ex.printStackTrace();
      System.err.println("*/");
    }
  }


  /**
   * globbing run
   */
  private static void globRun(String glob, String[] args) {
    try {
      // get the list of files
      int ndx = glob.lastIndexOf(File.separatorChar);    // cut last dir
      File globDir = new File(ndx > 0 ? glob.substring(0, ndx) : ".");
      if (ndx > 0)  {
        glob = glob.substring(ndx + 1);
      }
      final Pattern pattern = Pattern.compile(glob);

      // get files
      File[] files = globDir.listFiles((File dir, String name) -> pattern.matcher(name).matches());

      if (files != null) {
        // sort by name
        Arrays.sort(files, Comparator.comparing(File::getPath));

        for (File file: files) {
          // replace the file's name with the @, if found
          String[] sargs = new String[args.length];
          for (int i=0; i < args.length; i++) {
            String a = args[i];
            if (a.equals("@")) {
              // replace by file
              a = file.getPath();
            }
            sargs[i] = a;
          }
          // run the wurblet
          run(sargs);
        }
      }
    }
    catch (RuntimeException ex)  {
      System.err.println("/*");
      ex.printStackTrace();
      System.err.println("*/");
    }
  }


  /**
   * Invokes the wurbler from the command line.
   *
   * @param args the command line arguments
   */
  public static void main(String[] args) {

    if (args.length < 1)  {
      usage();
    }

    // check for glob option
    for (int i=0; i < args.length; i++) {
      String arg = args[i];
      if (arg.startsWith("--glob=")) {
        // run in glob mode
        String glob = arg.substring(7);
        args[i] = null;
        String[] globArgs = new String[args.length - 1];
        int ndx = 0;
        for (String a: args) {
          if (a != null) {
            globArgs[ndx++] = a;
          }
        }
        globRun(glob, globArgs);
        return;
      }
    }

    // single run
    run(args);
  }

}
