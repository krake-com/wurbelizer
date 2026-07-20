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

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * Tests {@link ExceptionHelper#concatenateMessages(Throwable)}.
 */
public class ExceptionHelperTest {

  @Test
  public void testSingleException() {
    assertEquals(ExceptionHelper.concatenateMessages(new Exception("boom")), "boom");
  }

  @Test
  public void testChain() {
    Throwable t = new Exception("top", new IllegalStateException("middle", new RuntimeException("root")));
    assertEquals(ExceptionHelper.concatenateMessages(t), "top\nmiddle\nroot");
  }

  @Test
  public void testDuplicateMessageSkipped() {
    // the cause message is already contained in the head, so it must not be appended again
    Throwable t = new Exception("already contains detail", new Exception("detail"));
    assertEquals(ExceptionHelper.concatenateMessages(t), "already contains detail");
  }

  @Test
  public void testNullHeadMessage() {
    Throwable t = new Exception((String) null, new Exception("cause"));
    assertEquals(ExceptionHelper.concatenateMessages(t), "cause");
  }

  @Test
  public void testNullCauseMessageSkipped() {
    Throwable t = new Exception("head", new Exception((String) null));
    assertEquals(ExceptionHelper.concatenateMessages(t), "head");
  }
}
