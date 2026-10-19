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

package org.wurbelizer.wurbel;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * The state of a single wurbel or wurbile run.
 * <p>
 * Holds the state: the file encoding, the extra properties, the heap files,
 * the source files, and the wurblet classloader.
 * The static accessors in {@link org.wurbelizer.misc.Settings}, {@link WurbelHelper},
 * {@link HeapFileFactory} and {@link SourceFileFactory} delegate to the {@link #current()} context.
 * <p>
 * A context is bound to the current thread via {@link #call(WurbelContext, ScopedValue.CallableOp)}.
 * This allows concurrent runs, e.g., in parallel Maven builds, without interfering with each other.
 * If no context is bound, a single process-wide global context is used.
 * <p>
 * Notice that threads started by a wurblet do not inherit the context, unless
 * they are forked via {@code java.util.concurrent.StructuredTaskScope}.
 *
 * @author harald
 */
public final class WurbelContext {

  private static final ScopedValue<WurbelContext> CURRENT = ScopedValue.newInstance();
  private static final WurbelContext GLOBAL = new WurbelContext();


  /**
   * Gets the current context.
   *
   * @return the context bound to the current thread, the global context if none bound
   */
  public static WurbelContext current() {
    return CURRENT.orElse(GLOBAL);
  }

  /**
   * Gets the global context.<br>
   * Used if no context is bound to the current thread.
   *
   * @return the global context
   */
  public static WurbelContext global() {
    return GLOBAL;
  }

  /**
   * Invokes an operation with the given context bound to the current thread.
   *
   * @param context the context
   * @param op the operation
   * @param <R> the result type
   * @param <X> the exception type
   * @return the result of the operation
   * @throws X if the operation failed
   */
  public static <R, X extends Throwable> R call(WurbelContext context, ScopedValue.CallableOp<? extends R, X> op) throws X {
    return ScopedValue.where(CURRENT, context).call(op);
  }


  private volatile Charset encodingCharset = StandardCharsets.UTF_8;  // encoding to read and write files
  private volatile Properties extraProperties;                        // extra properties (e.g., from maven or ant)
  private volatile ClassLoader wurbletClassLoader;                    // classloader for wurblets, null = default
  private final Map<String,HeapFile> heapFiles = new HashMap<>();     // heapfiles <name>:<file>
  private final Map<String,SourceFile> sourceFiles = new HashMap<>(); // sourcefiles <filename>:<file>


  /**
   * Gets the encoding used to read and write files.
   *
   * @return the encoding charset, default is {@link StandardCharsets#UTF_8}
   */
  public Charset getEncodingCharset() {
    return encodingCharset;
  }

  /**
   * Sets the encoding used to read and write files.
   *
   * @param encodingCharset the charset
   */
  public void setEncodingCharset(Charset encodingCharset) {
    this.encodingCharset = encodingCharset;
  }

  /**
   * Gets the extra properties.
   *
   * @return the properties, null if none
   */
  public Properties getExtraProperties() {
    return extraProperties;
  }

  /**
   * Sets the extra properties (valid among all wurblets).
   *
   * @param extraProperties the properties
   */
  public void setExtraProperties(Properties extraProperties) {
    this.extraProperties = extraProperties;
  }

  /**
   * Gets the classloader to load the wurblets from.
   *
   * @return the classloader, null to use the wurbelizer's classloader
   */
  public ClassLoader getWurbletClassLoader() {
    return wurbletClassLoader;
  }

  /**
   * Sets the classloader to load the wurblets from.
   *
   * @param wurbletClassLoader the classloader, null to use the wurbelizer's classloader
   */
  public void setWurbletClassLoader(ClassLoader wurbletClassLoader) {
    this.wurbletClassLoader = wurbletClassLoader;
  }

  /**
   * Gets the heapfiles.<br>
   * Access must be synchronized on the returned map.
   *
   * @return the map of heapfiles
   */
  Map<String,HeapFile> heapFiles() {
    return heapFiles;
  }

  /**
   * Gets the sourcefiles.<br>
   * Access must be synchronized on the returned map.
   *
   * @return the map of sourcefiles
   */
  Map<String,SourceFile> sourceFiles() {
    return sourceFiles;
  }

}
