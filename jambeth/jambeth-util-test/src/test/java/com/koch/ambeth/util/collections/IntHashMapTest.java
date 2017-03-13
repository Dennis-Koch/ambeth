package com.koch.ambeth.util.collections;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.junit.Assert;
import org.junit.Test;

import com.koch.ambeth.util.collections.IntArrayList;
import com.koch.ambeth.util.collections.IntHashMap;
import com.koch.ambeth.util.collections.IntHashMap.IntHashMapEntry;
import com.koch.ambeth.util.collections.IntHashMap.IntIterator;

public class IntHashMapTest
{
	protected int[] existingKeys = { 5, 6, 7, 8, 9 };

	protected int[] existingValues = { 15, 16, 17, 18, 19 };

	protected int[] nonExistingKeys = { 1, 2, 3, 4 };

	protected int expectedTestCount = 80 + existingKeys.length;

	protected IntHashMap fillForTest()
	{
		IntHashMap map = new IntHashMap();
		for (int a = 100; a-- > 20;)
		{
			map.put(a, a + 15);
		}
		for (int a = existingKeys.length; a-- > 0;)
		{
			map.put(existingKeys[a], existingValues[a]);
		}
		return map;
	}

	@Test
	public void ctorDefault()
	{
		new IntHashMap();
	}

	@Test
	public void ctorCapacity()
	{
		new IntHashMap(100);
	}

	@Test
	public void ctorCapacityAndFactor()
	{
		new IntHashMap(100, 0.75f);
	}

	@Test
	public void init()
	{
		IntHashMap map = new IntHashMap(100, 0.75f);
		map.init();
	}

	@Test
	public void containsKey()
	{
		IntHashMap map = fillForTest();

		Assert.assertTrue(map.containsKey(existingKeys[0]));
		Assert.assertFalse(map.containsKey(nonExistingKeys[0]));
	}

	@Test
	public void clear()
	{
		IntHashMap map = fillForTest();

		Assert.assertEquals(expectedTestCount, map.size());
		map.clear();
		Assert.assertEquals(0, map.size());
	}

	@Test
	public void containsValue()
	{
		IntHashMap map = fillForTest();

		Assert.assertTrue(map.containsValue(existingValues[0]));
		Assert.assertFalse(map.containsValue(-1));
	}

	@Test
	public void size()
	{
		IntHashMap map = fillForTest();

		Assert.assertEquals(expectedTestCount, map.size());
	}

	@Test
	public void isEmpty()
	{
		IntHashMap map = fillForTest();

		map.clear();

		Assert.assertTrue(map.isEmpty());
		Assert.assertEquals(0, map.size());
	}

	@Test
	public void get()
	{
		IntHashMap map = fillForTest();

		for (int a = existingKeys.length; a-- > 0;)
		{
			Assert.assertEquals(existingValues[a], map.get(existingKeys[a]));
		}
	}

	@Test
	public void put()
	{
		IntHashMap map = fillForTest();

		for (int a = existingKeys.length; a-- > 0;)
		{
			Assert.assertTrue(map.containsKey(existingKeys[a]));
		}
	}

	@Test
	public void putIfNotExists()
	{
		IntHashMap map = fillForTest();

		for (int a = nonExistingKeys.length; a-- > 0;)
		{
			Assert.assertTrue(map.putIfNotExists(nonExistingKeys[a], 1));
		}
		Assert.assertFalse(map.putIfNotExists(nonExistingKeys[0], 2));
		Assert.assertEquals(1, map.get(nonExistingKeys[0]));
		Assert.assertEquals(expectedTestCount + nonExistingKeys.length, map.size());
	}

	@Test
	public void remove()
	{
		IntHashMap map = fillForTest();

		for (int a = existingKeys.length; a-- > 0;)
		{
			Assert.assertEquals(existingValues[a], map.remove(existingKeys[a]));
		}
		Assert.assertEquals(0, map.remove(nonExistingKeys[0]));
		Assert.assertEquals(expectedTestCount - existingKeys.length, map.size());
	}

	@Test
	public void removeIfObject()
	{
		IntHashMap map = fillForTest();

		for (int a = existingKeys.length; a-- > 0;)
		{
			Assert.assertFalse(map.removeIfObject(existingKeys[a], -1));
		}
		for (int a = nonExistingKeys.length; a-- > 0;)
		{
			Assert.assertFalse(map.removeIfObject(nonExistingKeys[a], -1));
		}
		for (int a = existingKeys.length; a-- > 0;)
		{
			Assert.assertTrue(map.removeIfObject(existingKeys[a], existingValues[a]));
		}
		Assert.assertEquals(expectedTestCount - existingKeys.length, map.size());
	}

	@Test
	public void values()
	{
		IntHashMap map = fillForTest();

		IntArrayList valuesList = map.values();
		Assert.assertEquals(map.size(), valuesList.size);
	}

	@Test
	public void serialize() throws Throwable
	{
		IntHashMap map = fillForTest();

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
			IntHashMap cloneMap = (IntHashMap) is.readObject();
			Assert.assertEquals(map.size(), cloneMap.size());

			IntIterator iter = map.iterator();
			while (iter.hasNext())
			{
				IntHashMapEntry entry = iter.nextEntry();
				int cloneValue = cloneMap.remove(entry.getKey());
				Assert.assertEquals(entry.getValue(), cloneValue);
			}
			Assert.assertEquals(0, cloneMap.size());
		}
	}
}
