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

import org.wurbelizer.wurblet.AbstractWurblet;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Thread-safe test wurblet tracking the number of concurrent invocations.<br>
 * The first invocation waits until a second one runs concurrently (or times out).
 *
 * @see WurbletThreadSafetyTest
 */
public class ParallelTestWurblet extends AbstractWurblet {

  static final AtomicInteger CURRENT = new AtomicInteger();
  static final AtomicInteger MAX = new AtomicInteger();
  static volatile CountDownLatch meet = new CountDownLatch(2);

  @Override
  public boolean isThreadSafe() {
    return true;
  }

  @Override
  public void run() throws WurbelException {
    super.run();
    MAX.accumulateAndGet(CURRENT.incrementAndGet(), Math::max);
    try {
      meet.countDown();
      meet.await(5, TimeUnit.SECONDS);
    }
    catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
      throw new WurbelException("interrupted", ex);
    }
    finally {
      CURRENT.decrementAndGet();
    }
    out.println("  // generated");
  }

}
