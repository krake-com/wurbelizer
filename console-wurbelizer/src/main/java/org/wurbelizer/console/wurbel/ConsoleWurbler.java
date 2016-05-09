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

import java.io.File;
import java.util.Arrays;
import java.util.regex.Pattern;

/**
 * Invokes the wurbler from the console.
 *
 * @author harald
 */
public class ConsoleWurbler {


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
    try {
      new org.wurbelizer.wurbel.DefaultWurbler(args).runWurblet();
    }
    catch (Exception ex)  {
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
        Arrays.sort(files, (File o1, File o2) -> o1.getPath().compareTo(o2.getPath()));

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
    catch (Exception ex)  {
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
