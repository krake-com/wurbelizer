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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;
import org.wurbelizer.misc.Constants;
import org.wurbelizer.misc.Logger;
import org.wurbelizer.misc.Settings;
import org.wurbelizer.misc.Verbosity;



/**
 * A wurblet container.<br>
 * Reads a source file, scans for &#64;wurblets and sub elements and applies them
 * as long as there is no more change to the source code.
 * The {@code SourceWurbler} usually is invoked from within the {@code AntWurbler}.
 * but may also be invoked from the command line (for testing purposes).
 *
 * @author harald
 */

public class SourceWurbler extends AbstractWurbler {

  /**
   * Registered source types.<br>
   * Files not registered will not be processed.
   */
  private static final Map<String,SourceType> SOURCE_TYPES = new TreeMap<>();

  /**
   * Registers a source type.<br>
   *
   * @param type the source type
   */
  public static void registerSourceType(SourceType type) {
    SOURCE_TYPES.put(type.getFileExtension(), type);
  }



  private static final String PROPERTY_GUARDTYPE = "guardtype";
  private static final String PROPERTY_FOLDTYPE  = "foldtype";


  private final String filename;                    // the filename to wurbelize
  private final String[] wurbletPath;               // paths to load wurblets from
  private final String analyzeDir;                  // directory with extra wurblet information
  private final Logger logger;                      // the logger
  private final Verbosity verbosity;                // verbosity
  private final Set<String> errorsSoFar;            // old error message list
  private final boolean dryRun;                     // true if dry run (no wurblets, just parsing)

  private SourceElement.Type guardType;             // the guard type
  private SourceDocument.FoldType defaultFoldType;  // the default fold type (null if none)
  private Properties wurbProps;                     // properties from wurb-file
  private int loopCount;                            // loop counter
  private SourceType sourceType;                    // type of this source file



  /**
   * Creates a {@code SourceWurbler}.
   *
   * @param filename the full pathname of the java-file
   * @param wurbFilename optional wurblet properties file, null to check in same dir
   * @param wurbletPath the list of package names to load wurblets from (if wurblet name is not absolute)
   * @param analyzeDir the list of directories wurblets load extra information from (usually from apt-runs)
   * @param verbosity the verbosity
   * @param logger the logger (can also be used by the wurblets)
   * @param errorsSoFar a set of errors logged so far, null if no suppression of repeating errors
   * @param dryRun true if don't run any wurblets, just parse the source file
   * @throws WurbelException if loading the wurb-file failed
   */

  public SourceWurbler(String filename, String wurbFilename, String[] wurbletPath, String analyzeDir,
                       Logger logger, Verbosity verbosity, Set<String> errorsSoFar, boolean dryRun)
         throws WurbelException {

    this.filename       = filename;
    this.wurbletPath    = wurbletPath;
    this.analyzeDir     = analyzeDir;
    this.logger         = logger;
    this.verbosity      = verbosity == null ? Verbosity.DEFAULT : verbosity;
    this.errorsSoFar    = errorsSoFar;
    this.dryRun         = dryRun;


    File file = new File(filename);
    if (!file.exists()) {
      throw new WurbelException("no such file: " + filename);
    }
    if (!file.isFile()) {
      throw new WurbelException("not a file: " + filename);
    }

    // determine source type
    int ndx = filename.lastIndexOf('.');
    if (ndx < 0) {
      throw new WurbelException("cannot determine source type from filename: " + filename);
    }
    String fileExtension = filename.substring(ndx);

    sourceType = SOURCE_TYPES.get(fileExtension);
    if (sourceType == null) {
      throw new WurbelException("unknown file extension: " + fileExtension);
    }

    wurbProps = new Properties();

    // extract names
    ndx = filename.lastIndexOf(File.separatorChar);
    String dirname = ndx > 0 ? filename.substring(0, ndx) : ".";
    String basename = ndx > 0 && filename.length() > ndx ? filename.substring(ndx + 1) : filename;
    ndx = basename.lastIndexOf('.');
    if (ndx > 0)  {
      basename = basename.substring(0, ndx);
    }

    if (wurbFilename == null) {
      wurbFilename = dirname + File.separator + basename + Wurbler.WURBLET_PROPERTIES_EXTENSION;
    }

    file = new File(wurbFilename);
    if (file.exists()) {
      if (!file.isFile()) {
        throw new WurbelException("not a file: " + wurbFilename);
      }
      wurbProps.setProperty(WURBLET_WURBNAME, wurbFilename);
    }
    else  {
      wurbFilename = null;
    }


    // add names as properties
    wurbProps.setProperty(WURBLET_FILENAME, filename);    // full pathname
    wurbProps.setProperty(WURBLET_DIRNAME, dirname);      // directory name

    // get the guarded type
    guardType = SourceElement.Type.GUARDED_OTHER;    // default type

    // settings from extra properties
    Properties extraProps = WurbelHelper.getExtraProperties();
    if (extraProps != null) {
      setGuardType(extraProps.getProperty(PROPERTY_GUARDTYPE));
      setDefaultFoldType(extraProps.getProperty(PROPERTY_FOLDTYPE));
    }

    // load wurbfile, if any
    if (wurbFilename != null) {
      try (BufferedReader in =
              new BufferedReader(new InputStreamReader(new FileInputStream(wurbFilename), Settings.encodingCharset))) {
        for (;;) {
          String line = in.readLine();
          if (line == null) {
            break;
          }
          processPropertyLine(line);
        }
      }
      catch (IOException ex) {
        throw new WurbelException("cannot load wurblet properties file: " + wurbFilename, ex);
      }
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


  @Override
  public int getInvocationCount() {
    return loopCount;
  }


  @Override
  public File getAnalyzeFile(String name) {
    return new File(analyzeDir, name);
  }


  /**
   * Processes the source file, create here-docs and replace wurblet-guarded sections.
   *
   * @return number of errors if &gt; 0,<br>
   *         {@link #RC_HEREDOCS} = heredocs written and file not modified,<br>
   *         {@link #RC_UPDATED} = file updated without errors, 0 = no change
   *
   * @throws WurbelException if a file- or heapfile operation failed
   * @throws SourceException if source scanning failed
   */
  @Override
  public int wurbel() throws WurbelException, SourceException {

    SourceFileFactory.initialize();

    if (verbosity.isDebug())  {
      logger.info("--- " + filename + " ---");
    }

    // read the source file according to the systems encoding
    SourceFile sourceFile = SourceFileFactory.open(filename);
    String text = sourceFile.getOrgText();

    int errors = 0;                     // > 0: some error during running of wurblets
    boolean updated = false;            // true if updated
    boolean hereDocsWritten = false;    // true if heredocs written

    for (loopCount=1;;loopCount++) {   // until no more changes

      clearInvokedWurblets();

      // setup and scan document
      SourceDocument doc = new SourceDocument(text, filename, wurbProps, logger, verbosity);

      // extract some additional properties if this is a java file
      for (;;) {
        boolean foundProps = false;
        Properties sourceProps = sourceType.extractProperties(doc);
        for (String key: sourceProps.stringPropertyNames()) {
          String value = sourceProps.getProperty(key);
          Object oldValue = wurbProps.setProperty(key, value);
          if (oldValue == null || !(oldValue instanceof String) || !oldValue.equals(value)) {
            foundProps = true;
            if (verbosity.isDebug()) {
              logger.info(key + ": " + value);
            }
          }
        }
        if (foundProps) {
          // we need to set it up again because the properties from the
          // source may have an influence on the extracted elements (especially replaced args)
          doc = new SourceDocument(text, filename, wurbProps, logger, verbosity);
        }
        else  {
          break;  // no more properties changed
        }
      }

      // to check that all guarded sections have unique names
      Set<String> guardedNames = new HashSet<>();

      String orgText = doc.getText().toString();    // save original text

      // go through all sections and apply wurblets (we cant use iterator)
      for (int elementIndex=0; elementIndex < doc.getElements().size(); elementIndex++)  {

        SourceElement element = doc.getElements().get(elementIndex);

        // notice: property docs already applied in SourceElement.scan()

        if (element.containsHereDocuments())  {
          for (SourceHereSubElement hereDoc: element.getHereDocuments()) {
            String hereName = WurbelHelper.translateVariables(hereDoc.getName(), wurbProps);
            String hereText = WurbelHelper.translateVariables(hereDoc.getText(), wurbProps);
            if (hereName.charAt(0) == '.') {
              // in memory document (only in first loop!)
              if (loopCount == 1) {
                String heapFileName = hereName.substring(1);
                HeapFileFactory.createOrOverwrite(filename, heapFileName, hereText);
                if (verbosity.isDebug()) {
                  logger.info("heapfile '" + heapFileName + "' created");
                }
              }
            }
            else  {
              if (verbosity.isDebug()) {
                logger.info("[" + loopCount + "]file: " + hereName);
              }
              SourceFile hereDocFile = SourceFileFactory.open(hereName);
              String orgHereText = hereDocFile.getOrgText();
              if (orgHereText == null || !orgHereText.equals(hereText)) {
                // check that both have the same origin, if origin info provided
                if (orgHereText != null && orgHereText.startsWith(Constants.ORIGIN_INFO_LEAD) &&
                    hereText.startsWith(Constants.ORIGIN_INFO_LEAD)) {
                  String orgHereInfo = WurbelHelper.firstLine(orgHereText);
                  String hereInfo = WurbelHelper.firstLine(hereText);
                  if (!orgHereInfo.equals(hereInfo)) {
                    throw new WurbelException("heredoc '" + hereName + "' created from more than one sources:\n" +
                                   orgHereInfo.substring(2) + " !=\n" + hereInfo.substring(2));
                  }
                }
                hereDocFile.getStream().print(hereText);
                // heredoc originally present in source created (i.e. not a new heredoc created by a wurblet)
                if (verbosity.isDebug()) {
                  logger.info("heredoc '" + hereName + "' updated");
                }
                hereDocsWritten = true;
              }
              else if (verbosity.isDebug()) {
                logger.info("heredoc '" + hereName + "' unchanged");
              }
              hereDocFile.flush();   // flush if changed in case other parts read from this file
            }
          }
        }

        if (!dryRun) {
          if (hereDocsWritten) {
            // some heredocs updated: process wurblets later
            break;
          }

          if (element.containsWurblets()) {
            // process wurblets in reverse order
            List<SourceWurbletSubElement> wurbletList = element.getWurblets();
            for (int wurbletIndex = wurbletList.size() - 1; wurbletIndex >= 0; wurbletIndex--) {

              SourceWurbletSubElement wurblet = wurbletList.get(wurbletIndex);

              // parse container options (wurbler args)
              int indent = 0;                                       // extra indentation
              boolean invoke = true;                                // run the wurblet
              SourceDocument.FoldType foldType = defaultFoldType;   // the fold type (default none)

              // parse wurbler args
              for (String warg: wurblet.getWurblerArgs()) {
                if (warg.startsWith("indent=")) {
                  indent = Integer.parseInt(warg.substring(7));
                }
                else if (warg.startsWith("test:")) {
                  invoke = simpleCondition(warg.substring(5));
                }
                else if (warg.startsWith("fold=")) {
                  String val = warg.substring(5).toUpperCase();
                  if (!val.equals("NONE")) {
                    foldType = SourceDocument.FoldType.valueOf(val);
                  }
                }
              }

              ByteArrayOutputStream os = new ByteArrayOutputStream(); // holds the data
              PrintStream ps = new PrintStream(os);

              try {

                String currentGuardName = wurblet.getName();

                if (!wurblet.isInline() && !guardedNames.add(currentGuardName)) {
                  throw new WurbelException("guarded name '" + currentGuardName + "' not unique");
                }

                wurbProps.setProperty(WURBLET_GUARDNAME, currentGuardName);
                String wurbletName = wurblet.getWurbletName();
                wurbProps.setProperty(WURBLET_WURBLETNAME, wurbletName);

                if (verbosity.isDebug()) {
                  logger.info("[" + loopCount + "]wurblet: " + currentGuardName + ", " + wurbletName);
                }

                // load the wurblet (may throw an Exception, but not a WurbelException)
                loadWurblet(wurbletName, wurbletPath, wurbProps);

                // get arguments if not fixed
                if (!isFixedArgs()) {
                  setArgs(wurblet.getWurbletArgs());
                }
                setPrintStream(ps);

                if (invoke) {
                  runWurblet();    // run the wurblet. Errors are catched as exceptions!
                }
                else  {
                  ps.println("// condition not met");
                }
              }
              catch (Exception ex)  {
                errors++;
                // log only exceptions that did not already show up (avoids cluttering output)
                String errMsg = new File(filename).getName() + ":[" + doc.getLineNumber(wurblet.getWurbletBegin()) + "] " +
                                ExceptionHelper.concatenateMessages(ex);
                boolean doLog = true;
                if (errorsSoFar != null) {
                  doLog = !errorsSoFar.contains(errMsg);
                  if (doLog) {
                    errorsSoFar.add(errMsg);
                  }
                }
                if (doLog) {
                  logger.error(errMsg);
                }

                // place exceptions in comments in output
                if (wurblet.isInline()) {
                  // print only a single line as newlines would break the insertion point
                  ps.print(" { " + ex.getMessage() + " } ");
                }
                else  {
                  ps.println("/********");
                  ex.printStackTrace(ps);
                  ps.println("********/");
                }
              }

              // build generated text
              String srcText = os.toString();

              // add fixed indent if option set
              srcText = WurbelHelper.indent(srcText, indent);

              // replace and if no success delete
              if (wurblet.isInline()) {
                // inline wurblet: replace text between two consecutive /**/
                // within the next source element
                doc.replaceInline(srcText, elementIndex, wurblet);
              }
              else  {
                // generate text into guarded section
                if (doc.replaceGuarded(guardType, foldType, wurblet.getName(), srcText, wurblet) == -1) {
                  // insert the wurblet output
                  doc.insertGuarded(guardType, foldType, wurblet.getName(), srcText, elementIndex + 1, wurblet);
                }
              }
            }
          }
        }
      }

      if (dryRun || hereDocsWritten) {
        break;
      }

      // go through all sections and remove guarded blocks without a wurblet directive
      int max = doc.getElements().size();
      for (int i=0; i < max; i++)  {
        SourceElement elem = doc.getElements().get(i);
        if (elem.isGuarded() && !guardedNames.contains(elem.getGuardedName()) &&
            doc.deleteGuarded(elem.getGuardedName(), i) >= 0)  {
          max = doc.getElements().size();
          i--;
        }
      }

      // compare the output with the original (line-endings converted according to source file)
      String newText = sourceFile.convertLineEndings(doc.getText().toString());

      if (newText.equals(orgText)) {
        break;  // nothing updated -> done
      }
      else  {
        // text differs: write output
        updated = true; // file updated
        sourceFile.update(newText);

        if (errors > 0)  {
          break;        // abort process due to errors
        }

        text = newText;  // one more pass to check for more changes
      }

      // rewind all other created sources for next loop
      SourceFileFactory.resetAll();
    }

    if (!dryRun) {
      // write back all other created sources if they differ from original text
      SourceFileFactory.flushAll();
    }

    // run the cleanup method on all wurblets of the last pass
    cleanupInvokedWurblets();

    if (errors == 0) {
      if (hereDocsWritten) {
        return RC_HEREDOCS;
      }
      if (updated) {
        return RC_UPDATED;
      }
      return RC_UNCHANGED;
    }

    return errors;
  }




  /**
   * Checks for equality of a string-pair given by 'expr1 == expr2' or 'expr1 != expr2'.
   *
   * @param condStr the condition
   * @return true if condition is met, false if not
   */
  private boolean simpleCondition(String condStr) {
    boolean invert = false;
    if (condStr != null)  {

      int ndx = condStr.indexOf("==");
      if (ndx <= 0)  {
        ndx = condStr.indexOf("!=");
        invert = true;
      }
      if (ndx > 0) {
        StringTokenizer stok = new StringTokenizer(condStr.substring(0, ndx));
        if (stok.hasMoreTokens()) {
          String str1 = stok.nextToken();
          int len = condStr.length();
          if (ndx + 2 < len)  {
            stok = new StringTokenizer(condStr.substring(ndx + 2, len));
            if (stok.hasMoreTokens()) {
              String str2 = stok.nextToken();
              boolean rv = str1.equals(str2);
              return invert ? !rv : rv;
            }
          }
        }
      }
    }
    return invert;    // condition is not met
  }


  /**
   * Set guardtype from string
   */
  private void setGuardType(String value)  {
    // overwrite guardtype from wurbfile
    if (value != null && value.equalsIgnoreCase("netbeans")) {
      guardType = SourceElement.Type.GUARDED_NETBEANS;
    }
    // else if other guarded types to come...
  }


  /**
   * Set foldtype from string
   */
  private void setDefaultFoldType(String value)  {
    if (value != null) {
      value = value.toUpperCase();
      if (!value.equals("none")) {
        defaultFoldType = SourceDocument.FoldType.valueOf(value);
      }
    }
  }


  /**
   * process property line
   */
  private void processPropertyLine(String line) {
    String key = WurbelHelper.scanPropertyLine(line, wurbProps);
    if (key != null && key.equals(PROPERTY_GUARDTYPE)) {
      // overwrite guardtype from wurbfile
      setGuardType(wurbProps.getProperty(PROPERTY_GUARDTYPE));
    }
  }

}
