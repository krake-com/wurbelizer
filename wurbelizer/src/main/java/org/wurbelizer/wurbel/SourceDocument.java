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

import org.wurbelizer.misc.Logger;
import org.wurbelizer.misc.Verbosity;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * A {@code SourceDocument} represents the logical structure of a source file in memory.
 * <p>
 * Source documents are divided into sections, so-called {@link SourceElement}s.
 * Each element can be manipulated independently. Especially new elements, so-called
 * guarded blocks, can be inserted, replaced and removed. They are identified by
 * a unique name, a tag, which is stored along with the section within the document.
 * Elements can also be addressed by their logical index (starting at 0).
 *
 * @author harald
 */
public class SourceDocument {

  private static final String INLINE_COMMENT = "/**/";    // comment bounds for inline replacement

  private StringBuilder text;               // whole document as one String
  private final String name;                // the documents name
  private final Properties wurbProps;       // extra wurblet properties
  private final Logger logger;              // the logger
  private final Verbosity verbosity;        // the verbosity
  private List<SourceElement> elements;     // List of SourceElements


  /**
   * Constructs a SourceDocument from a text buffer.
   *
   * @param text the contents of the document
   * @param name the name of the document (usually the filename)
   * @param wurbProps optional properties for wurblets
   * @param logger the logger
   * @param verbosity the verbosity
   * @throws org.wurbelizer.wurbel.SourceException if parsing the document failed
   */
  public SourceDocument(StringBuilder text, String name, Properties wurbProps, Logger logger, Verbosity verbosity)
         throws SourceException {

    this.name       = name;
    this.wurbProps  = wurbProps;
    this.logger     = logger;
    this.verbosity  = verbosity == null ? Verbosity.DEFAULT : verbosity;

    // parse the document
    setText(text);

    checkGuardedElements();
  }


  /**
   * Constructs a SourceDocument from a {@link String}.
   *
   * @param text the contents of the document
   * @param name the name of the document (usually the filename)
   * @param wurbProps optional properties for wurblets
   * @param logger the logger
   * @param verbosity the verbosity
   *
   * @throws org.wurbelizer.wurbel.SourceException if parsing the document failed
   */
  public SourceDocument(String text, String name, Properties wurbProps, Logger logger, Verbosity verbosity)
         throws SourceException {

    this(new StringBuilder(text), name, wurbProps, logger, verbosity);
  }




  /**
   * Inserts a guarded section by creating a new SourceElement.
   *
   * @param guardType the guarded region type
   * @param foldType the editor fold type (currently only supported for Netbeans), null if none
   * @param guardedName the name of the guarded section
   * @param str the text to insert (the output of the wurblet)
   * @param elemNdx the element index where to insert the new element
   * @param wurbletElement the wurblet element, null if none
   *
   * @throws org.wurbelizer.wurbel.SourceException if creating the element failed
   */
  public void insertGuarded(SourceRegion guardType, SourceRegion.FoldType foldType,
                            String guardedName, String str, int elemNdx,
                            SourceWurbletSubElement wurbletElement)
         throws SourceException {

    // find initial indent for comment
    int indent = 0;
    int slen = str.length();
    for (int i=0; i < slen; i++) {
      char c = str.charAt(i);
      if (c == '\n')  {
        indent = 0;
      }
      else  {
        if (Character.isWhitespace(c))  {
          indent++;
        }
        else {
          break; // some other char
        }
      }
    }

    // create indent string
    StringBuilder indentBuf = new StringBuilder();
    while (indent-- > 0) {
      indentBuf.append(' ');
    }

    // create guarded code
    str = "\n" + indentBuf + guardType.createBegin(foldType, guardedName, wurbletElement) + "\n\n" +
          str +
          "\n" + indentBuf + guardType.createEnd(foldType, guardedName, wurbletElement) + "\n";

    // insert or append element

    if (elements.isEmpty()) {
      // pathological case: document is empty
      text.insert(0, str);
      SourceElement elem = new SourceElement(this, SourceElement.Type.GUARDED, 0, str.length(), wurbletElement);
      elem.setGuardedName(guardedName);
      elements.add(elem);
    }

    if (elemNdx >= elements.size()) {
      // append at end
      int begin = text.length();
      text.append(str);
      SourceElement elem = new SourceElement(this, SourceElement.Type.GUARDED, begin, text.length(), wurbletElement);
      elem.setGuardedName(guardedName);
      elements.add(elem);
    }
    else  {
      // somewhere in between (standard case)
      SourceElement insElem = elements.get(elemNdx);   // get Element where to insert *before*
      int begin = insElem.getBegin();
      int len   = str.length();
      int end   = begin + len;
      text.insert(begin, str);
      SourceElement elem = new SourceElement(this, SourceElement.Type.GUARDED, begin, end, wurbletElement);
      elem.setGuardedName(guardedName);
      elements.add(elemNdx, elem);
      for (int i=elemNdx+1; i < elements.size(); i++) {
        insElem = elements.get(i);
        insElem.setBegin(insElem.getBegin() + len);
        insElem.setEnd(insElem.getEnd() + len);
      }
    }
  }


  /**
   * Removes a guarded section with a given name starting at a given logical index.
   *
   * @param guardedName the name of the guarded section to remove
   * @param startIndex start index in elements where to start the search
   *
   * @return the index &ge; 0 if removed, -1 if no such section
   */
  public int deleteGuarded(String guardedName, int startIndex) {
    for (int i=startIndex; i < elements.size(); i++) {
      SourceElement elem = elements.get(i);
      if (elem.isGuarded() && elem.getGuardedName().equals(guardedName)) {
        // found
        deleteElement(i);
        /*
         * Because the guarded blocks begin with a single line comment which is
         * indendet by a newline + <spaces> the code-block before must be made
         * smaller to cut off this garbage. Otherwise, the generated sources
         * would get longer and longer... ;-)
         */
        if (i > 0) {
          elem = elements.get(i - 1);
          if (elem.isWhiteEmpty())  {
            // contains only whitespaces: remove it completely
            // this is usually the inserted block between a wurblet-directive and the
            // generated target.
            i--;
            deleteElement(i);
          }
          else  {
            // is a mixture of user-code + inserted whitespaces
            String elemText = elem.getText();
            int len = elemText.length();
            int ndx = elemText.lastIndexOf('\n');   // find last newline --> start of inserted guarded block

            if (ndx >= 0) {
              // check that all characters following the ndx are whitespaces
              for (int n=ndx; n < len; n++) {
                if (!Character.isWhitespace(elemText.charAt(n)))  {
                  return i; // no: how can that be??
                }
              }
              // shorten the block
              elem.deleteText(ndx, len);
            }
          }
        }
        return i;
      }
    }
    return -1;
  }


  /**
   * Removes a guarded section with a given name.
   *
   * @param guardedName the name of the guarded section to remove
   *
   * @return the index &ge; 0 if removed, -1 if no such section
   */
  public int deleteGuarded(String guardedName)  {
    return deleteGuarded(guardedName, 0);
  }


  /**
   * Replaces a guarded section.
   *
   * @param guardType the guarded region type
   * @param foldType the editor fold type (currently only supported for Netbeans), null if none
   * @param guardedName the name of the guarded section to replace
   * @param str the new text
   * @param wurbletElement the wurblet element, null if none
   *
   * @return the index &ge; of the replaced element, -1 if not found
   *
   * @throws org.wurbelizer.wurbel.SourceException if operation failed
   */
  public int replaceGuarded(SourceRegion guardType, SourceRegion.FoldType foldType,
                            String guardedName, String str,
                            SourceWurbletSubElement wurbletElement) throws SourceException {
    int ndx = deleteGuarded(guardedName);
    if (ndx >= 0) {
      insertGuarded(guardType, foldType, guardedName, str, ndx, wurbletElement);
      return ndx;
    }
    return ndx;
  }


  /**
   * Replaces an inline wurblet section.
   * Generated inline sections are enclosed between two empty
   * block comments within a CODE-element. Notice that empty block comments
   * are <strong>not</strong> treated as regular comments but as code!
   * Furthermore, inline sections are not guarded, i.e. any manual change
   * will be overwritten by the next wurblet invocation.
   *
   * @param str the new text
   * @param elemNdx the index of the SourceElement
   * @param wurblet the wurblet
   *
   * @throws org.wurbelizer.wurbel.SourceException if operaton failed
   */
  public void replaceInline(String str, int elemNdx, SourceWurbletSubElement wurblet) throws SourceException {
    // adjust element index
    if (wurblet.getParentWurbletElement() == null) {
      if (wurblet.isInlinePreviousBlock()) {
        elemNdx--;
      }
      else {
        elemNdx++;
      }
    }

    if (elemNdx >= 0 && elemNdx < elements.size())  {
      SourceElement elem = elements.get(elemNdx);
      int elemBegin = elem.getBegin();
      int elemEnd = elem.getEnd();
      if (wurblet.isInlinePreviousBlock()) {
        elemEnd = wurblet.getWurbletBegin();
      }
      else {
        elemBegin = wurblet.getWurbletEnd();
      }
      String elemText = text.substring(elemBegin, elemEnd);

      int start = -1;
      int end = -1;
      if (wurblet.isInlinePreviousBlock()) {
        end = elemText.lastIndexOf(INLINE_COMMENT);
        if (end >= 0) {
          start = elemText.substring(0, end).lastIndexOf(INLINE_COMMENT);
          if (start >= 0) {
            start += 4;
          }
        }
      }
      else {
        start = elemText.indexOf(INLINE_COMMENT);
        if (start >= 0) {
          start += 4;
          end = elemText.indexOf(INLINE_COMMENT, start);
        }
      }

      if (start >= 0 && end >= start) {
        // found! Replace the text in between
        start += elem.getBegin();   // transform to document index
        end   += elem.getBegin();
        int diff = str.length() - end + start;  // difference in length: new - old
        text.replace(start, end, str);  // replace
        elem.setEnd(elem.getEnd() + diff);
        elemNdx++;
        while (elemNdx < elements.size()) {
          elem = elements.get(elemNdx);
          elem.move(diff);
          elemNdx++;
        }
        return;
      }
      throw new SourceException(elem, "no /**/.../**/ found in code section for inline wurblet " + wurblet);
    }
    else  {
      throw new SourceException(this, "inline wurblet out of document bounds: " + wurblet);
    }
  }


  /**
   * Removes an element at given index.
   *
   * @param elemNdx the index to elements
   */
  public void deleteElement(int elemNdx)  {
    if (elemNdx >= 0 && elemNdx < elements.size())  {
      SourceElement elem = elements.get(elemNdx);
      int begin = elem.getBegin();
      int end   = elem.getEnd();
      int len   = end - begin;
      text.delete(begin, end);
      elements.remove(elemNdx);
      for (int i=elemNdx; i < elements.size(); i++) {
        elem = elements.get(i);
        elem.setBegin(elem.getBegin() - len);
        elem.setEnd(elem.getEnd() - len);
      }
    }
  }


  /**
   * Gets the line number for a given character position.
   * For performance reasons line number or not calculated when parsing
   * the document but only when printing an error message.
   *
   * @param pos the character position in the document.
   *
   * @return the line number.
   */
  public int getLineNumber(int pos) {
    int line = 0;
    int ndx = -1;

    do {
      line++;
      ndx = text.indexOf("\n", ndx + 1);
    }
    while (ndx >= 0 && ndx < pos);

    return line;
  }


  /**
   * Prints the name of the document without the package name.
   *
   * @return the name of the document
   */
  @Override
  public String toString() {
    int ndx = name.lastIndexOf(File.separatorChar);
    return ndx > 0 ? name.substring(ndx+1) : name;
  }


  /**
   * Gets the name of the document.
   *
   * @return the document name
   */
  public String getName() {
    return name;
  }

  /**
   * Gets the contents of the whole document.
   *
   * @return the document's content
   */
  public StringBuilder getText() {
    return text;
  }


  /**
   * Gets the optional properties for this document
   *
   * @return the properties
   */
  public Properties getProperties() {
    return wurbProps;
  }


  /**
   * Sets the text of the document.
   * The text will be parsed and separated into elements.
   *
   * @param text the contents of the document
   *
   * @throws SourceException if parsing the contents failed
   */
  public void setText(StringBuilder text) throws SourceException {

    this.text = text;

    // scan the file (for speed this is not generic but for well-known java-syntax)
    elements = new ArrayList<>();

    // first pass: extract only the comment blocks

    int begin               = 0;              // begin of current element
    SourceElement.Type type = null;           // type of element (initially unknown)
    int remains             = text.length();  // remaining text length

    for (int pos=0; pos < text.length(); pos++, remains--) {
      if (type == null) {
        char c = text.charAt(pos);
        if (c == '/' && remains > 0) {
          c = text.charAt(pos + 1);
          if      (c == '/')  {
            type = SourceElement.Type.SINGLE_COMMENT;
            begin = pos;
          }
          else if (c == '*')  {
            // block comment start (if not /**/)
            if (remains < 3 || text.charAt(pos+2) != '*' || text.charAt(pos+3) != '/') {
              type = SourceElement.Type.BLOCK_COMMENT;
              begin = pos;
            }
            else  {
              // skip **/
              pos += 3;
              remains -= 3;
            }
          }
        }
      }
      else  {
        boolean endFound = false;
        if (type == SourceElement.Type.SINGLE_COMMENT)  {
          if (text.charAt(pos) == '\n')  {
            endFound = true;
          }
        }
        else {
          // type is block-comment
          endFound = remains > 1 && text.charAt(pos) == '*' && text.charAt(pos + 1) == '/';
          if (endFound) {
            pos++;
            remains--;
          }
        }
        if (endFound) {
          SourceElement element = new SourceElement(this, type, begin, pos + 1, null);
          if (verbosity.isDebug()) {
            logger.info("adding source element " + element);
          }
          elements.add(element);
          begin = pos + 1;
          type  = null;
        }
      }
    }

    // concat single-line comments if ended with a backslash
    SourceElement lastElem = null;    // != null if SINGLE_COMMENT and ends with backslash
    for (Iterator<SourceElement> iter = elements.iterator(); iter.hasNext(); ) {
      SourceElement elem = iter.next();
      if (elem.getType() == SourceElement.Type.SINGLE_COMMENT) {
        if (lastElem != null) {
          lastElem.setEnd(elem.getEnd());
          lastElem.scan();
          iter.remove();
          elem = lastElem;
        }
        String str = elem.getText();
        lastElem = str.endsWith("\\\n") || str.endsWith("\\\r\n") ? elem : null;
      }
      else {
        lastElem = null;
      }
    }

    // now check single-line comments for guarded patterns
    List<SourceElement> guardedList = new ArrayList<>();    // new processed element list with guarded sections
    String guardedName = null;                              // != null if in guarded region
    SourceElement guardedStart = null;                      // != null if first guarded start element
    SourceRegion guardedType = null;                        // != null if in guarded region
    for (SourceElement elem : elements) {
      if (guardedName != null) {
        // looking for end of guarded block (guarded types are not mixed within a single file!)
        if (elem.getType() == SourceElement.Type.SINGLE_COMMENT && guardedType.isEnd(elem.getText(), guardedName)) {
          // block ends here: combine all segments to one guarded segment
          SourceElement guardElem = new SourceElement(this, SourceElement.Type.GUARDED, guardedStart.getBegin(), elem.getEnd(), null);
          guardElem.setGuardedName(guardedName);
          if (verbosity.isDebug()) {
            logger.info("found guarded section " + guardElem);
          }
          guardedList.add(guardElem);
          guardedName = null;
          guardedType = null;
        }
      }
      else {
        // looking for start of next guarded block
        if (elem.getType() == SourceElement.Type.SINGLE_COMMENT) {
          String elemText = elem.getText();
          for (SourceRegion region : SourceRegion.getAll()) {
            guardedName = region.getBeginName(elemText);
            if (guardedName != null) {
              guardedType = region;
              guardedStart = elem;
              break;
            }
            else {
              guardedName = region.getEndName(elemText);
              if (guardedName != null) {
                for (SourceElement element : guardedList) {
                  if (element.isGuarded() && guardedName.equals(element.getGuardedName())) {
                    // hangover from c&p, whatever
                    throw new SourceException(elem, "odd end of guarded section '" + guardedName + "', duplicates " + element);
                  }
                }
                throw new SourceException(elem, "missing start of guarded section '" + guardedName + "'");
              }
            }
          }
        }
        if (guardedName == null) {
          // some other section
          if (verbosity.isDebug()) {
            logger.info("found section " + elem);
          }
          guardedList.add(elem);
        }
      }
    }

    if (guardedStart != null && guardedName != null) {
      /*
       * Orphan end tags are simply ignored, but orphan start tags effectively stop further processing
       * up to the end of the source document.
       * Hence, we must abort the build process with an error.
       * Notice that duplicate or unreferenced blocks will be detected later.
       */
      for (SourceElement element : guardedList) {
        if (element.isGuarded() && guardedName.equals(element.getGuardedName())) {
          // hangover from c&p, whatever
          throw new SourceException(guardedStart, "odd start of guarded section '" + guardedName + "', duplicates " + element);
        }
      }
      throw new SourceException(guardedStart, "missing end of guarded section '" + guardedName + "'");
    }

    elements = guardedList;

    // phase 3: insert CODE-elements for the rest
    List<SourceElement> docList = new ArrayList<>();
    int pos = 0;      // position in text
    for (SourceElement elem : elements) {
      if (elem.getBegin() > pos) {
        SourceElement code = new SourceElement(this, SourceElement.Type.CODE, pos, elem.getBegin(), null);
        if (verbosity.isDebug()) {
          logger.info("adding code element " + code);
        }
        docList.add(code);
      }
      docList.add(elem);
      pos = elem.getEnd();
    }
    if (pos < text.length())  {
      SourceElement code = new SourceElement(this, SourceElement.Type.CODE, pos, text.length(), null);
      if (verbosity.isDebug()) {
        logger.info("adding code element " + code);
      }
      docList.add(code);
    }
    elements = docList;
  }


  /**
   * Gets the list of all elements of this document.
   *
   * @return the elements
   */
  public List<SourceElement> getElements() {
    return elements;
  }


  /**
   * Checks that the guarded blocks are unique among the whole document.
   *
   * @throws SourceException if not unique
   */
  public void checkGuardedElements() throws SourceException {
    StringBuilder buf = new StringBuilder();
    Map<String, SourceElement> guardedElements = new HashMap<>();
    for (SourceElement elem : elements) {
      if (elem.isGuarded()) {
        String guardName = elem.getGuardedName();
        if (guardName != null && !guardName.isBlank()) {
          SourceElement other = guardedElements.put(guardName, elem);
          if (other != null) {
            buf.append("\nguarded block '").append(guardName).append("' found in ")
               .append(other).append(" and ").append(elem);
          }
        }
      }
    }
    if (buf.length() > 0) {
      throw new SourceException(buf.toString());
    }
  }


  /**
   * Removes characters in text and update elements.
   * Assumes start/end does not overlap elements and
   * elements won't get a length of zero, i.e. need not be deleted.
   * Package scope!
   *
   * @param start the start character index (>=0)
   * @param end the end index (>=start)
   */
  void deleteText(int start, int end)  {
    text.delete(start, end);
    int diff = end - start;
    for (SourceElement elem: getElements()) {
      if (elem.getEnd() > start)  {
        if (elem.getBegin() >= end)  {
          elem.setBegin(elem.getBegin() - diff);
        }
        elem.setEnd(elem.getEnd() - diff);
      }
    }
  }

}
