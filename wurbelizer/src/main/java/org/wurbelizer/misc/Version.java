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


package org.wurbelizer.misc;



/**
 * Holds versioning information.<br>
 *
 * @author harald
 * @version $Revision: 97 $
 */
public class Version {

  /** the release from manifest **/
  public static final String RELEASE = Version.class.getPackage().getImplementationVersion();


  /**
   * Prints the version, copyright and license.
   *
   * @param args command line arguments (not used)
   */
  public static void main(String[] args) {
    System.out.println(
"\nWurbelizer - http://www.wurbelizer.org" +
"\n\nVersion " + RELEASE + "\n\n" +
"This library is free software; you can redistribute it and/or\n" +
"modify it under the terms of the GNU Lesser General Public\n" +
"License as published by the Free Software Foundation; either\n" +
"version 2.1 of the License, or (at your option) any later version.\n\n" +
"This library is distributed in the hope that it will be useful,\n" +
"but WITHOUT ANY WARRANTY; without even the implied warranty of\n" +
"MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU\n" +
"Lesser General Public License for more details.\n\n" +
"You should have received a copy of the GNU Lesser General Public\n" +
"License along with this library; if not, write to the Free Software\n" +
"Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA\n");
  }
}
