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

/**
 * Netbeans style source region.
 */
public class NetbeansRegion extends AbstractSourceRegion {

  private static final String MATCH_BEGIN = "<editor-fold ";
  private static final String MATCH_END = "</editor-fold>";

  private static final String REGION_BEGIN_1 = "//" + MATCH_BEGIN + "desc=\"";
  private static final String REGION_COLLAPSED_BEGIN_1 = "//" + MATCH_BEGIN + "defaultstate=\"collapsed\" desc=\"";
  private static final String REGION_EXPANDED_BEGIN_1 = "//" + MATCH_BEGIN + "defaultstate=\"expanded\" desc=\"";
  private static final String REGION_BEGIN_2 = "\">" + GUARD_BEGIN;
  private static final String REGION_END = "//" + MATCH_END + GUARD_END;


  @Override
  public String type() {
    return "netbeans";
  }

  @Override
  public boolean isDefault() {
    return true;
  }

  @Override
  public String createBegin(FoldType foldType, String guardName, SourceWurbletSubElement wurbletElement) {
    String begin1;
    if (foldType == FoldType.COLLAPSED) {
      begin1 = REGION_COLLAPSED_BEGIN_1;
    }
    else if (foldType == FoldType.EXPANDED) {
      begin1 = REGION_EXPANDED_BEGIN_1;
    }
    else {
      begin1 = REGION_BEGIN_1;
    }

    return begin1 + createCommentGeneratedBy(guardName, wurbletElement) + REGION_BEGIN_2 + guardName;
  }

  @Override
  public String createEnd(FoldType foldType, String guardName, SourceWurbletSubElement wurbletElement) {
    return REGION_END + guardName;
  }

  @Override
  public String getBeginName(String comment) {
    String name = null;
    if (matches(comment, MATCH_BEGIN)) {
      int ndx = comment.indexOf(GUARD_BEGIN);
      if (ndx >= 0) {
        name = comment.substring(ndx + GUARD_BEGIN.length()).trim();
      }
    }
    return name;
  }

  @Override
  public boolean isEnd(String comment, String name) {
    return matches(comment, MATCH_END) && comment.contains(GUARD_END + name);
  }

}
