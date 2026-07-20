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

package org.wurbelizer.wurblet;

import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

/**
 * Tests the serializable {@link WurbletData} holder.
 */
public class WurbletDataTest {

  @Test
  public void testAccessors() {
    String[] source = {"line1", "line2"};
    String[] args = {"a", "b"};
    WurbletData data = new WurbletData(source, args);
    assertEquals(data.getSource(), source);
    assertEquals(data.getArgs(), args);
  }

  @Test
  public void testNullArgsAllowed() {
    WurbletData data = new WurbletData(new String[0], null);
    assertNull(data.getArgs());
  }

  @Test
  public void testSetters() {
    WurbletData data = new WurbletData(new String[0], null);
    data.setSource(new String[] {"x"});
    data.setArgs(new String[] {"y"});
    assertEquals(data.getSource(), new String[] {"x"});
    assertEquals(data.getArgs(), new String[] {"y"});
  }

  @Test
  public void testSerializationRoundTrip() throws IOException, ClassNotFoundException {
    WurbletData original = new WurbletData(new String[] {"src"}, new String[] {"arg"});

    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
      oos.writeObject(original);
    }

    WurbletData restored;
    try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
      restored = (WurbletData) ois.readObject();
    }

    assertEquals(restored.getSource(), new String[] {"src"});
    assertEquals(restored.getArgs(), new String[] {"arg"});
  }
}
