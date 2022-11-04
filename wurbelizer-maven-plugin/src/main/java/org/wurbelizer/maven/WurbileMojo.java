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


package org.wurbelizer.maven;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.shared.model.fileset.FileSet;
import org.apache.maven.shared.model.fileset.util.FileSetManager;
import org.slf4j.LoggerFactory;
import org.wurbelizer.misc.Constants;
import org.wurbelizer.misc.Logger;
import org.wurbelizer.misc.Verbosity;
import org.wurbelizer.wurbel.WurbelHelper;
import org.wurbelizer.wurbile.SourceWurbiler;
import org.wurbelizer.wurbile.WurbileException;
import org.wurbelizer.wurbile.Wurbiler;

import java.io.File;
import java.io.IOException;

/**
 * Wurbile Wurblets.<br>
 * The wurblet sources (wrbl-files) are translated to Java-sources
 * and ser-files (serialized fixed text) and then compiled to Java class files.<br>
 * Those class files can later be instantiated as code generators by the Wurbler.<br>
 * See the wurbelizer:wurbel goal.
 *
 * @author harald
 */
@Mojo(name = "wurbile",
      defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class WurbileMojo extends AbstractWurbelizerMojo {


  /**
   * Directory holding the wurblet sources.
   */
  @Parameter(defaultValue = "${project.build.sourceDirectory}/../wurblets",
             property = "wurbile.sourceDir",
             required = true)
  private File wurbletSourceDir;

  /**
   * Directory for the generated wurblet Java sources.
   */
  @Parameter(defaultValue = "${project.build.directory}/generated-sources/wurblets",
             property = "wurbile.javaDir")
  private File wurbletTargetJavaDir;

  /**
   * Directory for the generated wurbley fixed text sources.
   */
  @Parameter(defaultValue = "${project.build.directory}/classes",
             property = "wurbile.textDir")
  private File wurbletTargetTextDir;

  /**
   * Sets the indent of the generated Java source.
   */
  @Parameter(defaultValue = "-1")
  private Integer indent;



  /**
   * Total number of errors
   */
  protected int totalErrors;


  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {

    // validate configuration
    validate();

    // set up the file charset
    determineEncoding();

    // get the verbosity
    determineVerbosity();

    // process properties
    exportExtraProperties(createExtraProperties());

    // add generated sources
    project.addCompileSourceRoot(wurbletTargetJavaDir.getAbsolutePath());

    totalErrors = 0;

    if (filesets != null && !filesets.isEmpty()) {
      // explicit filesets given instead of wurblet source dir
      for (FileSet fileSet: filesets) {
        wurbileFileSet(fileSet);
      }
    }
    else {
      // all from wurblet source dir with default extension
      if (wurbletSourceDir.isDirectory()) {
        String[] files = wurbletSourceDir.list();
        if (files != null && files.length > 0) {
          final FileSet fs = new FileSet();
          fs.setDirectory(wurbletSourceDir.getPath());
          fs.addInclude("**/*" + Constants.WURBLET_SOURCE_EXTENSION);
          wurbileFileSet(fs);
        }
        else {
          getLog().warn("empty wurbile.sourceDir " + wurbletSourceDir.getAbsolutePath());
        }
      }
      else {
        getLog().warn("missing wurbile.sourceDir " + wurbletSourceDir.getAbsolutePath());
      }
    }

    if (totalErrors > 0) {
      throw new MojoFailureException(totalErrors + " wurbile errors");
    }
  }



  /**
   * Creates an instance of a wurbiler.
   *
   * @param wrblFile the input wurbelizer file
   * @param outputJavaDir the output directory getting the Java-files
   * @param outputTextDir the output directory getting the fixed text .ser-files.
   * @param logger the logger
   * @param verbosity the verbosity
   * @return the wurbiler
   * @throws IOException if source file could not be loaded
   */
  protected Wurbiler createWurbiler(File wrblFile, File outputJavaDir, File outputTextDir, Logger logger, Verbosity verbosity)
         throws IOException {
    return new SourceWurbiler(wrblFile, outputJavaDir, outputTextDir, logger, verbosity);
  }


  /**
   * Wurbiles all wurblets in a fileset.
   *
   * @param fileSet the set of files
   * @throws MojoExecutionException if compilation failed
   */
  private void wurbileFileSet(FileSet fileSet) throws MojoExecutionException {

    int wurbiledFilesCount  = 0;
    int errorFilesCount     = 0;

    if (fileSet.getDirectory() == null) {
      // directory missing: use sourceDir as default
      fileSet.setDirectory(wurbletSourceDir.getAbsolutePath());
    }

    String dirName;
    try {
      dirName = new File(fileSet.getDirectory()).getCanonicalPath();
    }
    catch (IOException ex) {
      throw new MojoExecutionException("cannot determine canonical path of " + fileSet.getDirectory(), ex);
    }

    if (verbosityLevel.isDebug()) {
      getLog().info("wurbiling files in " + dirName);
    }

    for (String fileName: new FileSetManager(LoggerFactory.getLogger(FileSetManager.class), verbosityLevel.isDebug()).getIncludedFiles(fileSet)) {

      try {

        File wurbletFile = new File(dirName + File.separator + fileName);

        // set per file extra properties (e.g. for include processing)
        WurbelHelper.getExtraProperties().setProperty("currentDir", wurbletFile.getParent());
        WurbelHelper.getExtraProperties().setProperty("currentFile", wurbletFile.getName());

        // create wurbiler
        Wurbiler wurbiler = createWurbiler(
                wurbletFile, wurbletTargetJavaDir, wurbletTargetTextDir, new MavenLogger(getLog()), verbosityLevel);

        // set options
        if (indent >= 0)         {
          wurbiler.setIndent(indent);
        }

        wurbiledFilesCount++;

        if (verbosityLevel.isDebug()) {
          getLog().info("------ " + fileName + " ------");
        }

        // run the wurbiler
        int errors = wurbiler.compile();

        if (errors == 0) {
          if (verbosityLevel.isInfo() && !verbosityLevel.isDebug()) {
            // file has been compiled without errors
            getLog().info(fileName);
          }
        }
        else  {
          errorFilesCount++;
          getLog().error(fileName + ": " + errors + " wurbile errors");
        }
      }
      catch (IOException | WurbileException | RuntimeException ex) {
        totalErrors += errorFilesCount + 1;
        throw new MojoExecutionException("unable to wurbile source " + fileName, ex);
      }
    }

    totalErrors += errorFilesCount;

    getLog().info(getPathRelativeToBasedir(new File(dirName).getAbsolutePath()) + ": " +
                  wurbiledFilesCount + " files wurbiled, " + errorFilesCount + " errors");
  }


  /**
   * Checks mandatory params.<br>
   *
   * @throws MojoExecutionException if validation fails
   */
  private void validate() throws MojoExecutionException {
    if (wurbletSourceDir == null) {
      throw new MojoExecutionException("missing wurbile.sourceDir");
    }
    if (wurbletTargetJavaDir == null) {
      throw new MojoExecutionException("missing wurbile.javaDir");
    }
    if (wurbletTargetTextDir == null) {
      throw new MojoExecutionException("missing wurbile.textDir");
    }
  }

}
