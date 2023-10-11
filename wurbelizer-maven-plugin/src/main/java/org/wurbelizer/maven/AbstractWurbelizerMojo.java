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

import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.model.fileset.FileSet;
import org.wurbelizer.misc.Settings;
import org.wurbelizer.misc.Verbosity;
import org.wurbelizer.wurbel.WurbelHelper;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

/**
 * Base class of wurbelizer mojos.
 *
 * @author harald
 */
public abstract class AbstractWurbelizerMojo extends AbstractMojo {

  /**
   * The Maven Project Object.
   */
  @Parameter(defaultValue = "${project}",
             required = true,
             readonly = true)
  protected MavenProject project;

  /**
   * The maven session.
   */
  @Parameter(defaultValue = "${session}",
             required = true,
             readonly = true)
  protected MavenSession session;

  /**
   * The list of file sets to wurbile.<br>
   * If set the source directory is ignored.
   */
  @Parameter
  protected List<FileSet> filesets;

  /**
   * The verbosity.<br>
   * One of "default", "info" or "debug".
   * Debus is also turned on (if not set explicitly) by Maven's global debug flag (see command line switch <code>-X</code>).
   */
  @Parameter(defaultValue = "${wurbel.verbosity}")
  protected String verbosity;

  /**
   * The charset used to read and write files.
   */
  @Parameter(defaultValue = "${project.build.sourceEncoding}")
  protected String charset;   // don't use the name "encoding" as this is handled by maven

  /**
   * Skips processing.<br>
   * Defaults to true if packaging is "pom".
   */
  @Parameter
  protected Boolean skip;

  /**
   * mapped verbosity level
   */
  protected Verbosity verbosityLevel;

  /**
   * List of resource dirs.
   */
  protected List<String> resourceDirs;



  /**
   * Determines the charset.
   */
  protected void determineEncoding() {
    if (charset != null) {
      Settings.setEncodingCharset(Charset.forName(charset));
    }
  }


  /**
   * sets the verbosity
   */
  protected void determineVerbosity() {
    if (verbosity != null) {
      try {
        verbosityLevel = Verbosity.valueOf(verbosity.toUpperCase(Locale.ROOT));
      }
      catch (RuntimeException ex) {
        verbosityLevel = Verbosity.DEFAULT;
      }
    }
    else  {
      // use maven's global setting
      verbosityLevel = getLog().isDebugEnabled() ? Verbosity.DEBUG : Verbosity.DEFAULT;
    }
  }


  /**
   * Gets the path relative to the basedir.<br>
   * Parent dirs of the basedir will also be tried.
   *
   * @param path the absolute path
   * @return the shortened path
   */
  protected String getPathRelativeToBasedir(String path) {
    String basePath = project.getBasedir().getAbsolutePath();
    while (!basePath.isEmpty()) {
      if (path.startsWith(basePath)) {
        int len = basePath.lastIndexOf(File.separatorChar);
        if (len > 0) {
          len++;    // include the last dir of the basepath
        }
        else  {
          len = basePath.length() + 1;
        }
        return path.substring(len);
      }
      // continue with the parent dir
      int ndx = basePath.lastIndexOf(File.separatorChar);
      if (ndx > 0) {
        basePath = basePath.substring(0, ndx);
      }
      else  {
        break;
      }
    }
    return path;
  }


  /**
   * Finds all resource directories.
   */
  protected void findResourceDirs() {
    resourceDirs = new ArrayList<>();
    for (Resource resource: project.getBuild().getResources()) {
      String name = resource.getDirectory();
      if (name != null) {
        resourceDirs.add(name);
      }
    }
  }


  /**
   * Checks if given directory resides in resources.
   *
   * @param dirName the directory name
   * @return the resource dir, null if not a resource
   */
  protected String getResourceDirName(String dirName) {
    if (resourceDirs != null) {
      for (String resourceDir: resourceDirs) {
        if (dirName.startsWith(resourceDir)) {
          return resourceDir;
        }
      }
    }
    return null;
  }


  /**
   * Creates the extra properties.
   *
   * @return the properties
   */
  protected Properties createExtraProperties() {
    Properties props = new Properties();
    props.setProperty("project", project.getBasedir().getPath());
    // create parentProject, parentParentProject, etc...
    MavenProject parentProject = project.getParent();
    StringBuilder projectKey = new StringBuilder();
    while (parentProject != null) {
      File baseDir = parentProject.getBasedir();
      if (baseDir == null) {
        getLog().warn("parent project " + parentProject.getName() + " not in filesystem");
        break;
      }
      projectKey.append("Parent");
      props.setProperty(Character.toLowerCase(projectKey.charAt(0)) + projectKey.substring(1) + "Project",
                        baseDir.getPath());
      parentProject = parentProject.getParent();
    }
    return props;
  }


  /**
   * Make extra properties available.
   *
   * @param props the properties
   */
  protected void exportExtraProperties(Properties props) {

    WurbelHelper.setExtraProperties(props);

    if (verbosityLevel.isDebug()) {
      Enumeration<?> keys = props.keys();
      while (keys.hasMoreElements())  {
        Object key = keys.nextElement();
        if (key instanceof String)  {
          getLog().info(key + " = " + props.get(key));
        }
      }
    }
  }

  /**
   * Returns whether the execution should be skipped.
   *
   * @return true to do nothing
   */
  protected boolean isSkipped() {
    if (skip == null && "pom".equals(project.getPackaging())) {
      skip = true;    // pom artifacts are skipped by default
    }
    if (Boolean.TRUE.equals(skip)) {
      getLog().info("skipped");
      return true;
    }
    return false;
  }

}
