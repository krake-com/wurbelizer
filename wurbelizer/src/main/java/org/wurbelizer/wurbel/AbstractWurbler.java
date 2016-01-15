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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.wurbelizer.misc.Constants;
import org.wurbelizer.wurblet.Wurblet;
import org.wurbelizer.wurblet.WurbletData;




/**
 * Provides basic functionality of a wurblet container.<br>
 * The container is responsible to load a wurblet and run it.
 *
 * @author harald
 */

public abstract class AbstractWurbler implements Wurbler {

  private Wurblet wurblet;            // current wurblet
  private List<Wurblet> wurblets;     // all wurblets invoked during the last wurbel pass
  private Properties otherProps;      // wurblet specific properties
  private String[] source;            // the fixed source
  private PrintStream out;            // the output stream for generated code
  private String[] args;              // arguments for the wurblet (never null)
  private boolean fixedArgs;          // true = args are fixed from wurblet, container should not override


  /**
   * Creates a wurbler.
   * The output of the wurblet output by default goes to stdout.
   */
  public AbstractWurbler()  {
    setPrintStream(System.out);   // default output goes to stdout
    setArgs(null);                // default is no extra args
    clearInvokedWurblets();
  }


  /**
   * Loads the wurblet from persistant storage.
   * The wurblet's classname may be either absolute or relative. If relative
   * a classpath must be provided. If the wurblet class could be loaded and
   * the wurblet instance created the serialized fixed source will be loaded
   * from a file with the same name as the wurblet-class but the extension replaced
   * by {@code .ser}.
   *
   * @param className the pathname of the java-class.
   * @param classPath an optional list of prefixes to load the className from
   * @param otherProps extra properties for this wurblet only, null = none
   *
   * @throws WurbelException if loading the wurblet failed
   */
  public void loadWurblet(String className, String[] classPath, Properties otherProps)
         throws WurbelException {

    this.otherProps = otherProps;

    Class<?> clazz  = null;
    setWurblet(null);

    // search in classpath only if classPath set and className is not fully qualified
    int max = (classPath == null || className.indexOf('.') > 0) ? 1 : classPath.length + 1;
    for (int i=0; i < max; i++) {
      String name = (i == max-1) ? className : (classPath[i] + "." + className);
      try {
        // load the class
        clazz = Class.forName(name);
        if (Wurblet.class.isAssignableFrom(clazz))  {
          setWurblet((Wurblet)clazz.newInstance());
          break;
        }
        else  {
          throw new WurbelException("not a wurblet: " + className);
        }
      }
      catch (ClassNotFoundException ex) {
        // try next
      }
      catch (Exception ex) {
        throw new WurbelException("cannot initialize wurblet '" + className + "'", ex);
      }
    }

    if (wurblet == null) {
      throw new WurbelException("Wurblet '" + className + "' not found");
    }

    // load fixed source
    ObjectInputStream is = null;
    // make className relative, if absolute
    int ndx = className.lastIndexOf('.');
    String serName = (ndx > 0 ? className.substring(ndx+1) : className) + Constants.FILE_SOURCE_EXTENSION;
    try {
      is = new ObjectInputStream(clazz.getResourceAsStream(serName));
    }
    catch (Exception ex)  {
      throw new WurbelException("can't load fixed text object '" + serName + "'", ex);
    }

    try {
      WurbletData data = (WurbletData)is.readObject();
      setSource(data.getSource());
      setArgs(data.getArgs());
      setFixedArgs(data.getArgs() != null);
    }
    catch (IOException | ClassNotFoundException | RuntimeException ex) {
      throw new WurbelException("cannot read from fixed text object '" + serName + "'", ex);
    }
    finally {
      try {
        is.close();
      }
      catch (Exception ex) { }
    }
  }


  /**
   * Clears the list of all executed wurblets so far.
   */
  public void clearInvokedWurblets() {
    wurblets = new ArrayList<>();
  }


  /**
   * Gets the list of all invoked wurblets so far.
   *
   * @return the wurblets
   */
  public List<Wurblet> getInvokedWurblets() {
    return wurblets;
  }


  /**
   * Runs the wurblet.
   *
   * @throws WurbelException if running the wurblet failed.
   */
  public void runWurblet() throws WurbelException {
    wurblets.add(wurblet);
    wurblet.run();
  }


  /**
   * Invokes the cleanup method on all wurblets ran so far.
   *
   * @throws WurbelException if cleanup failed
   */
  public void cleanupInvokedWurblets() throws WurbelException {
    if (wurblets != null) {
      for (Wurblet w: wurblets) {
        w.cleanup();
      }
    }
  }

  /**
   * Gets the fixed source texts.
   *
   * @return the source array
   */
  @Override
  public String[] getSource() {
    return source;
  }


  /**
   * Sets the fixed source text.
   *
   * @param source the fixed source text
   */
  public void setSource(String[] source)  {
    this.source = source;
    clearInvokedWurblets();
  }


  /**
   * Gets the output stream where to write generated code to.
   *
   * @return the output print stream
   */
  @Override
  public PrintStream getPrintStream() {
    return out;
  }


  /**
   * Sets the output stream.
   *
   * @param out the output stream
   */
  public void setPrintStream(PrintStream out) {
    this.out = out;
  }


  /**
   * Gets the wurblet.
   *
   * @return the currently loaded wurblet
   */
  public Wurblet getWurblet() {
    return wurblet;
  }


  /**
   * Sets the current wurblet.
   *
   * @param wurblet the new wurblet.
   */
  public void setWurblet(Wurblet wurblet) {
    if (this.wurblet != null) {
      this.wurblet.setContainer(null);
    }
    this.wurblet = wurblet;
    if (wurblet != null) {
      wurblet.setContainer(this);
    }
  }


  /**
   * Gets the wurblet args.
   *
   * @return array of strings representing args for the wurblet (never null)
   */
  @Override
  public String[] getArgs() {
    return args;
  }


  /**
   * Sets the wurblet args.
   *
   * @param args the arguments, null creates an empty array of args[]
   */
  public void setArgs(String[] args)  {
    // assure not null
    if (args == null) {
      this.args = new String[0];
    }
    else  {
      // translate runtime variables
      this.args = args;
    }
  }


  /**
   * Checks if container is allowed to override the wurblet args.
   *
   * @return true if args are preset in the wurblet, else container may override
   */
  public boolean isFixedArgs() {
    return fixedArgs;
  }


  /**
   * Enables or disables the ability of the container to override the wurblet args.
   *
   * @param fixedArgs true if args are fixed, false if container is allowed to override
   */
  public void setFixedArgs(boolean fixedArgs) {
    this.fixedArgs = fixedArgs;
  }


  /**
   * Gets the properties for a given namespace.
   *
   * @param nameSpace the name of the namespace, null for the default runtime java-properties.
   * @return the properties for the given namespace
   */
  @Override
  public Properties getProperties(String nameSpace) {
    if (nameSpace == null)  {
      return System.getProperties();
    }
    else if (nameSpace.equals(PROPSPACE_ENV))  {
      return WurbelUtilities.getEnvProperties();
    }
    else if (nameSpace.equals(PROPSPACE_EXTRA))  {
      return WurbelUtilities.getExtraProperties();
    }
    else if (nameSpace.equals(PROPSPACE_WURBLET)) {
      return otherProps;
    }
    return null;
  }


  /**
   * Gets the property value for a given namespace and key.
   *
   * @param nameSpace the name of the namespace, null for the default runtime java-properties.
   * @param key the key of the property value
   * @return the value for key, null if no such key
   */
  @Override
  public String getProperty(String nameSpace, String key) {
    Properties props = getProperties(nameSpace);
    if (props != null)  {
      return props.getProperty(key);
    }
    return null;
  }

}
