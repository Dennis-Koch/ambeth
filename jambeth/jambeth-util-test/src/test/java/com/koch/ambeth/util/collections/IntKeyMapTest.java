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

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.koch.ambeth.util.ParamHolder;
import com.koch.ambeth.util.collections.IntKeyMap;

public class IntKeyMapTest
{
	@Test
	public void ctorDefault()
	{
		new IntKeyMap<Object>();
	}

	@Test
	public void ctorCapacity()
	{
		new IntKeyMap<Object>(100);
	}

	@Test
	public void ctorCapacityAndFactor()
	{
		new IntKeyMap<Object>(100, 0.75f);
	}

	@Test
	public void init()
	{
		IntKeyMap<Object> map = new IntKeyMap<Object>(100, 0.75f);
		map.init();
	}

	@Test
	public void containsKey()
	{
		IntKeyMap<Object> map = new IntKeyMap<Object>(100, 0.75f);

		map.put(5, new Object());
		map.put(6, new Object());
		map.put(7, new Object());

		Assert.assertTrue(map.containsKey(6));
		Assert.assertFalse(map.containsKey(77));
	}

	@Test
	public void clear()
	{
		IntKeyMap<Object> map = new IntKeyMap<Object>();

		map.putIfNotExists(new Object());
		map.putIfNotExists(new Object());
		map.putIfNotExists(new Object());
		map.putIfNotExists(new Object());

		Assert.assertEquals(4, map.size());
		map.clear();
		Assert.assertEquals(0, map.size());
	}

	@Test
	public void containsValue()
	{
		IntKeyMap<Object> map = new IntKeyMap<Object>(100, 0.75f);

		Object value = new Object();
		map.put(5, value);

		Assert.assertTrue(map.containsValue(value));
		Assert.assertFalse(map.containsValue(new Object()));
	}

	@Test
	public void size()
	{
		IntKeyMap<Object> map = new IntKeyMap<Object>(100, 0.75f);

		map.put(5, new Object());
		map.put(6, new Object());
		map.put(7, new Object());

		Assert.assertEquals(3, map.size());
	}

	@Test
	public void isEmpty()
	{
		IntKeyMap<Object> map = new IntKeyMap<Object>(100, 0.75f);

		map.put(5, new Object());
		map.put(6, new Object());
		map.put(7, new Object());

		map.clear();

		Assert.assertTrue(map.isEmpty());
	}

	@Test
	public void get()
	{
		IntKeyMap<Object> map = new IntKeyMap<Object>(100, 0.75f);

		Object obj1 = new Object();
		Object obj2 = new Object();
		Object obj3 = new Object();
		map.put(100, obj1);
		map.put(200, obj2);
		map.put(300, obj3);

		Assert.assertSame(obj1, map.get(100));
		Assert.assertSame(obj2, map.get(200));
		Assert.assertSame(obj3, map.get(300));
	}

	@Test
	public void put()
	{
		IntKeyMap<Object> map = new IntKeyMap<Object>(100, 0.75f);

		map.put(5, new Object());
		map.put(6, new Object());
		map.put(7, new Object());

		Assert.assertTrue(map.containsKey(5));
		Assert.assertTrue(map.containsKey(6));
		Assert.assertTrue(map.containsKey(7));
	}

	@Test
	public void putIfNotExistsIntV()
	{
		IntKeyMap<Object> map = new IntKeyMap<Object>();

		map.putIfNotExists(5, new Object());
		map.putIfNotExists(6, new Object());
		map.putIfNotExists(7, new Object());
		Assert.assertFalse(map.putIfNotExists(5, new Object()));

		Assert.assertTrue(map.containsKey(5));
		Assert.assertTrue(map.containsKey(6));
		Assert.assertTrue(map.containsKey(7));
		Assert.assertEquals(3, map.size());
	}

	@Test
	public void putIfNotExistsV()
	{
		IntKeyMap<Object> map = new IntKeyMap<Object>();

		int randomId1 = map.putIfNotExists(new Object());
		int randomId2 = map.putIfNotExists(new Object());
		int randomId3 = map.putIfNotExists(new Object());
		int randomId4 = map.putIfNotExists(new Object());
		int randomId5 = map.putIfNotExists(new Object());

		Assert.assertTrue(map.containsKey(randomId1));
		Assert.assertTrue(map.containsKey(randomId2));
		Assert.assertTrue(map.containsKey(randomId3));
		Assert.assertTrue(map.containsKey(randomId4));
		Assert.assertTrue(map.containsKey(randomId5));
		Assert.assertEquals(5, map.size());
	}

	@Test
	public void remove()
	{
		IntKeyMap<Object> map = new IntKeyMap<Object>(100, 0.75f);

		Object value = new Object();
		map.put(5, value);

		Assert.assertSame(value, map.remove(5));
		Assert.assertEquals(0, map.size());
	}

	@Test
	public void removeIfObject()
	{
		IntKeyMap<Object> map = new IntKeyMap<Object>(100, 0.75f);

		Object value = new Object();
		map.put(5, value);

		Assert.assertFalse(map.removeIfObject(5, new Object()));
		Assert.assertFalse(map.removeIfObject(6, value));
		Assert.assertTrue(map.removeIfObject(5, value));
		Assert.assertEquals(0, map.size());
	}

	@Test
	public void removeAndNotify()
	{
		final ParamHolder<Boolean> notified = new ParamHolder<Boolean>(Boolean.FALSE);
		IntKeyMap<Object> map = new IntKeyMap<Object>(100, 0.75f)
		{
			@Override
			protected void disposeEntry(IntKeyMapEntry<Object> entry)
			{
				notified.setValue(Boolean.TRUE);
			};
		};
		Object value = new Object();
		map.put(5, value);

		map.removeAndNotify(5);
		Assert.assertEquals(Boolean.TRUE, notified.getValue());
	}

	@Test
	public void values()
	{
		IntKeyMap<Object> map = new IntKeyMap<Object>();
		Object[] values = new Object[5];
		for (int a = values.length; a-- > 0;)
		{
			values[a] = new Object();
			map.putIfNotExists(values[a]);
		}
		List<Object> valuesList = map.values();
		Assert.assertEquals(values.length, valuesList.size());
		for (Object value : values)
		{
			valuesList.remove(value);
		}
		Assert.assertEquals(0, valuesList.size());
	}
}
