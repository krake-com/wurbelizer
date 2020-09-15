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


package org.wurbelizer.ant.wurbile;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.MatchingTask;
import org.apache.tools.ant.types.Path;
import org.wurbelizer.ant.misc.AntLogger;
import org.wurbelizer.misc.Constants;
import org.wurbelizer.misc.Verbosity;
import org.wurbelizer.wurbile.SourceWurbiler;
import org.wurbelizer.wurbile.WurbileException;
import org.wurbelizer.wurbile.Wurbiler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * Ant Task to invoke the SourceWurbiler.<br>
 * This task will recursively scan the sourcedir looking
 * for *.wrbl-files to wurbile.<p>
 *
 * Integrate into ant with:
 * <pre>
 *   &lt;taskdef name="wurbile" classname="org.wurbelizer.AntWurbiler"/&gt;
 * </pre>
 * Make sure that wurbelizer.jar is in the classpath of ant.
 *
 * @author harald
 */

public class AntWurbiler extends MatchingTask {

  private static final String FAIL_MSG = "wurbilation failed";

  /**
   * True if fail on wurblet error.
   */
  protected boolean failOnError;

  /**
   * List of all wrbl-files that need to be wurbiled.
   */
  protected File[] wurbleList;


  private Path src;                 // source path (for wrbl-files)
  private File destDir;             // destination directory
  private Verbosity verbosity;      // tell what's going on
  private int indent;               // source indent, -1 = default = 4
  private String parentClass;       // implementing parent class (default = AbstractWurblet)
  private String packageName;       // package name, null = none



  /**
   * Creates an instance of a wurbiler within ant.
   */
  public AntWurbiler() {
    super();
    failOnError = true;
    verbosity   = Verbosity.DEFAULT;
    wurbleList  = new File[0];
    indent      = -1;
  }


  /**
   * Sets the verbosity.
   * @param verbosity one of {@code "default"}, {@code "info"} or {@code "debug"}
   */
  public void setVerbosity(String verbosity) {
    try {
      this.verbosity = Verbosity.valueOf(verbosity.toUpperCase());
    }
    catch (RuntimeException ex) {
      this.verbosity = Verbosity.DEFAULT;
    }
  }


  /**
   * Gets the verbosity.
   * @return the verbosity
   */
  public String getVerbosity() {
    return verbosity.toString().toLowerCase();
  }


  /**
   * Gets the initial source indent.
   * Defaults to 4.
   *
   * @return the source ident.
   */
  public int getIndent() {
    return indent;
  }


  /**
   * Sets the initial source ident (leading spaces for compiled wurblet source)
   * Default is 4.
   *
   * @param indent the source indent, -1 = default (4)
   */
  public void setIndent(int indent) {
    this.indent = indent;
  }


  /**
   * Gets the parent class.
   *
   * @return the parent class, null = default (AbstractWurblet)
   */
  public String getParentClass() {
    return parentClass;
  }


  /**
   * Sets the parent class.
   *
   * @param parentClass the parent class, null = default (AbstractWurblet)
   */
  public void setParentClass(String parentClass) {
    this.parentClass = parentClass;
  }


  /**
   * Gets the packageName.
   *
   * @return the package name
   */
  public String getPackageName() {
    return packageName;
  }


  /**
   * Sets the package name.
   *
   * @param packageName the package name, null = default
   */
  public void setPackageName(String packageName) {
    this.packageName = packageName;
  }


  /**
   * Creates a path for source compilation.
   *
   * @return a nested src element.
   */
  public Path createSrc() {
    if (src == null) {
      src = new Path(getProject());
    }
    return src.createPath();
  }


  /**
   * Recreates a path for source compilation.
   *
   * @return a nested src element.
   */
  protected Path recreateSrc() {
    src = null;
    return createSrc();
  }


  /**
   * Sets the source directories to find the source Java files.
   *
   * @param srcDir the source directories as a path
   */
  public void setSrcdir(Path srcDir) {
    if (src == null) {
      src = srcDir;
    } else {
      src.append(srcDir);
    }
  }


  /**
   * Gets the source dirs to find the source java files.
   *
   * @return the source directories as a path
   */
  public Path getSrcdir() {
    return src;
  }


  /**
   * Sets the destination directory into which the Java source
   * files should be compiled.
   *
   * @param destDir the destination director
   */
  public void setDestdir(File destDir) {
    this.destDir = destDir;
  }


  /**
   * Gets the destination directory into which the java source files
   * should be compiled.
   *
   * @return the destination directory
   */
  public File getDestdir() {
    return destDir;
  }


  /**
   * Indicates whether the build will continue
   * even if there are compilation errors; defaults to true.
   *
   * @param fail if true halt the build on failure
   */
  public void setFailOnError(boolean fail) {
    failOnError = fail;
  }


  /**
   * Sets whether to proceed on error.
   * This is the inverse of Failonerror.
   *
   * @param proceed true to proceed on error
   */
  public void setProceed(boolean proceed) {
    failOnError = !proceed;
  }


  /**
   * Gets the failonerror flag.
   *
   * @return the failonerror flag
   */
  public boolean getFailOnError() {
    return failOnError;
  }


  /**
   * Executes the task.
   * <p>
   * Throws {@link BuildException} if an error occurs.
   */
  @Override
  public void execute() {

    checkParameters();
    resetFileLists();

    // scan source directories directory to build up wurbelize list
    String[] list = src.list();
    for (String list1 : list) {
      File srcDir = getProject().resolveFile(list1);
      if (!srcDir.exists()) {
        throw new BuildException("srcdir \""
                + srcDir.getPath()
                + "\" does not exist!", getLocation());
      }
      DirectoryScanner ds = this.getDirectoryScanner(srcDir);
      String[] files = ds.getIncludedFiles();
      scanDir(srcDir, files);
    }

    wurbile();
  }


  /**
   * Clears the list of files to be compiled and copied.
   */
  protected void resetFileLists() {
    wurbleList = new File[0];
  }


  /**
   * Scans the directory looking for source files to be wurbelized.
   * The results are returned in the class variable wurbleList.
   *
   * @param srcDir the source directory
   * @param files the array of filenames
   */
  protected void scanDir(File srcDir, String[] files) {

    List<File> wurbFiles = new ArrayList<>();

    for (String file1 : files) {
      if (file1.endsWith(Constants.WURBLET_SOURCE_EXTENSION)) {
        // check if file exists
        File file = new File(srcDir.getPath() + File.separator + file1);
        if (file.exists())  {
          wurbFiles.add(file);
        }
      }
    }
    wurbleList = new File[wurbFiles.size()];
    Iterator<File> iter = wurbFiles.iterator();
    int i = 0;
    while (iter.hasNext())  {
      wurbleList[i++] = iter.next();
    }
  }


  /**
   * Gets the list of files to be compiled.
   *
   * @return the list of files as an array
   */
  public File[] getFileList() {
    return wurbleList;
  }


  /**
   * Checks that all required attributes have been set and nothing
   * silly has been entered.
   * <p>
   * Throws {@link BuildException} if an error occurs.
   */
  protected void checkParameters() {
    if (src == null) {
      throw new BuildException("srcdir attribute must be set!", getLocation());
    }
    if (src.size() == 0) {
      throw new BuildException("srcdir attribute must be set!", getLocation());
    }
  }


  /**
   * Compiles the wurblets.
   */
  protected void wurbile()  {

    int wurbiledFilesCount  = 0;
    int errorFilesCount     = 0;

    for (File file : wurbleList) {
      try {
        File outputDir = destDir;
        if (outputDir == null)  {
          String dirname = file.getParent();
          if (dirname == null) {
            dirname = ".";
          }
          outputDir = new File(dirname);
        }

        // create wurbiler
        Wurbiler wurbiler = new SourceWurbiler(file, outputDir, outputDir, new AntLogger(getProject()), verbosity);

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

        wurbiledFilesCount++;
        if (verbosity.isDebug()) {
          log("------ " + file.getName() + " ------");
        }

        // run the wurbiler
        int errors = wurbiler.compile();

        if (errors == 0) {
          if (verbosity.isInfo() && !verbosity.isDebug()) {
            // file has been modified without errors
            log(file.getName());
          }
        }
        else  {
          errorFilesCount++;
          log(file.getName() + ": " + errors + " wurbile errors");
        }
      }
      catch (IOException | WurbileException | RuntimeException ex)  {
        errorFilesCount++;
        log(ex.toString(), Project.MSG_ERR);
      }
    }

    log(wurbiledFilesCount + " files wurbiled, " + errorFilesCount + " errors");

    if (errorFilesCount > 0) {
      if (failOnError) {
         throw new BuildException(FAIL_MSG, getLocation());
      } else {
         log(FAIL_MSG, Project.MSG_ERR);
      }
    }

  }

}
