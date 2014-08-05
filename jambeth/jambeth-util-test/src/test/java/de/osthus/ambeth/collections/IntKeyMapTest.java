package de.osthus.ambeth.collections;

import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

public class IntKeyMapTest
{
	@BeforeClass
	public static void setUpBeforeClass() throws Exception
	{
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception
	{
	}

	@Before
	public void setUp() throws Exception
	{
	}

	@After
	public void tearDown() throws Exception
	{
	}

	@Test
	public void testIntKeyMap()
	{
		new IntKeyMap<Object>();
	}

	@Test
	public void testIntKeyMapInt()
	{
		new IntKeyMap<Object>(100);
	}

	@Test
	public void testIntKeyMapIntFloat()
	{
		new IntKeyMap<Object>(100, 0.75f);
	}

	@Test
	public void testInit()
	{
		IntKeyMap<Object> map = new IntKeyMap<Object>(100, 0.75f);
		map.init();
	}

	@Test
	public void testSize()
	{
		IntKeyMap<Object> map = new IntKeyMap<Object>(100, 0.75f);

		map.put(5, new Object());
		map.put(6, new Object());
		map.put(7, new Object());

		Assert.assertEquals(3, map.size());
	}

	@Test
	public void testIsEmpty()
	{
		IntKeyMap<Object> map = new IntKeyMap<Object>(100, 0.75f);

		map.put(5, new Object());
		map.put(6, new Object());
		map.put(7, new Object());

		map.clear();

		Assert.assertTrue(map.isEmpty());
	}

	@Test
	public void testGet()
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
	public void testContainsKey()
	{
		IntKeyMap<Object> map = new IntKeyMap<Object>(100, 0.75f);

		map.put(5, new Object());
		map.put(6, new Object());
		map.put(7, new Object());

		Assert.assertTrue(map.containsKey(6));
		Assert.assertFalse(map.containsKey(77));
	}

	@Test
	public void testPut()
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
	public void testPutIfNotExistsIntV()
	{
		IntKeyMap<Object> map = new IntKeyMap<Object>(100, 0.75f);

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
	@Ignore
	public void testPutIfNotExistsV()
	{
		fail("Not yet implemented");
	}

	@Test
	@Ignore
	public void testContainsValue()
	{
		fail("Not yet implemented");
	}

	@Test
	@Ignore
	public void testRemove()
	{
		fail("Not yet implemented");
	}

	@Test
	@Ignore
	public void testRemoveIfObject()
	{
		fail("Not yet implemented");
	}

	@Test
	@Ignore
	public void testRemoveAndNotify()
	{
		fail("Not yet implemented");
	}

	@Test
	@Ignore
	public void testClear()
	{
		fail("Not yet implemented");
	}

	@Test
	@Ignore
	public void testValues()
	{
		fail("Not yet implemented");
	}
}
