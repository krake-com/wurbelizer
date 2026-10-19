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

import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;
import org.wurbelizer.misc.Settings;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;

/**
 * Tests the {@link WurbelContext}.
 */
public class WurbelContextTest {

  @AfterMethod
  public void reset() {
    HeapFileFactory.initialize();
    Settings.setEncodingCharset(StandardCharsets.UTF_8);
    WurbelHelper.setExtraProperties(null);
  }

  @Test
  public void testUnboundUsesGlobal() {
    assertSame(WurbelContext.current(), WurbelContext.global());
  }

  @Test
  public void testBoundContextIsolatesState() throws Exception {
    HeapFileFactory.create("h", "global");
    Properties globalProps = new Properties();
    WurbelHelper.setExtraProperties(globalProps);

    WurbelContext context = new WurbelContext();
    WurbelContext.call(context, () -> {
      assertSame(WurbelContext.current(), context);
      assertNull(HeapFileFactory.get("h"));
      assertNull(WurbelHelper.getExtraProperties());
      assertEquals(Settings.getEncodingCharset(), StandardCharsets.UTF_8);

      HeapFileFactory.create("h", "bound");
      WurbelHelper.setExtraProperties(new Properties());
      Settings.setEncodingCharset(StandardCharsets.ISO_8859_1);
      return null;
    });

    // global state untouched
    assertEquals(HeapFileFactory.get("h").getText(), "global");
    assertSame(WurbelHelper.getExtraProperties(), globalProps);
    assertEquals(Settings.getEncodingCharset(), StandardCharsets.UTF_8);

    // bound state kept in context
    assertEquals(context.heapFiles().get("h").getText(), "bound");
    assertNotNull(context.getExtraProperties());
    assertEquals(context.getEncodingCharset(), StandardCharsets.ISO_8859_1);
  }

  @Test
  public void testNestedBindingRestoresOuter() {
    WurbelContext outer = new WurbelContext();
    WurbelContext inner = new WurbelContext();
    WurbelContext.call(outer, () -> {
      WurbelContext.call(inner, () -> {
        assertSame(WurbelContext.current(), inner);
        return null;
      });
      assertSame(WurbelContext.current(), outer);
      return null;
    });
    assertSame(WurbelContext.current(), WurbelContext.global());
  }

  @Test
  public void testConcurrentContextsDontInterfere() throws Exception {
    Charset[] charsets = { StandardCharsets.UTF_8, StandardCharsets.ISO_8859_1, StandardCharsets.US_ASCII, StandardCharsets.UTF_16 };
    int threadCount = 8;
    int loops = 500;
    CountDownLatch start = new CountDownLatch(1);
    List<Future<String>> results = new ArrayList<>();

    try (ExecutorService executor = Executors.newFixedThreadPool(threadCount)) {
      for (int t = 0; t < threadCount; t++) {
        String id = Integer.toString(t);
        Charset charset = charsets[t % charsets.length];
        results.add(executor.submit(() -> WurbelContext.call(new WurbelContext(), () -> {
          start.await();
          for (int i = 0; i < loops; i++) {
            // same heapfile name and same property key in all threads
            HeapFileFactory.initialize();
            HeapFileFactory.createOrOverwrite("Src" + id + ".java", "shared", id);
            Properties props = new Properties();
            props.setProperty("id", id);
            WurbelHelper.setExtraProperties(props);
            Settings.setEncodingCharset(charset);
            Thread.yield();
            HeapFile heapFile = HeapFileFactory.get("shared");
            if (heapFile == null || !id.equals(heapFile.getText())) {
              return "heapfile of thread " + id + " modified: " + heapFile;
            }
            if (!id.equals(WurbelHelper.translateVariables("$id", null))) {
              return "extra properties of thread " + id + " modified";
            }
            if (!charset.equals(Settings.getEncodingCharset())) {
              return "charset of thread " + id + " modified";
            }
          }
          return null;
        })));
      }
      start.countDown();
      for (Future<String> result : results) {
        assertNull(result.get());
      }
    }
  }

}
