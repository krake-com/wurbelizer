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

package org.wurbelizer.console.wurbile;

import java.io.File;
import java.io.IOException;
import org.wurbelizer.misc.DefaultLogger;
import org.wurbelizer.misc.Verbosity;
import org.wurbelizer.wurbile.SourceWurbiler;
import org.wurbelizer.wurbile.WurbileException;
import org.wurbelizer.wurbile.Wurbiler;

/**
 * Invokes the wurbiler from the console.
 *
 * @author harald
 */
public class ConsoleWurbiler {


  /**
   * Prints the usage.
   */
  private static void usage() {
    System.out.println(
      "usage: java org.wurbelizer.ConsoleWurbiler [--verbose=default|info|debug] [--indent=n] [--extends=<classpath>] [--package=<package>] <wurbelizable>.wrbl");
    System.exit(1);
  }


  /**
   * Invokes the wurbiler from the command line.
   *
   * @param args the command line arguments
   */
  public static void main(String[] args) {

    if (args.length < 1)  {
      usage();
    }

    String wrblname     = null;
    String parentClass  = null;
    String packageName  = null;
    int indent          = -1;
    Verbosity verbosity = Verbosity.DEFAULT;

    for (int argc=0; argc < args.length; argc++)  {
      String arg = args[argc];
      if      (arg.startsWith("--indent="))  {
        indent = Integer.parseInt(arg.substring(9));
      }
      else if (arg.startsWith("--extends="))  {
        parentClass = arg.substring(10);
      }
      else if (arg.startsWith("--package="))  {
        packageName = arg.substring(10);
      }
      else if (arg.startsWith("--verbosity=")) {
        try {
          verbosity = Verbosity.valueOf(arg.substring(12));
        }
        catch (Exception ex) {
          verbosity = Verbosity.DEFAULT;
        }
      }
      else if (argc == args.length - 1) {
        wrblname = arg;
      }
    }


    if (wrblname == null) {
      usage();
    }

    try {
      File wrblFile = new File(wrblname);
      String dirname = wrblFile.getParent();
      if (dirname == null) {
        dirname = ".";
      }
      File outputDir = new File(dirname);
      Wurbiler wurbiler = new SourceWurbiler(wrblFile, outputDir, outputDir, new DefaultLogger(), verbosity);
      // set options
      if (indent != -1)         {
        wurbiler.setIndent(indent);
      }
      if (packageName != null)  {
        wurbiler.setPackageName(packageName);
      }
      if (parentClass != null)  {
        wurbiler.setParentClass(parentClass);
      }
      // run the wurbiler
      int errors = wurbiler.compile();

      if (errors > 0) {
        System.err.println(errors + " error" + (errors > 1 ? "s" : ""));
        System.exit(2);
      }
    }
    catch (IOException | WurbileException | RuntimeException ex)  {
      ex.printStackTrace();
      System.exit(1);
    }

    System.exit(0);
  }

}
