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

import java.util.Collection;

/**
 * Generates comments to mark generated code as named regions.
 * <p>
 * Source region types must be registed in {@code META-INF/services/org.wurbelizer.wurbel.SourceRegion}.<br>
 * The wurbelizer already provides some implementations with {@code 'netbeans'} being by default.
 */
public interface SourceRegion {

  /**
   * Editor fold type.
   */
  enum FoldType {

    /** editor fold which is collapsed by default. */
    COLLAPSED,

    /** editor fold which is expanded by default. */
    EXPANDED
  }


  /**
   * Gets the unique name of the region type.
   *
   * @return the type name
   */
  String type();

  /**
   * Returns whether this is the default region type.
   *
   * @return true if default
   */
  default boolean isDefault() {
    return false;
  }

  /**
   * Creates the begin comment.
   *
   * @param foldType the fold type, null if none
   * @param guardName the name of the guarded section
   * @param wurbletElement the wurblet element
   * @return the comment
   */
  String createBegin(FoldType foldType, String guardName, SourceWurbletSubElement wurbletElement);

  /**
   * Creates the end comment.
   *
   * @param foldType the fold type, null if none
   * @param guardName the name of the guarded section
   * @param wurbletElement the wurblet element
   * @return the comment
   */
  String createEnd(FoldType foldType, String guardName, SourceWurbletSubElement wurbletElement);

  /**
   * Gets the region's name (wurblet tag) if this is the begin comment is of this region type.
   *
   * @param comment the comment string
   * @return the region's name if is of this type
   */
  String getBeginName(String comment);

  /**
   * Returns whether this is the end comment for the given region's name.
   *
   * @param comment the comment string
   * @param name the region's name
   * @return true if matching end comment
   */
  boolean isEnd(String comment, String name);


  /**
   * Gets all registered source regions.
   *
   * @return the source regions
   */
  static Collection<SourceRegion> getAll() {
    return WurbelHelper.getSourceRegionMap().values();
  }

  /**
   * Gets the source region of given type.
   *
   * @param type the type, null if default type
   * @return the region of given type
   * @throws WurbelException if no matching region type configured
   */
  static SourceRegion get(String type) throws WurbelException {
    SourceRegion region;
    if (type == null) {
      region = WurbelHelper.getDefaultSourceRegion();
    }
    else {
      region = WurbelHelper.getSourceRegionMap().get(type);
    }
    if (region == null) {
      StringBuilder buf = new StringBuilder();
      if (type == null) {
        buf.append("no default region type");
      }
      else {
        buf.append("no region type '").append(type).append("'");
      }
      buf.append(" configured. Available are: ");
      boolean comma = false;
      for (SourceRegion sourceRegion: getAll()) {
        if (comma) {
          buf.append(", ");
        }
        buf.append(sourceRegion);
        comma = true;
      }
      throw new WurbelTerminationException(buf.toString());
    }
    return region;
  }

}
