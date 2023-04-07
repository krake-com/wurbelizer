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


package org.wurbelizer.ant.wurbel;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.MatchingTask;
import org.apache.tools.ant.types.Path;
import org.wurbelizer.ant.misc.AntLogger;
import org.wurbelizer.misc.Constants;
import org.wurbelizer.misc.Verbosity;
import org.wurbelizer.wurbel.HeapFileFactory;
import org.wurbelizer.wurbel.SourceException;
import org.wurbelizer.wurbel.SourceWurbler;
import org.wurbelizer.wurbel.WurbelException;
import org.wurbelizer.wurbel.WurbelHelper;
import org.wurbelizer.wurbel.WurbelResult;
import org.wurbelizer.wurbel.Wurbler;

import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;


/**
 * Ant Task to invoke the SourceWurbler.<br>
 * This task will recursively scan the sourcedir looking *.java-files or
 * *.wurb-files and their corresponding Java source files to wurbelize.
 * If it finds a wurb-file <strong>and</strong> its java-file, the java file will be passed
 * through the SourceWurbler applying all wurblet-directives. As an alternative,
 * the java files can be given without wurb-files.<p>
 *
 * Integrate into ant with:
 * <pre>
 *   &lt;taskdef name="wurbel" classname="org.wurbelizer.AntWurbler"/&gt;
 * </pre>
 * Make sure that wurbelizer.jar is in the classpath of ant.
 *
 * @author harald
 */


public class AntWurbler extends MatchingTask {

  private static final String FAIL_MSG = "Wurbelization failed! See the wurbelizers error output for details.";

  // list of files already processed (for runOnce) within an ant-session
  private static final Set<String> filesDone = new TreeSet<>();

  private         Path src;             // source path (must be the same for java and wurb-files!)
  private         String[] wurbletPath; // classpath for loading the wurblets
  private         String analyzeDir;    // wurblet extra info file
  private boolean failOnError;          // fail on wurblet error (default)
  private boolean printStackTrace;      // true = print stacktrace on each exception
  private Verbosity verbosity;          // tell what's going on
  private boolean runOnce;              // true = don't wurbelize a file more than once within an ant-session
  private boolean dryRun;               // true = don't run wurblets, just parse
  private File[]  wurbleList;           // list of all java-files that need to be wurbelized



  /**
   * Creates an instanceof of a wurbler for wurbelization of source files from within ant.
   */
  public AntWurbler() {
    super();
    failOnError     = true;
    printStackTrace = false;
    verbosity       = Verbosity.DEFAULT;
    runOnce         = true;
    wurbleList      = new File[0];
  }


  /**
   * Sets the verbosity.
   * @param verbosity one of {@code "default"}, {@code "info"} or {@code "debug"}
   */
  public void setVerbosity(String verbosity) {
    try {
      this.verbosity = Verbosity.valueOf(verbosity.toUpperCase(Locale.ROOT));
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
    return verbosity.toString().toLowerCase(Locale.ROOT);
  }


  /**
   * Sets whether files should be wurbelized more than once within a single ant-run.
   * Enabling this feature allows specifying source files with wildcards in more than
   * one ant target without the need to provide an extra exclude-clause.
   *
   * @param runOnce true if run once (default), false to run even more than once.
   */
  public void setRunOnce(boolean runOnce) {
    this.runOnce = runOnce;
  }


  /**
   * Gets the runonce feature.
   *
   * @return true if files are not wurbelized more than once within an ant-session
   */
  public boolean isRunOnce() {
    return runOnce;
  }


  /**
   * Returns whether this is only a dry run.
   *
   * @return true if dry run
   */
  public boolean isDryRun() {
    return dryRun;
  }

  /**
   * Sets the dry run property.
   *
   * @param dryRun true to just parse the source file, don't run any wurblets
   */
  public void setDryRun(boolean dryRun) {
    this.dryRun = dryRun;
  }


  /**
   * Sets the wurblet info directory.
   *
   * @param analyzeDir the extra info directory
   */
  public void setAnalyzeDir(String analyzeDir) {
    this.analyzeDir = analyzeDir;
  }


  /**
   * Gets the wurblet info directory.
   *
   * @return the extra info directory
   */
  public String getAnalyzeDir() {
    return analyzeDir;
  }


  /**
   * Sets the print stacktrace feature.
   * If an exception in thrown while executing a wurblet the wurbler prints a
   * diagnostic message containing the file and location within the file of the wurblet-anchor.
   * Setting this attribute to true will also print a stacktrace which is handy
   * for debugging wurblets.
   *
   * @param printStackTrace if true (false is default)
   */
  public void setPrintStackTrace(boolean printStackTrace) {
    this.printStackTrace = printStackTrace;
  }


  /**
   * Gets the stacktrace flag.
   *
   * @return the stacktrace flag
   */
  public boolean getPrintStackTrace() {
    return printStackTrace;
  }



  /**
   * Creates a path for source wurbelization.
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
   * Recreate a path for source wurbelization.
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
   * Sets the fail on error flag.
   * Indicates whether the build will continue
   * even if there are compilation errors; defaults to true.
   *
   * @param fail if true halt the build on failure
   */
  public void setFailOnError(boolean fail) {
    failOnError = fail;
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
   * Sets whether to proceed on error.
   * This is the inverse of Failonerror.
   *
   * @param proceed true to proceed on error
   */
  public void setProceed(boolean proceed) {
    failOnError = !proceed;
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
    exportProperties();

    // scan source directories directory to build up wurbelize list
    String[] list = src.list();
    for (String list1 : list) {
      File srcDir = getProject().resolveFile(list1);
      if (!srcDir.exists()) {
        throw new BuildException("srcdir \""
                + srcDir.getPath()
                + "\" does not exist!", getLocation());
      }
      DirectoryScanner ds = getDirectoryScanner(srcDir);
      String[] files = ds.getIncludedFiles();
      scanDir(srcDir, files);
    }

    wurbelize();
  }


  /**
   * Extracts all ant-properties to make them available in wurblet-args.
   */
  protected void exportProperties() {
    Properties props = new Properties();
    Hashtable<?,?> antProps = getProject().getProperties();
    Enumeration<?> keys = antProps.keys();
    while (keys.hasMoreElements())  {
      Object key = keys.nextElement();
      if (key instanceof String)  {
        Object value = antProps.get(key);
        if (value instanceof String)  {
          props.setProperty((String)key, (String)value);
        }
      }
    }
    WurbelHelper.setExtraProperties(props);
  }


  /**
   * Clears the list of files to be compiled and copied.
   */
  protected void resetFileLists() {
    wurbleList = new File[0];
  }



  /**
   * Checks if given filename applies.<br>
   * By default java and wurb-files are ok.
   *
   * @param srcDir the source directory
   * @param fileName the file to check
   * @return the returned filename if ok, null if skip this file
   */
  protected String checkFileName(File srcDir, String fileName) {
    if (fileName.endsWith(Wurbler.WURBLET_PROPERTIES_EXTENSION)) {
      // replace filetype with java
      return srcDir.getPath() + File.separator + fileName.substring(0, fileName.length() - 5) + Constants.JAVA_SOURCE_EXTENSION;
    }
    else if (fileName.endsWith(Constants.JAVA_SOURCE_EXTENSION)) {
      return srcDir.getPath() + File.separator + fileName;
    }
    return null;
  }



  /**
   * Scans the directory looking for source files to be wurbelized.
   *
   * @param srcDir the source directory
   * @param files the array of filenames
   */
  protected void scanDir(File srcDir, String[] files) {

    List<File> wurbFiles = new ArrayList<>();

    for (String file1 : files) {
      String fileName = checkFileName(srcDir, file1);
      if (fileName != null) {
        // add if file exists
        File file = new File(fileName);
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
   * Gets the list of files to be wurbelized.
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
   * Perform the wurbelization.
   */
  protected void wurbelize() {

    int phase = 1;
    TreeSet<Integer> collectedPhases = new TreeSet<>();

    for (;;) {

      boolean hereDocsUpdated = true;

      while (hereDocsUpdated) {

        hereDocsUpdated = false;
        HeapFileFactory.initialize();

        int wurbeledFilesCount  = 0;
        int wurbletCount        = 0;
        int updatedFilesCount   = 0;
        int errorFilesCount     = 0;

        Set<String> errorsSoFar = new HashSet<>();


        for (File file : wurbleList) {

          if (runOnce && !filesDone.add(file.getAbsolutePath())) {
            continue;   // skip
          }

          try {
            wurbeledFilesCount++;

            WurbelResult result = new SourceWurbler (
                file.getPath(), null, getWurbletPathArray(), analyzeDir, phase, null, dryRun,

                new AntLogger(getProject()) {
                  @Override
                  public void info(String msg) {
                    AntWurbler.this.log(msg, Project.MSG_INFO);
                  }
                },
                verbosity, errorsSoFar, collectedPhases) {

              /**
               * gets the property value for a given namespace and key.
               * Overwritten from AbstractWurbler to set "ant" synonym for "extra".
               * Just to make it easier to remember... ;-)
               *
               * @param nameSpace use null for the default java-properties.
               * @return the value for key, null if no such key
               */
              @Override
              public String getProperty(String nameSpace, String key) {
                if (nameSpace != null && nameSpace.equals("ant")) {
                  return WurbelHelper.getExtraProperties().getProperty(key);
                }
                return super.getProperty(nameSpace, key);
              }

            }.wurbel();

            wurbletCount += result.wurbletCount();

            if (result.errors() > 0) {
              errorFilesCount++;
              log(file.getName() + " generated with wurblet errors (" + result.errors() + ")", Project.MSG_WARN);
            }
            else {
              if (result.updateType() == WurbelResult.UpdateType.UPDATED) {
                updatedFilesCount++;
                // file has been modified without errors
                if (verbosity.isDebug()) {
                  log(">>> " + file.getName() + " <<< UPDATED!");
                }
                else if (verbosity.isInfo()) {
                  log(file.getName());
                }
              }
              else if (result.updateType() == WurbelResult.UpdateType.HEREDOCS) {
                hereDocsUpdated = true;
              }
            }
          }

          catch (WurbelException | SourceException | RuntimeException ex)  {
            String msg = ex instanceof SourceException ? "FAILED" : file.getName();
            if (printStackTrace) {
              log(msg, ex, Project.MSG_ERR);
            }
            else  {
              log(msg + ": " + ex, Project.MSG_ERR);
            }
          }
        }

        if (!hereDocsUpdated) {
          log("phase " + phase + ", " +
              wurbletCount + " wurblets processed, " +
              wurbeledFilesCount + " files wurbeled, " +
              updatedFilesCount + " updated, " +
              errorFilesCount + " errors");

          if (errorFilesCount > 0) {
            if (failOnError) {
              throw new BuildException(FAIL_MSG, getLocation());
            }
            else {
              log(FAIL_MSG, Project.MSG_ERR);
            }
          }
        }
      }

      Integer nextPhase = collectedPhases.higher(phase);
      if (nextPhase == null) {
        break;
      }
      phase = nextPhase;
    }
  }


  /**
   * Gets the wurblet path.
   *
   * @return the wurbletpath String[]-array (this is not an ANT-arg!)
   */
  public String[] getWurbletPathArray()  {
    return wurbletPath;
  }


  /**
   * Gets the wurblet path as a colon-separated string.
   *
   * @return the wurbletpath
   */
  public String getWurbletpath() {
    StringBuilder path = new StringBuilder();
    if (wurbletPath != null) {
      boolean first = true;
      for (String s: wurbletPath) {
        if (first)  {
          first = false;
        }
        else  {
          path.append(':');
        }
        path.append(s);
      }
    }
    return path.toString();
  }


  /**
   * Sets the package names to load the wurblets.
   * The path is a colon-separated list of strings,
   * e.g.: {@code wurbletpath="org.tentacle.wurblets:de.krake.jplsbl.wurblets"}
   *
   * @param path colon-separated list of package names
   */
  public void setWurbletpath(String path) {
    List<String> pathList = new ArrayList<>();
    StringTokenizer stok = new StringTokenizer(path, " \t\r;:");
    while (stok.hasMoreTokens())  {
      pathList.add(stok.nextToken());
    }
    if (pathList.isEmpty()) {
      wurbletPath = null;
    }
    else  {
      wurbletPath = new String[pathList.size()];
      pathList.toArray(wurbletPath);
    }
  }

}
