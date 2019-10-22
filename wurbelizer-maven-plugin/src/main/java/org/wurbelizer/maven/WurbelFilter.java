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

import org.wurbelizer.wurbel.SourceWurbletSubElement;
import org.wurbelizer.wurbel.WurbletExecutionFilter;

import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Wurblet execution filter.
 */
public class WurbelFilter implements WurbletExecutionFilter {

  private final List<Pattern> includeWurbletPatterns;
  private final List<Pattern> includeAnchorPatterns;
  private final List<Pattern> excludeWurbletPatterns;
  private final List<Pattern> excludeAnchorPatterns;

  /**
   * Creates the execution filter.
   *
   * @param includeWurbletPatterns regex patterns for wurblet names to include, empty if none
   * @param includeAnchorPatterns regex patterns for wurblet anchors to include, empty if none
   * @param excludeWurbletPatterns regex patterns for wurblet names to exclude, empty if none
   * @param excludeAnchorPatterns regex patterns for wurblet anchors to exclude, empty if none
   */
  public WurbelFilter(List<Pattern> includeWurbletPatterns, List<Pattern> includeAnchorPatterns,
                      List<Pattern> excludeWurbletPatterns, List<Pattern> excludeAnchorPatterns) {

    this.includeWurbletPatterns = Objects.requireNonNull(includeWurbletPatterns);
    this.includeAnchorPatterns = Objects.requireNonNull(includeAnchorPatterns);
    this.excludeWurbletPatterns = Objects.requireNonNull(excludeWurbletPatterns);
    this.excludeAnchorPatterns = Objects.requireNonNull(excludeAnchorPatterns);
  }

  @Override
  public boolean isExecutable(SourceWurbletSubElement wurbletElement) {
    boolean execute = true;

    if (!includeAnchorPatterns.isEmpty()) {
      execute = false;
      for (Pattern pattern : includeAnchorPatterns) {
        if (pattern.matcher(wurbletElement.getName()).matches()) {
          execute = true;
          break;
        }
      }
    }
    if (execute && !includeWurbletPatterns.isEmpty()) {
      execute = false;
      for (Pattern pattern : includeWurbletPatterns) {
        if (pattern.matcher(wurbletElement.getWurbletName()).matches()) {
          execute = true;
          break;
        }
      }
    }

    if (execute) {
      for (Pattern pattern : excludeAnchorPatterns) {
        if (pattern.matcher(wurbletElement.getName()).matches()) {
          execute = false;
          break;
        }
      }
    }
    if (execute) {
      for (Pattern pattern : excludeWurbletPatterns) {
        if (pattern.matcher(wurbletElement.getWurbletName()).matches()) {
          execute = false;
          break;
        }
      }
    }

    return execute;
  }
}
