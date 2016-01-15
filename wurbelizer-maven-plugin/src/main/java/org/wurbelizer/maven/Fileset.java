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

// $Id: Fileset.java 97 2016-01-04 11:35:02Z svn $

package org.wurbelizer.maven;

import org.apache.maven.shared.model.fileset.FileSet;

/**
 * Simple FileSet wrapper to make fileset configuration available without
 * camel-case fiddling.
 *
 * @author mnolte
 */
public class Fileset extends FileSet {

    /**
     * Generated serial version id.
     */
    private static final long serialVersionUID = 4061903418270599092L;
}
