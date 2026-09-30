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
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wurbelizer.misc.DefaultLogger;
import org.wurbelizer.misc.Verbosity;
import org.wurbelizer.wurblet.AbstractWurblet;
import org.wurbelizer.wurblet.Wurblet;
import org.wurbelizer.wurblet.WurbletData;

import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Stream;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Tests that only thread-safe wurblets are executed concurrently.
 */
public class WurbletThreadSafetyTest {

  private static final int THREADS = 4;

  private final List<Path> dirs = new ArrayList<>();

  @BeforeClass
  public void createFixedTextObjects() throws Exception {
    // the wurbler expects a <wurblet>.ser next to the wurblet's class
    for (Class<? extends Wurblet> clazz : List.of(SerialTestWurblet.class, ParallelTestWurblet.class)) {
      Path classFile = Path.of(clazz.getResource(clazz.getSimpleName() + ".class").toURI());
      Path serFile = classFile.resolveSibling(clazz.getSimpleName() + ".ser");
      try (ObjectOutputStream os = new ObjectOutputStream(Files.newOutputStream(serFile))) {
        os.writeObject(new WurbletData(new String[0], null));
      }
    }
  }

  @AfterMethod
  public void deleteDirs() throws IOException {
    for (Path dir : dirs) {
      try (Stream<Path> paths = Files.walk(dir)) {
        paths.sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
      }
    }
    dirs.clear();
  }

  @Test
  public void testDefaultIsNotThreadSafe() {
    assertFalse(new AbstractWurblet() {}.isThreadSafe());
  }

  @Test
  public void testNonThreadSafeWurbletsRunSequentially() throws Exception {
    SerialTestWurblet.CURRENT.set(0);
    SerialTestWurblet.MAX.set(0);
    wurbelConcurrently("SerialTestWurblet");
    assertEquals(SerialTestWurblet.MAX.get(), 1);
  }

  @Test
  public void testThreadSafeWurbletsRunConcurrently() throws Exception {
    ParallelTestWurblet.CURRENT.set(0);
    ParallelTestWurblet.MAX.set(0);
    ParallelTestWurblet.meet = new CountDownLatch(2);
    wurbelConcurrently("ParallelTestWurblet");
    assertTrue(ParallelTestWurblet.MAX.get() > 1, "max concurrency " + ParallelTestWurblet.MAX.get());
  }


  /**
   * Wurbels a source with the given wurblet in several threads, each with its own context.
   *
   * @param wurbletName the wurblet name
   */
  private void wurbelConcurrently(String wurbletName) throws Exception {
    CountDownLatch start = new CountDownLatch(1);
    List<Future<WurbelResult>> results = new ArrayList<>();
    try (ExecutorService executor = Executors.newFixedThreadPool(THREADS)) {
      for (int t = 0; t < THREADS; t++) {
        Path dir = Files.createTempDirectory("wurblets");
        dirs.add(dir);
        Path source = Files.writeString(dir.resolve("Src.java"),
                                        "public class Src {\n\n  // @wurblet gen " + wurbletName + "\n\n}\n",
                                        StandardCharsets.UTF_8);
        results.add(executor.submit(() -> WurbelContext.call(new WurbelContext(), () -> {
          start.await();
          return new SourceWurbler(source.toString(), null, new String[] { "org.wurbelizer.wurbel" }, dir.toString(),
                                   1, null, false, new DefaultLogger(), Verbosity.DEFAULT,
                                   new HashSet<>(), new HashSet<>()).wurbel();
        })));
      }
      start.countDown();
      for (Future<WurbelResult> future : results) {
        WurbelResult result = future.get();
        assertEquals(result.errors(), 0);
        assertEquals(result.updateType(), WurbelResult.UpdateType.UPDATED);
      }
    }
    for (Path dir : dirs) {
      assertTrue(Files.readString(dir.resolve("Src.java"), StandardCharsets.UTF_8).contains("// generated"));
    }
  }

}
