package com.koch.ambeth.util.collections;

/*-
 * #%L
 * jambeth-util-test
 * %%
 * Copyright (C) 2017 Koch Softwaredevelopment
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 * #L%
 */

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

public class Tuple3KeyHashMapTest {
    protected Integer[] existingKeys1 = { 5, 6, 7, 8, 9 };

    protected Long[] existingKeys2 = { 25L, 26L, 27L, 28L, 29L };

    protected Double[] existingKeys3 = { 25.0, 26.0, 27.0, 28.0, 29.0 };

    protected Object[] existingValues = { 15, 16, 17, 18, 19 };

    protected Integer[] nonExistingKeys1 = { 1, 2, 3, 4 };

    protected Long[] nonExistingKeys2 = { 1L, 2L, 3L, 4L };

    protected Double[] nonExistingKeys3 = { 1.0, 2.0, 3.0, 4.0 };

    protected int expectedTestCount = 80 + existingKeys1.length;

    protected Tuple3KeyHashMap<Integer, Long, Double, Object> fillForTest() {
        Tuple3KeyHashMap<Integer, Long, Double, Object> map = new Tuple3KeyHashMap<>();
        for (int a = 100; a-- > 20; ) {
            map.put(Integer.valueOf(a + 20), Long.valueOf(a + 35), Double.valueOf(a + 40), a + 15);
        }
        for (int a = existingKeys1.length; a-- > 0; ) {
            map.put(existingKeys1[a], existingKeys2[a], existingKeys3[a], existingValues[a]);
        }
        return map;
    }

    @Test
    public void ctorDefault() {
        new Tuple3KeyHashMap<Integer, Long, Double, Object>();
    }

    @Test
    public void ctorCapacity() {
        new Tuple3KeyHashMap<Integer, Long, Double, Object>(100);
    }

    @Test
    public void ctorCapacityAndFactor() {
        new Tuple3KeyHashMap<Integer, Long, Double, Object>(100, 0.75f);
    }

    @Test
    public void init() {
        Tuple3KeyHashMap<Integer, Long, Double, Object> map = new Tuple3KeyHashMap<>(100, 0.75f);
        map.init();
    }

    @Test
    public void containsKey() {
        Tuple3KeyHashMap<Integer, Long, Double, Object> map = fillForTest();

        Assert.assertTrue(map.containsKey(existingKeys1[0], existingKeys2[0], existingKeys3[0]));
        Assert.assertFalse(map.containsKey(nonExistingKeys1[0], nonExistingKeys2[0], nonExistingKeys3[0]));
    }

    @Test
    public void clear() {
        Tuple3KeyHashMap<Integer, Long, Double, Object> map = fillForTest();

        Assert.assertEquals(expectedTestCount, map.size());
        map.clear();
        Assert.assertEquals(0, map.size());
    }

    @Test
    public void containsValue() {
        Tuple3KeyHashMap<Integer, Long, Double, Object> map = fillForTest();

        Assert.assertTrue(map.containsValue(existingValues[0]));
        Assert.assertFalse(map.containsValue(-1));
    }

    @Test
    public void size() {
        Tuple3KeyHashMap<Integer, Long, Double, Object> map = fillForTest();

        Assert.assertEquals(expectedTestCount, map.size());
    }

    @Test
    public void isEmpty() {
        Tuple3KeyHashMap<Integer, Long, Double, Object> map = fillForTest();

        map.clear();

        Assert.assertTrue(map.isEmpty());
        Assert.assertEquals(0, map.size());
    }

    @Test
    public void get() {
        Tuple3KeyHashMap<Integer, Long, Double, Object> map = fillForTest();

        for (int a = existingKeys1.length; a-- > 0; ) {
            Assert.assertEquals(existingValues[a], map.get(existingKeys1[a], existingKeys2[a], existingKeys3[a]));
        }
    }

    @Test
    public void put() {
        Tuple3KeyHashMap<Integer, Long, Double, Object> map = fillForTest();

        for (int a = existingKeys1.length; a-- > 0; ) {
            Assert.assertTrue(map.containsKey(existingKeys1[a], existingKeys2[a], existingKeys3[a]));
        }
    }

    @Test
    public void putIfNotExists() {
        Tuple3KeyHashMap<Integer, Long, Double, Object> map = fillForTest();

        for (int a = nonExistingKeys1.length; a-- > 0; ) {
            Assert.assertTrue(map.putIfNotExists(nonExistingKeys1[a], nonExistingKeys2[a], nonExistingKeys3[a], 1));
        }
        Assert.assertFalse(map.putIfNotExists(nonExistingKeys1[0], nonExistingKeys2[0], nonExistingKeys3[0], 2));
        Assert.assertEquals(1, map.get(nonExistingKeys1[0], nonExistingKeys2[0], nonExistingKeys3[0]));
        Assert.assertEquals(expectedTestCount + nonExistingKeys1.length, map.size());
    }

    @Test
    public void remove() {
        Tuple3KeyHashMap<Integer, Long, Double, Object> map = fillForTest();

        for (int a = existingKeys1.length; a-- > 0; ) {
            Assert.assertEquals(existingValues[a], map.remove(existingKeys1[a], existingKeys2[a], existingKeys3[a]));
        }
        Assert.assertNull(map.remove(nonExistingKeys1[0], nonExistingKeys2[0], nonExistingKeys3[0]));
        Assert.assertEquals(expectedTestCount - existingKeys1.length, map.size());
    }

    @Test
    public void values() {
        Tuple3KeyHashMap<Integer, Long, Double, Object> map = fillForTest();

        List<Object> valuesList = map.values();
        Assert.assertEquals(map.size(), valuesList.size());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void serialize() throws Throwable {
        Tuple3KeyHashMap<Integer, Long, Double, Object> map = fillForTest();

        byte[] content;
        {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream os = new ObjectOutputStream(bos);
            os.writeObject(map);
            os.flush();
            content = bos.toByteArray();
        }
        {
            ByteArrayInputStream bis = new ByteArrayInputStream(content);
            ObjectInputStream is = new ObjectInputStream(bis);
            Tuple3KeyHashMap<Integer, Long, Double, Object> cloneMap = (Tuple3KeyHashMap<Integer, Long, Double, Object>) is.readObject();
            Assert.assertEquals(map.size(), cloneMap.size());

            for (Tuple3KeyEntry<Integer, Long, Double, Object> entry : map) {
                Object cloneValue = cloneMap.remove(entry.getKey1(), entry.getKey2(), entry.getKey3());
                Assert.assertEquals(entry.getValue(), cloneValue);
            }
            Assert.assertEquals(0, cloneMap.size());
        }
    }
}
