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

import java.util.Properties;

/**
 * Source file type.
 *
 * @author harald
 */
public interface SourceType {


  /**
   * Gets the file extension (including the dot).
   * @return the extension, never null
   */
  String getFileExtension();

  /**
   * Extract properties for given file.
   *
   * @param document the source document
   * @return the properties, never null
   */
  Properties extractProperties(SourceDocument document);

}
