package de.osthus.ambeth.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

public class ListUtilTest
{
	protected static List<Object> expectedList;

	protected static Set<Object> expectedSet;

	protected static List<Object> emptyList = Collections.<Object> emptyList();

	@BeforeClass
	public static void setUpBeforeClass() throws Exception
	{
		expectedList = new ArrayList<Object>();
		expectedList.add(2);
		expectedList.add(ListUtilTest.class);
		expectedList.add("test");
		expectedList.add(ListUtilTest.class);

		expectedSet = new HashSet<Object>();
		expectedSet.addAll(expectedList);
	}

	@Test
	public void testToArray()
	{
		Object[] actual = ListUtil.toArray(Object.class, expectedList);
		assertSimilar(expectedList, Arrays.asList(actual));
	}

	@Test
	@Ignore
	public void testToListEnumerationOfT()
	{
		fail("Not yet implemented"); // TODO
	}

	@Test
	@Ignore
	public void testToListIterableOfT()
	{
		fail("Not yet implemented"); // TODO
	}

	@Test
	@Ignore
	public void testToListIObjectCollectorIterableOfT()
	{
		fail("Not yet implemented"); // TODO
	}

	@Test
	@Ignore
	public void testCreateCollectionOfType()
	{
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testAnyToList_Null()
	{
		List<Object> actual = ListUtil.anyToList(null);
		assertSame(emptyList, actual);
	}

	@Test
	public void testAnyToList_List()
	{
		List<Object> actual = ListUtil.anyToList(expectedList);
		assertSimilar(expectedList, actual);
		assertSame(expectedList, actual);
	}

	@Test
	public void testAnyToList_Set()
	{
		List<Object> expected = new ArrayList<Object>(expectedSet);
		List<Object> actual = ListUtil.anyToList(expectedSet);
		assertSimilar(expected, actual);
	}

	@Test
	public void testAnyToList_Collection()
	{
		Vector<Object> input = new Vector<Object>(expectedList);
		List<Object> actual = ListUtil.anyToList(input);
		assertSimilar(expectedList, actual);
	}

	@Test
	public void testAnyToList_Array1()
	{
		List<Object> actual = ListUtil.anyToList(expectedList.toArray(new Object[expectedList.size()]));
		assertSimilar(expectedList, actual);
	}

	@Test
	public void testAnyToList_Array2()
	{
		Integer[] input = { 1, 2, 3, 4, 5 };
		List<Object> expected = new ArrayList<Object>(Arrays.asList(input));
		List<Object> actual = ListUtil.anyToList(input);
		assertSimilar(expected, actual);
	}

	@Test
	public void testAnyToList_Array3()
	{
		Integer[] input = {};
		List<Object> actual = ListUtil.anyToList(input);
		assertSame(emptyList, actual);
	}

	@Test
	public void testAnyToList_Iterator1()
	{
		List<Object> actual = ListUtil.anyToList(expectedList.iterator());
		assertSimilar(expectedList, actual);
	}

	@Test
	public void testAnyToList_Iterator2()
	{
		List<Object> actual = ListUtil.anyToList(emptyList.iterator());
		assertSame(emptyList, actual);
	}

	@Test
	public void testAnyToList_Object()
	{
		String testItem = "Test Item";
		List<Object> actual = ListUtil.anyToList(testItem);
		assertNotNull(actual);
		assertEquals(1, actual.size());
		assertSame(testItem, actual.get(0));
	}

	@Test
	public void testAnyToSet_List()
	{
		Set<Object> actual = ListUtil.anyToSet(expectedList);
		assertSimilar(expectedSet, actual);
	}

	@Test
	public void testAnyToSet_Set()
	{
		Set<Object> actual = ListUtil.anyToSet(expectedSet);
		assertSimilar(expectedSet, actual);
	}

	@Test
	public void testAnyToSet_Collection()
	{
		Vector<Object> input = new Vector<Object>(expectedList);
		Set<Object> actual = ListUtil.anyToSet(input);
		assertSimilar(expectedSet, actual);
	}

	@Test
	public void testAnyToSet_Array()
	{
		Set<Object> actual = ListUtil.anyToSet(expectedList.toArray(new Object[expectedList.size()]));
		assertSimilar(expectedSet, actual);
	}

	@Test
	public void testAnyToSet_Object()
	{
		String testItem = "Test Item";
		Set<Object> actual = ListUtil.anyToSet(testItem);
		assertNotNull(actual);
		assertEquals(1, actual.size());
		assertSame(testItem, actual.iterator().next());
	}

	protected void assertSimilar(List<Object> expected, List<Object> actual)
	{
		assertNotNull(expected);
		assertNotNull(actual);
		assertEquals(expected.size(), actual.size());
		for (int i = actual.size(); i-- > 0;)
		{
			assertEquals(expected.get(i), actual.get(i));
		}
	}

	protected void assertSimilar(List<Object> expected, Set<Object> actual)
	{
		assertNotNull(expected);
		assertNotNull(actual);
		for (int i = actual.size(); i-- > 0;)
		{
			assertTrue(actual.contains(expected.get(i)));
		}
	}

	protected void assertSimilar(Set<Object> expected, Set<Object> actual)
	{
		assertNotNull(expected);
		assertNotNull(actual);
		assertEquals(expected.size(), actual.size());
		Iterator<Object> iter = expected.iterator();
		while (iter.hasNext())
		{
			assertTrue(actual.contains(iter.next()));
		}
	}
}
