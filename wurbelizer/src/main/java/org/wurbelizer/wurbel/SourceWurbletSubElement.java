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

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Wurblet anchor within a {@link SourceElement}.
 * <p>
 * A wurblet anchor is of the form:
 * <pre>
 * &#64;wurblet[(wurbler-args...)] &lt;guardname&gt; &lt;wurbletname&gt; [wurblet-args ...]
 * </pre>
 * The optional wurbler args must be enclosed in parantheses and are directives for
 * the container that executes the wurblets.
 * The guardname is mandatory and must be unique within the source file, except if
 * an inline wurblet (-,+,&lt;,&gt;).
 * The wurbletname is the classname of the wurblet and may be given either as an
 * absolute classname (containing dots) or a relative name which the wurbler
 * will expand according to some given wurblet path.
 * The optional wurblet arguments are passed through the {@link ArgScanner}.
 *
 * @author harald
 */
public class SourceWurbletSubElement extends SourceSubElement {

  private String wurbletName;       // the name of the wurblet
  private String[] wurbletArgs;     // the wurblet arguments
  private String[] wurblerArgs;     // arguments for the wurbler (e.g. fixed indent)
  private int wurbletBegin;         // text offset for begin of wurblet within whole document
  private int wurbletEnd;           // text offset for end of wurblet within whole document


  /**
   * Creates a sub element.
   *
   * @param element the {@link SourceElement} containing the sub element.
   * @param commentLead the comment lead string at start of line (null if none)
   * @param wurbletBegin text offset for begin of wurblet within whole document
   * @param wurbletEnd text offset for end of wurblet within whole document
   *
   * @throws org.wurbelizer.wurbel.SourceException if creation failed
   */
  @SuppressWarnings("this-escape")
  public SourceWurbletSubElement(SourceElement element, String commentLead, int wurbletBegin, int wurbletEnd)
         throws SourceException {

    super(element, element.getSourceDocument().getText().substring(wurbletBegin, wurbletEnd));

    this.wurbletBegin = wurbletBegin;
    this.wurbletEnd = wurbletEnd;

    int start = 8;    // skip "@wurblet"

    Properties props = element.getSourceDocument().getProperties();

    // check if @wurblet is followed by wurbler args, e.g. @wurblet(indent=2)
    List<String> wrgList = new ArrayList<>();   // wurbler args (special args for container)
    if (getText().charAt(start) == '(')  {
      start++;
      int wend = getText().indexOf(')', start);
      if (wend > start) {
        ArgScanner scanner = new ArgScanner(getText().substring(start, wend), props);
        String arg;
        while ((arg = scanner.next()) != null)  {
          wrgList.add(arg);    // wurbler arguments || c == '{' || c == '['
        }
        start = wend + 1;
      }
    }
    setWurblerArgs(wrgList.toArray(new String[0]));

    // scan for args
    List<String> argList = new ArrayList<>();   // wurblet args (args for wurblet)
    String currentGuardedName = null;           // guardname in comment block
    String currentWurbletName = null;           // name of the wurblet in comment block
    // wurblet text with comment leads removed
    String wurbletText = filterCommentLeads(getText().substring(start), commentLead);
    ArgScanner scanner = new ArgScanner(wurbletText, props);
    String arg;
    while ((arg = scanner.next()) != null)  {
      if (!"//".equals(arg)) {    // if not single line comment continuation
        if (currentGuardedName == null) {
          currentGuardedName = arg; // first is guardedName
        }
        else if (currentWurbletName == null) {
          currentWurbletName = arg; // second is wurbletName
        }
        else {
          // others are arguments
          argList.add(arg);
        }
      }
    }

    // set the guard name
    if (currentGuardedName == null) {
      throw new SourceException(element, "missing <guardname> in wurblet anchor");
    }
    setName(currentGuardedName);

    // set the wurblet name
    if (currentWurbletName == null) {
      throw new SourceException(element, "missing <wurbletname> in wurblet anchor");
    }
    setWurbletName(currentWurbletName);

    // set the wurblet argument list (could be empty)
    setWurbletArgs(argList.toArray(new String[0]));
  }


  /**
   * Gets the parent wurblet element that generated the code for this wurblet element.
   *
   * @return the parent wurblet element, null if none
   */
  public SourceWurbletSubElement getParentWurbletElement() {
    return getElement().getParentWurbletElement();
  }


  /**
   * Gets the text offset for the start of the wurblet within whole document.
   *
   * @return the start
   */
  public int getWurbletBegin() {
    return wurbletBegin;
  }

  /**
   * Gets the text offset for end of wurblet within whole document.
   *
   * @return the end
   */
  public int getWurbletEnd() {
    return wurbletEnd;
  }

  /**
   * Moves the wurblet within the document.
   *
   * @param diff chars to move
   */
  public void move(int diff) {
    wurbletBegin += diff;
    wurbletEnd += diff;
  }


  /**
   * Checks whether this is an inline wurblet related the previous code block.
   *
   * @return true if inline wurblet for the previous block
   */
  public boolean isInlinePreviousBlock() {
    char c = getName().charAt(0);
    return c == '-' || c == '<';
  }


  /**
   * Checks whether this is an inline wurblet related the next code block.
   *
   * @return true if inline wurblet for the next block
   */
  public boolean isInlineNextBlock() {
    char c = getName().charAt(0);
    return c == '+' || c == '>';
  }


  /**
   * Checks if this is an inline wurblet.
   *
   * @return true if this is an inline wurblet
   */
  public boolean isInline() {
    return isInlinePreviousBlock() || isInlineNextBlock();
  }


  /**
   * Gets the name of the wurblet.
   *
   * @return the wurblet's name
   */
  public String getWurbletName() {
    return wurbletName;
  }


  /**
   * Sets the name of the wurblet.
   *
   * @param wurbletName the name of the wurbet
   */
  public void setWurbletName(String wurbletName) {
    this.wurbletName = wurbletName;
  }


  /**
   * Gets the wurblet args.
   *
   * @return the wurblet args
   */
  public String[] getWurbletArgs() {
    return wurbletArgs;
  }


  /**
   * Sets the wurblet args.
   *
   * @param wurbletArgs the args
   */
  public void setWurbletArgs(String[] wurbletArgs) {
    this.wurbletArgs = wurbletArgs;
  }


  /**
   * Gets the wurbler (wurblet container) args.
   *
   * @return the args
   */
  public String[] getWurblerArgs() {
    return wurblerArgs;
  }


  /**
   * Sets the wurbler (wurblet container) args
   *
   * @param wurblerArgs the args
   */
  public void setWurblerArgs(String[] wurblerArgs) {
    this.wurblerArgs = wurblerArgs;
  }


  /**
   * Prints the name of the subelement/element/document.
   *
   * @return the text representation for diagnostics.
   */
  @Override
  public String toString()  {
    StringBuilder buf = new StringBuilder();
    buf.append(super.toString()).append(": ").append(wurbletName);
    for (String arg: wurbletArgs) {
      buf.append(' ').append(arg);
    }
    return buf.toString();
  }


  /**
   * Removes all comment leads.
   *
   * @param text the original text
   * @param commentLead the comment lead, null if none
   * @return the stripped text
   */
  private String filterCommentLeads(String text, String commentLead) {
    if (commentLead != null && !commentLead.isEmpty()) {
      String line = text;
      StringBuilder buf = new StringBuilder();
      while (line != null) {
        if (line.startsWith(commentLead)) {
          line = line.substring(commentLead.length());
        }
        int ndx = line.indexOf('\n');
        if (ndx >= 0) {
          buf.append(line, 0, ndx + 1);
          line = line.substring(ndx + 1);
        }
        else {
          buf.append(line);
          line = null;
        }
      }
      return buf.toString();
    }
    return text;
  }

}
