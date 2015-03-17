package de.osthus.ambeth.collections;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.junit.Assert;
import org.junit.Test;

public class Tuple2KeyHashMapTest
{
	protected Integer[] existingKeys1 = { 5, 6, 7, 8, 9 };

	protected Long[] existingKeys2 = { 25L, 26L, 27L, 28L, 29L };

	protected Object[] existingValues = { 15, 16, 17, 18, 19 };

	protected Integer[] nonExistingKeys1 = { 1, 2, 3, 4 };

	protected Long[] nonExistingKeys2 = { 1L, 2L, 3L, 4L };

	protected int expectedTestCount = 80 + existingKeys1.length;

	protected Tuple2KeyHashMap<Integer, Long, Object> fillForTest()
	{
		Tuple2KeyHashMap<Integer, Long, Object> map = new Tuple2KeyHashMap<Integer, Long, Object>();
		for (int a = 100; a-- > 20;)
		{
			map.put(Integer.valueOf(a + 20), Long.valueOf(a + 35), a + 15);
		}
		for (int a = existingKeys1.length; a-- > 0;)
		{
			map.put(existingKeys1[a], existingKeys2[a], existingValues[a]);
		}
		return map;
	}

	@Test
	public void ctorDefault()
	{
		new Tuple2KeyHashMap<Integer, Long, Object>();
	}

	@Test
	public void ctorCapacity()
	{
		new Tuple2KeyHashMap<Integer, Long, Object>(100);
	}

	@Test
	public void ctorCapacityAndFactor()
	{
		new Tuple2KeyHashMap<Integer, Long, Object>(100, 0.75f);
	}

	@Test
	public void init()
	{
		Tuple2KeyHashMap<Integer, Long, Object> map = new Tuple2KeyHashMap<Integer, Long, Object>(100, 0.75f);
		map.init();
	}

	@Test
	public void containsKey()
	{
		Tuple2KeyHashMap<Integer, Long, Object> map = fillForTest();

		Assert.assertTrue(map.containsKey(existingKeys1[0], existingKeys2[0]));
		Assert.assertFalse(map.containsKey(nonExistingKeys1[0], nonExistingKeys2[0]));
	}

	@Test
	public void clear()
	{
		Tuple2KeyHashMap<Integer, Long, Object> map = fillForTest();

		Assert.assertEquals(expectedTestCount, map.size());
		map.clear();
		Assert.assertEquals(0, map.size());
	}

	@Test
	public void containsValue()
	{
		Tuple2KeyHashMap<Integer, Long, Object> map = fillForTest();

		Assert.assertTrue(map.containsValue(existingValues[0]));
		Assert.assertFalse(map.containsValue(-1));
	}

	@Test
	public void size()
	{
		Tuple2KeyHashMap<Integer, Long, Object> map = fillForTest();

		Assert.assertEquals(expectedTestCount, map.size());
	}

	@Test
	public void isEmpty()
	{
		Tuple2KeyHashMap<Integer, Long, Object> map = fillForTest();

		map.clear();

		Assert.assertTrue(map.isEmpty());
		Assert.assertEquals(0, map.size());
	}

	@Test
	public void get()
	{
		Tuple2KeyHashMap<Integer, Long, Object> map = fillForTest();

		for (int a = existingKeys1.length; a-- > 0;)
		{
			Assert.assertEquals(existingValues[a], map.get(existingKeys1[a], existingKeys2[a]));
		}
	}

	@Test
	public void put()
	{
		Tuple2KeyHashMap<Integer, Long, Object> map = fillForTest();

		for (int a = existingKeys1.length; a-- > 0;)
		{
			Assert.assertTrue(map.containsKey(existingKeys1[a], existingKeys2[a]));
		}
	}

	@Test
	public void putIfNotExists()
	{
		Tuple2KeyHashMap<Integer, Long, Object> map = fillForTest();

		for (int a = nonExistingKeys1.length; a-- > 0;)
		{
			Assert.assertTrue(map.putIfNotExists(nonExistingKeys1[a], nonExistingKeys2[a], 1));
		}
		Assert.assertFalse(map.putIfNotExists(nonExistingKeys1[0], nonExistingKeys2[0], 2));
		Assert.assertEquals(1, map.get(nonExistingKeys1[0], nonExistingKeys2[0]));
		Assert.assertEquals(expectedTestCount + nonExistingKeys1.length, map.size());
	}

	@Test
	public void remove()
	{
		Tuple2KeyHashMap<Integer, Long, Object> map = fillForTest();

		for (int a = existingKeys1.length; a-- > 0;)
		{
			Assert.assertEquals(existingValues[a], map.remove(existingKeys1[a], existingKeys2[a]));
		}
		Assert.assertNull(map.remove(nonExistingKeys1[0], nonExistingKeys2[0]));
		Assert.assertEquals(expectedTestCount - existingKeys1.length, map.size());
	}

	@Test
	public void values()
	{
		Tuple2KeyHashMap<Integer, Long, Object> map = fillForTest();

		IList<Object> valuesList = map.values();
		Assert.assertEquals(map.size(), valuesList.size());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void serialize() throws Throwable
	{
		Tuple2KeyHashMap<Integer, Long, Object> map = fillForTest();

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
			Tuple2KeyHashMap<Integer, Long, Object> cloneMap = (Tuple2KeyHashMap<Integer, Long, Object>) is.readObject();
			Assert.assertEquals(map.size(), cloneMap.size());

			for (Tuple2KeyEntry<Integer, Long, Object> entry : map)
			{
				Object cloneValue = cloneMap.remove(entry.getKey1(), entry.getKey2());
				Assert.assertEquals(entry.getValue(), cloneValue);
			}
			Assert.assertEquals(0, cloneMap.size());
		}
	}
}
