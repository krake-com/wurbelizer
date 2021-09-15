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


package org.wurbelizer.wurbile;

import org.wurbelizer.misc.Logger;
import org.wurbelizer.misc.Verbosity;
import org.wurbelizer.wurbel.WurbelHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;


/**
 * Provides basic functionality of a wurblet compiler.<br>
 *
 * Translates an input stream (usually a file with extension *.wrbl)
 * into a an output stream and a fixed source list.
 * The generated java-output implements the interface {@code Wurblet}.
 * The fixed source part of the wurblet is stored in a {@code List} of Strings which usually is
 * stored in serialized form in a separate file and will be deserialized by the wurbler
 * when running the wurblet.<p>
 *
 * By default the input stream starts at the source-level.
 * The backslash is recognized as wurblet-level is entered via {@code '@['} and left via {@code ']@'}.
 * Wurblet-levels again may contain other wurblet-levels, i.e. they may be nested
 * resulting in a wurblet generating a wurblet which in turn generates java source
 * or even another wurblet.<p>
 *
 * As a shorthand for {@code @[out.print(object);]@} the wurbiler understands the
 * notation {@code @(object)@}.
 * This may be even used in a string (e.g.: {@code "This is line @(line)@\n");}).<p>
 *
 * The backslash {@code '\'} is recognized as an escape char.
 * If followed by a {@code '@'}, the backslash will be consumed and the @ passed to the source.
 * Other pairs of escapes will be passed unprocessed (e.g. {@code '\n'} or whatever).<p>
 *
 * Commands may also be embedded at the source-level via {@code @{command par1 par2 ... parN}@}.
 * The following commands are supported:<p>
 *
 * {@code @{include <pathname>}@}: includes a file in the current input stream.
 * Includes may be nested to any depth. Loops are detected.
 * the include pathname allows $-variables that will be replaced
 * at compile time.<p>
 *
 * {@code @{package <packagename>}@}: sets the package-name for the generated class.<p>
 *
 * {@code @{import <classpath>}@}: adds to the extra imports.<p>
 *
 * {@code @{extends <parentclass>}@}  sets the parent-class (default is AbstractWurblet).<p>
 *
 * {@code @{implements <if1> ... <ifN>}@}  implements interfaces.<p>
 *
 * {@code @{indent <spaces>}@}: sets the fixed source indentation.
 * "auto" enables autoindent., i.e. {@code @{indent auto}@}.
 * Autoindent is the default.<p>
 *
 * {@code @{args <arg1> <arg2> ...}@}: presets the wurblet arguments (default is from container, i.e. wurbler)
 * notice that {@code @{args}@} without any arguments enables container args again.
 * This is especially useful for @{including} serveral wurblets in a single source-file.
 * Thus, if the wurblet needs args, it's a good practice to include an @{args}-line
 * in the wurblet-code.<p>
 *
 * {@code @{phase N}@}: sets the execution phase of the wurblet. Default is 1.<p>
 *
 * {@code @{to <outputstream>}@}: sets the output stream (this is the stream the wurblet
 * will write the generated code to, not the output writer of the compiled wurblet!).
 * {@code @{to out}} resets to the default stream, which is equivalent to {@code @{to}}.<p>
 *
 * {@code @{comment ..... }@}: adds the text to the wurblet comment<p>
 *
 * {@code @{code ...}@}: adds the code to the class level code. This code is appended to the
 * end of the wurblet's java code and can be used to override or implement wurblet methods.
 *
 * @author harald
 */

public abstract class AbstractWurbiler implements Wurbiler {

  private static final String OUTNAME      = "out";      // default for outName
  private static final int WURBLET_NONE    = 0;          // wurblet mode not entered yet
  private static final int WURBLET_BLOCK   = '[';        // in wurblet block
  private static final int WURBLET_OBJECT  = '(';        // in wurblet object
  private static final int WURBLET_COMMAND = '{';        // in wurblet command

  private static final String CMD_INCLUDE     = "include";
  private static final String CMD_PACKAGE     = "package";
  private static final String CMD_EXTENDS     = "extends";
  private static final String CMD_IMPLEMENTS  = "implements";
  private static final String CMD_IMPORT      = "import";
  private static final String CMD_INDENT      = "indent";
  private static final String CMD_TO          = "to";
  private static final String CMD_ARGS        = "args";
  private static final String CMD_COMMENT     = "comment";
  private static final String CMD_PHASE       = "phase";
  private static final String CMD_CONFIG      = "config";
  private static final String CMD_CODE        = "code";



  private String        packageName;        // package name from @{package}@ command
  private Set<String>   imports;            // all imports form @{import}@ commands
  private String        parentClass;        // extends parentclass from @{extends}@ command, null = default
  private Set<String>   interfaces;         // collection of @{implements}@, null = default
  private String[]      args;               // wurblet arguments from @{args ...}@ command, null = default,
                                            // i.e. from command line, @wurblet, etc...
  private Integer       phase;              // the execution phase if != null
  private StringBuilder comment;            // the wurblet comment, null if none
  private StringBuilder code;               // extra top level code, null if none
  private StringBuilder configuration;      // generic wurblet options

  private final List<NestedReader> readers; // Reader list (active reader is always at start of list)
  private final Set<String> paths;          // nested paths to detect include-loops
  private NestedReader  in;                 // current reader (always readers.get(0))
  private final Writer  out;                // output stream
  private List<String>  sourceList;         // list of fixed source-strings
  private String        outName;            // name of output stream
  private boolean       startAutoIndent;    // true if newline found in autoindent processing
  private boolean       inAutoIndent;       // true = in leading space string processing
  private boolean       lastWasOpenBlock;   // true = last line ends with '{' (used in autoindent processing)
  private int           autoIndent;         // if >= 0, indent is determined automatically
  private int           indent;             // length of indent generated code for writing source-strings
  private String        indentStr;          // indent string (blanks)

  private StringBuilder sourceBuf;          // buffer for current source string
  private String        sourceLoc;          // source location (hint in generated source)
  private int           errors;             // number of errors
  private Logger        logger;             // logger for errors and warnings
  private Verbosity     verbosity;          // the verbosity
  private int           wurbletMode;        // current or previous wurblet mode: one of WURBLET_...



  /**
   * Creates a compiler instance.
   *
   * @param in the input reader
   * @param inName the name of the input stream (for diagnostics only)
   * @param out the output writer
   * @param sourceList the fixed source list (null to create it)
   */
  public AbstractWurbiler(BufferedReader in, String inName, Writer out, List<String> sourceList) {

    this.out = out;
    this.sourceList = sourceList;
    this.in = new NestedReader(in, inName);

    readers = new ArrayList<>();
    readers.add(this.in);
    paths = new HashSet<>();
    setOutName();
    setAutoIndent(true);
    setIndent(0);
    setImports(null);
  }



  /**
   * Gets the output writer.
   *
   * @return the output writer
   */
  public Writer getWriter()  {
    return out;
  }

  /**
   * Gets the input reader.
   *
   * @return the input reader
   */
  public Reader getReader() {
    return in.getReader();
  }


  /**
   * Compiles the wurbelizable.
   *
   * @return the number of errors, 0 = no errors (warnings are not counted)
   * @throws WurbileException if the compiled output could not be created
   */
  @Override
  public int compile() throws WurbileException {

    try {

      char          c           = 0;                    // next character, -1 if EOF
      boolean       inWurblet   = false;                // true if in wurblet level
      int           subWurblets = 0;                    // number of sub-wurblet levels (nested wurblet)
      StringBuilder wurbletBuf  = new StringBuilder();  // if WURBLET_OBJECT or WURBLET_COMMAND:
                                                        // buffer for text in between @-clause
      boolean       dontRead    = false;                // true if don't read from input stream (once)
      boolean       blockStart  = false;                // true = next char is first in wurblet-block

      errors      = 0;
      wurbletMode = WURBLET_NONE;
      sourceBuf   = new StringBuilder();
      sourceLoc   = srcLocation();

      // initialize the source-list if not yet done
      if (sourceList == null) {
        sourceList = new ArrayList<>();
      }

      OUTER:
      for (;;) {
        if (dontRead)  {
          dontRead = false;
        }
        else  {
          int ic = read();
          if (ic == -1) {
            break;    // End of File
          }
          c = (char) ic;
        }
        if (c == '\\') {
          int ic = read();
          if (ic == -1) {
            break;    // eof
          }
          char nc = (char) ic;
          if (nc == '@')  {
            if (inWurblet) {
              if (wurbletMode == WURBLET_BLOCK) {
                out.write(nc);
              }
              else  {
                wurbletBuf.append(nc);
              }
            }
            else        {
              sourceBuf.append(nc);
            }
          }
          else if (nc != ' ') {
            if (inWurblet) {
              if (wurbletMode == WURBLET_BLOCK) {
                out.write(c);
                out.write(nc);
              }
              else  {
                wurbletBuf.append(c);
                wurbletBuf.append(nc);
              }
            }
            else        {
              sourceBuf.append(c);
              sourceBuf.append(nc);
            }
          }
        }
        else {
          if (c == '@') {
            int ic = read();

            char nc = (char) ic;

            if (nc == '{' || nc == '(' || nc == '[') {
              // open next wurblet-level
              if (inWurblet) {
                // we are already on wurblet level: count nested wurblet level.
                subWurblets++;
                out.write(c);
                out.write(nc);
              }
              else  {
                // we are on source-level: close current source buffer and enter wurblet-level
                flushSource();
                wurbletMode = nc;
                inWurblet = true;
                blockStart = (nc == WURBLET_BLOCK);
              }
            }
            else  {
              // some unknown sequence, simply keep it
              logWarning("unknown @-sequence at " + srcLocation());
              if (inWurblet) {
                out.write(c);
              }
              else {
                sourceBuf.append(c);
              }
              if (ic == -1) {
                break;    // end of file
              }
              if (inWurblet) {
                out.write(nc);
              }
              else {
                sourceBuf.append(nc);
              }
            }
          }
          else if (c == '}' || c == ')' || c == ']') {
            int ic = read();
            if (ic == -1) {
              break;  // eof
            }
            char nc = (char) ic;
            if (nc == '@') {
              if (inWurblet) {
                if (subWurblets > 0) {
                  subWurblets--;
                  out.write(c);
                  out.write(nc);
                }
                else {
                  inWurblet = false;
                  sourceLoc = srcLocation();
                  if (wurbletMode == WURBLET_OBJECT) {
                    if (isAutoIndent()) {
                      setIndent(autoIndent + (lastWasOpenBlock ? 2 : 0));
                    }
                    out.write(indentStr + outName + ".print(" + wurbletBuf.toString() + ");\n");
                  }
                  else if (wurbletMode == WURBLET_COMMAND) {
                    StringTokenizer stok = new StringTokenizer(wurbletBuf.toString(), " \t\n\r");
                    if (stok.hasMoreTokens()) {
                      String cmd = stok.nextToken().toLowerCase();
                      boolean cmdOk;
                      switch (cmd) {
                        case CMD_INCLUDE:
                          // include <pathname>
                          cmdOk = handleInclude(stok);
                          break;
                        case CMD_PACKAGE:
                          // set the package name
                          cmdOk = handlePackage(stok);
                          break;
                        case CMD_EXTENDS:
                          // set the parent class
                          cmdOk = handleExtends(stok);
                          break;
                        case CMD_IMPLEMENTS:
                          // add some interface(s)
                          cmdOk = handleImplements(stok);
                          break;
                        case CMD_IMPORT:
                          // add to imports
                          handleImport(stok);
                          cmdOk = true;
                          break;
                        case CMD_INDENT:
                          // set the indent command
                          handleIndent(stok);
                          cmdOk = true;
                          break;
                        case CMD_TO:
                          // switch output stream
                          handleOutName(stok);
                          cmdOk = true;
                          break;
                        case CMD_ARGS:
                          // set the args command
                          handleArgs(stok);
                          cmdOk = true;
                          break;
                        case CMD_PHASE:
                          // set the wurblet's phase
                          handlePhase(stok);
                          cmdOk = true;
                          break;
                        case CMD_COMMENT:
                          // add wurblet comment
                          cmdOk = handleComment(wurbletBuf.toString());
                          break;
                        case CMD_CODE:
                          // add wurblet code
                          cmdOk = handleCode(wurbletBuf.toString());
                          break;
                        case CMD_CONFIG:
                          // add wurblet comment
                          cmdOk = handleConfig(wurbletBuf.toString());
                          break;
                        default:
                          logError("unknown wurblet-command '" + cmd + "' at " + srcLocation());
                          break OUTER;
                      }
                      if (!cmdOk) {
                        break;  // some error
                      }
                    }
                  }
                  wurbletBuf.setLength(0);
                }
              }
              else {
                ic = read();
                if (ic != -1) {
                  char nnc = (char) ic;
                  if (nnc == '(') {    // pathological ...blahhh)@(x==y?"a":"b")@
                    sourceBuf.append(c);
                    flushSource();
                    inWurblet = true;
                    wurbletMode = WURBLET_OBJECT;
                    continue;
                  }
                }
                logError("wurblet-level closed unexpectedly on output level '" + getOutName() + "' at " + srcLocation());
                break;
              }
            }
            else {
              if (inWurblet) {
                if (wurbletMode == WURBLET_BLOCK) {
                  out.write(c);
                }
                else  {
                  wurbletBuf.append(c);
                }
              }
              else        {
                sourceBuf.append(c);
              }
              c = nc;
              dontRead = true;
            }
          }
          else {
            if (inWurblet) {
              if (wurbletMode == WURBLET_BLOCK) {
                // simpy write wurblet-code to out
                if (blockStart) {
                  blockStart = false;
                  if (c != '\n')  {
                    out.write(c);
                  }
                }
                else  {
                  out.write(c);
                }
                if (isAutoIndent()) {
                  // check for autoindent
                  if (c == '\n')  {
                    startAutoIndent = true;  // start count leading spaces
                  }
                  else {
                    if (startAutoIndent)  {
                      autoIndent = 0;       // line start
                      startAutoIndent = false;
                      inAutoIndent = true;
                    }
                    if (inAutoIndent) {
                      if (c == ' ') {
                        autoIndent++;
                      }
                      else {
                        inAutoIndent = false;
                      }
                    }
                    lastWasOpenBlock = (c == '{');
                  }
                }
              }
              else {
                // append to wurblet-buffer (is an object-name or a wurblet-command)
                wurbletBuf.append(c);
              }
            }
            else  {
              // append to source buffer
              sourceBuf.append(c);
            }
          }
        }
      }

      // if there's something left in the source-buffer, append that and write closing line
      flushSource();

      // do some checks
      if (inWurblet) {
        logError("unclosed wurblet level (" + (subWurblets + 1) + ")");
      }

      return errors;
    }
    catch (IOException ex) {
      throw new WurbileException("wurbilation failed", ex);
    }
    finally {
      // close any pending include streams (in case of some error or exception)
      while (readers.size() > 1)  {
        try {
          in.getReader().close();
        }
        catch (IOException ex) {
          // ignored
        }
        readers.remove(0);
        paths.remove(in.getPath());
        in = readers.get(0);
      }
    }
  }



  /**
   * Gets the list of fixed source-strings.
   *
   * @return the list of strings
   */
  @Override
  public List<String> getSourceList() {
    return sourceList;
  }


  /**
   * Presets the source list.
   *
   * @param sourceList the source list, null if compile() should initialize it
   */
  @Override
  public void setSourceList(List<String> sourceList) {
    this.sourceList = sourceList;
  }


  /**
   * Gets the source indentation.
   *
   * @return the current indentation
   */
  @Override
  public int getIndent() {
    return indent;
  }


  /**
   * Sets the indentation for code generating the source.
   *
   * @param indent the new indentation
   */
  @Override
  public void setIndent(int indent) {
    if (indent < 0) {
      indent = 0;
    }
    if (this.indent != indent || indentStr == null) {
      this.indent = indent;
      StringBuilder buf = new StringBuilder();
      while (indent-- > 0) {
        buf.append(' ');
      }
      indentStr = buf.toString();
    }
  }


  /**
   * Sets the autoindent feature.
   *
   * @param autoIndent true to enable autoindent, false to disable.
   */
  @Override
  public void setAutoIndent(boolean autoIndent) {
    this.autoIndent = autoIndent ? 0 : -1;
    inAutoIndent = false;
  }

  /**
   * Gets the autoindent feature.
   *
   * @return true if autoindent is enabled (default)
   */
  @Override
  public boolean isAutoIndent() {
    return autoIndent >= 0;
  }


  /**
   * Sets the name of the output stream in the generated source.
   *
   * @param outName the name of the output stream, null sets to the default value "out".
   */
  @Override
  public void setOutName(String outName)  {
    this.outName = outName == null ? OUTNAME : outName;
  }


  /**
   * Sets outname to the default.
   */
  @Override
  public void setOutName() {
    setOutName(null);
  }


  /**
   * Gets the outname.
   *
   * @return the name of the output stream
   */
  @Override
  public String getOutName() {
    return outName;
  }


  /**
   * Gets the logger for warnings and errors.
   *
   * @return the logger.
   */
  @Override
  public Logger getLogger() {
    return logger;
  }


  /**
   * Sets the logger for warnings and errors.
   *
   * @param logger the logger.
   */
  @Override
  public void setLogger(Logger logger) {
    this.logger = logger;
  }


  /**
   * Gets the verbosity.
   * @return the verbosity
   */
  @Override
  public Verbosity getVerbosity() {
    return verbosity;
  }


  /**
   * Sets the verbosity.
   * @param verbosity the verbosity
   */
  @Override
  public void setVerbosity(Verbosity verbosity) {
    this.verbosity = verbosity;
  }


  /**
   * Logs an error message.
   *
   * @param msg the error message
   */
  public void logError(String msg) {
    if (logger != null) {
      logger.error(msg + " in " + in);
    }
    errors++;
  }


  /**
   * Logs a warning.
   *
   * @param msg the warning message
   */
  public void logWarning(String msg) {
    if (logger != null) {
      logger.warning(msg + " in " + in);
    }
  }


  /**
   * Logs a verbose message.
   *
   * @param msg the warning message
   */
  public void logInfo(String msg) {
    if (logger != null) {
      logger.info(msg + " in " + in);
    }
  }



  /**
   * Gets the package name.
   *
   * @return the package name, null if default package
   */
  @Override
  public String getPackageName() {
    return packageName;
  }


  /**
   * Sets the package-name.
   *
   * @param packageName the package name.
   */
  @Override
  public void setPackageName(String packageName)  {
    this.packageName = packageName;
  }


  /**
   * Gets the name of the parent class.
   *
   * @return the name of the parent class, null if default
   */
  @Override
  public String getParentClass()  {
    return parentClass;
  }


  /**
   * Sets the parent class.
   *
   * @param parentClass the name of the parent class
   */
  @Override
  public void setParentClass(String parentClass)  {
    this.parentClass = parentClass;
  }


  /**
   * Adds an interface.
   *
   * @param iface the name of the interface
   * @return true if added, false if already in list of interfaces.
   */
  public boolean addInterface(String iface)  {
    if (interfaces == null) {
      interfaces = new TreeSet<>();
    }
    return interfaces.add(iface);
  }

  /**
   * Gets the interface list.
   *
   * @return the list of interfaces
   */
  public Collection<String> getInterfaces() {
    return interfaces;
  }


  @Override
  public String[] getImports() {
    return imports.toArray(new String[0]);
  }

  @Override
  public void setImports(String[] imps) {
    imports = new HashSet<>();
    if (imps != null) {
      imports.addAll(Arrays.asList(imps));
    }
  }

  @Override
  public String[] getArgs() {
    return this.args;
  }

  @Override
  public void setArgs(String[] args) {
    this.args = args;
  }

  @Override
  public String getComment() {
    return comment == null ? null : comment.toString();
  }

  @Override
  public void setComment(String comment) {
    this.comment = comment == null ? null : new StringBuilder(comment);
  }

  @Override
  public String getExtraCode() {
    return code == null ? null : code.toString();
  }

  @Override
  public void setExtraCode(String code) {
    this.code = code == null ? null : new StringBuilder(code);
  }

  @Override
  public String getConfiguration() {
    return configuration == null ? null : configuration.toString();
  }

  @Override
  public void setConfiguration(String configuration) {
    this.configuration = configuration == null ? null : new StringBuilder(configuration);
  }

  @Override
  public Integer getPhase() {
    return phase;
  }

  @Override
  public void setPhase(Integer phase) {
    this.phase = phase;
  }



  /**
   * Extracts or appends a multiline text.
   *
   * @param text the original text including the command
   * @param cmd the command string
   * @param buf the string builder
   * @return the possibly new string builder, null if none
   */
  private StringBuilder extractMultiline(String text, String cmd, StringBuilder buf) {
    int ndx = text.indexOf(cmd);
    if (ndx >= 0) {
      String str = text.substring(ndx + cmd.length());
      if (!str.isEmpty() && Character.isWhitespace(str.charAt(0))) {
        // cut first whitespace
        str = str.substring(1);
      }
      if (buf == null) {
        buf = new StringBuilder();
      }
      if (buf.length() > 0) {
        buf.append('\n');
      }
      buf.append(str);
      if (verbosity.isDebug()) {
        logger.info("{" + cmd + " " + str + "}");
      }
    }
    return buf;
  }


    /**
   * Handles the include-command.
   *
   * @param stok the tokenizer pointing to first arg
   * @return true if ok, false if some error
   */
  private boolean handleInclude(StringTokenizer stok) {
    if (stok.hasMoreTokens()) {
      try {
        String path = WurbelHelper.translateVariables(stok.nextToken(), null);
        if (paths.add(path))  {
          in = new NestedReader(path);
          readers.add(0, in);
          sourceLoc = srcLocation();
          if (verbosity.isDebug()) {
            logger.info("{" + CMD_INCLUDE + " " + path + "}");
          }
        }
        else  {
          logError("include-loop while including '" + path + "' at " + srcLocation());
          return false;
        }
      }
      catch (IOException ex)  {
        logError(ex.toString());
        return false;
      }
    }
    else  {
      logError("filename missing in include command at " + srcLocation());
      return false;
    }
    return true;
  }


  /**
   * Handles the package-command.
   *
   * @param stok the tokenizer pointing to first arg
   * @return true if ok, false if some error
   */
  private boolean handlePackage(StringTokenizer stok) {
    if (stok.hasMoreTokens()) {
      String token = stok.nextToken();
      if (packageName != null && !packageName.equals(token))  {
        logError("more than one package-command ignored at " + srcLocation());
        return false;
      }
      else  {
        setPackageName(token);
          if (verbosity.isDebug()) {
            logger.info("{" + CMD_PACKAGE + " " + token + "}");
          }
      }
    }
    return true;
  }


  /**
   * Handles the extends-command.
   *
   * @param stok the tokenizer pointing to first arg
   * @return true if ok, false if some error
   */
  private boolean handleExtends(StringTokenizer stok) {
    if (stok.hasMoreTokens()) {
      String token = stok.nextToken();
      if (parentClass != null && !parentClass.equals(token))  {
        logError("more than one extends-command ignored at " + srcLocation());
        return false;
      }
      else  {
        setParentClass(token);
        if (verbosity.isDebug()) {
          logger.info("{" + CMD_EXTENDS + " " + token + "}");
        }
      }
    }
    return true;
  }


  /**
   * Handles the implements-command.
   *
   * @param stok the tokenizer pointing to first arg
   * @return true if ok, false if some error
   */
  private boolean handleImplements(StringTokenizer stok) {
    while (stok.hasMoreTokens()) {
      String iface = stok.nextToken();
      if (!addInterface(iface)) {
        logError("interface  " + iface + " already added at " + srcLocation());
        return false;
      }
      if (verbosity.isDebug()) {
        logger.info("{" + CMD_IMPLEMENTS + " " + iface + "}");
      }
    }
    return true;
  }


  /**
   * Handles the import-command.
   *
   * @param stok the tokenizer pointing to first arg
   */
  private void handleImport(StringTokenizer stok) {
    if (stok.hasMoreTokens()) {
      String token = stok.nextToken();
      imports.add(token);
      if (verbosity.isDebug()) {
        logger.info("{" + CMD_IMPORT + " " + token + "}");
      }
    }
  }


  /**
   * Handles the args-command.
   *
   * @param stok the tokenizer pointing to first arg
   */
  private void handleArgs(StringTokenizer stok) {
    List<String> argList = new ArrayList<>();
    if (stok.hasMoreTokens()) {
      String arg = stok.nextToken();
      argList.add(arg);
      if (verbosity.isDebug()) {
        logger.info("{" + CMD_ARGS + " " + arg + "}");
      }
    }
    if (!argList.isEmpty()) {
      args = new String[argList.size()];
      Iterator<String> iter = argList.iterator();
      int i=0;
      while (iter.hasNext())  {
        args[i++] = iter.next();
      }
    }
    else  {
      args = null;
    }
  }


  /**
   * Handles the comment command.
   *
   * @param buf the comment text
   * @return true if ok, false if some error
   */
  private boolean handleComment(String buf) {
    comment = extractMultiline(buf, CMD_COMMENT, comment);
    return comment != null;
  }

  /**
   * Handles the code command.
   *
   * @param buf the code
   * @return true if ok, false if some error
   */
  private boolean handleCode(String buf) {
    code = extractMultiline(buf, CMD_CODE, code);
    return code != null;
  }

  /**
   * Handles the config command.
   *
   * @param buf the configuration text
   * @return true if ok, false if some error
   */
  private boolean handleConfig(String buf) {
    configuration = extractMultiline(buf, CMD_CONFIG, configuration);
    return configuration != null;
  }


  /**
   * Handles the indent-command.
   *
   * @param stok the tokenizer pointing to first arg
   */
  private void handleIndent(StringTokenizer stok) {
    if (stok.hasMoreTokens()) {
      String token = stok.nextToken();
      if (token.equals("auto")) {
        setAutoIndent(true);
      }
      else  {
        setIndent(Integer.parseInt(token));
        setAutoIndent(false);
      }
      if (verbosity.isDebug()) {
        logger.info("{" + CMD_INDENT + " " + token + "}");
      }
    }
  }


  /**
   * Handles the phase-command.
   *
   * @param stok the tokenizer pointing to first arg
   */
  private void handlePhase(StringTokenizer stok) {
    if (stok.hasMoreTokens()) {
      String token = stok.nextToken();
      int ph = Integer.parseInt(token);
      if (ph < 1) {
        setPhase(null);
      }
      else {
        setPhase(ph);
      }
      if (verbosity.isDebug()) {
        logger.info("{" + CMD_PHASE + " " + token + "}");
      }
    }
  }


  /**
   * Handles the to-command.
   *
   * @param stok the tokenizer pointing to first arg
   */
  private void handleOutName(StringTokenizer stok) {
    String path = stok.hasMoreTokens() ? stok.nextToken() : null;
    setOutName(path);
    if (verbosity.isDebug()) {
      logger.info("{" + CMD_TO + (path == null ? "" : (" " + path)) + "}");
    }
  }


  /**
   * Reads next char from input.
   * Could be from some inluded file or the main stream.
   */
  private int read() throws IOException {
    int c = in.read();
    if (c == -1)  {
      if (readers.size() <= 1) {
        return -1;   // EOF on main reader
      }
      // EOF was on an included reader
      readers.remove(0);
      paths.remove(in.getPath());
      in = readers.get(0);
      flushSource();
      return read();
    }
    return c;
  }



  /**
   * Flushes the source buffer.
   */
  private void flushSource() throws IOException {
    if (sourceBuf.length() > 0) {
      // cut first newline, if any (comes from @[ ... newline)
      if ((wurbletMode == WURBLET_BLOCK || wurbletMode == WURBLET_COMMAND) &&
          sourceBuf.charAt(0) == '\n')  {
        sourceBuf.deleteCharAt(0);
      }
      if (sourceBuf.length() > 0) {
        if (isAutoIndent()) {
          setIndent(autoIndent + (lastWasOpenBlock ? 2 : 0));
        }
        out.write(indentStr + outName + ".print(source[" + sourceList.size() + "]); // " +
                  (in.isIncluded() ? (in + ":") : "") + sourceLoc + " = " + hintString(sourceBuf, 40) + "\n");
        sourceList.add(sourceBuf.toString());
        sourceBuf.setLength(0);
      }
    }
    sourceLoc = srcLocation();
  }


  /**
   * Gets the current location in input source.
   *
   * @return the current source-location
   */
  private String srcLocation()  {
    return in.getLineNo() + ":" + in.getLinePos();
  }


  /**
   * Gets a formatted source string of max length and no control-chars.
   *
   * @return the formatted source string
   */
  private String hintString(StringBuilder buf, int max)  {
    int len = buf.length();
    boolean trailDots = false;
    if (max > len)  {
      max = len;
    }
    else  {
      trailDots = true;
    }
    StringBuilder nbuf = new StringBuilder();
    nbuf.append('"');
    for (int i=0; i < max; i++) {
      char c = buf.charAt(i);
      if (!Character.isISOControl(c)) {
        nbuf.append(c);
      }
    }
    if (trailDots)  {
      nbuf.append("...");
    }
    nbuf.append('"');
    return nbuf.toString();
  }

}
