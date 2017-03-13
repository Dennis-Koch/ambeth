package com.koch.ambeth.util.collections;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.koch.ambeth.util.collections.IListElem;
import com.koch.ambeth.util.collections.InterfaceFastList;

public class InterfaceFastListTest
{
	@BeforeClass
	public static void setUpBeforeClass() throws Exception
	{
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception
	{
	}

	public class TestListElem implements IListElem<TestListElem>
	{
		private Object listHandle;
		private IListElem<TestListElem> prev;
		private IListElem<TestListElem> next;

		@Override
		public Object getListHandle()
		{
			return listHandle;
		}

		@Override
		public void setListHandle(Object listHandle)
		{
			this.listHandle = listHandle;
		}

		@Override
		public IListElem<TestListElem> getPrev()
		{
			return prev;
		}

		@Override
		public void setPrev(IListElem<TestListElem> prev)
		{
			this.prev = prev;
		}

		@Override
		public IListElem<TestListElem> getNext()
		{
			return next;
		}

		@Override
		public void setNext(IListElem<TestListElem> next)
		{
			this.next = next;
		}

		@Override
		public TestListElem getElemValue()
		{
			return this;
		}

		@Override
		public void setElemValue(TestListElem value)
		{
			throw new UnsupportedOperationException();
		}
	}

	protected InterfaceFastList<TestListElem> fixture;

	@Before
	public void setUp() throws Exception
	{
		fixture = new InterfaceFastList<TestListElem>();
	}

	@After
	public void tearDown() throws Exception
	{
		fixture = null;
	}

	protected TestListElem[] createTestArray(int size)
	{
		TestListElem[] array = new TestListElem[size];
		for (int a = array.length; a-- > 0;)
		{
			array[a] = new TestListElem();
		}
		return array;
	}

	protected void checkSequence(TestListElem[] array, InterfaceFastList<TestListElem> list)
	{
		Assert.assertEquals("size not valid", array.length, list.size());
		int index = 0;
		IListElem<TestListElem> elem = list.first();
		while (elem != null)
		{
			Assert.assertSame("Item at index " + index + " not valid", array[index], elem);
			index++;
			elem = elem.getNext();
		}
		index = array.length - 1;
		elem = list.last();
		while (elem != null)
		{
			Assert.assertSame("Item at index " + index + " not valid", array[index], elem);
			index--;
			elem = elem.getPrev();
		}
	}

	@Test
	public final void testPushLast()
	{
		TestListElem[] array = createTestArray(10);
		for (int a = 0, size = array.length; a < size; a++)
		{
			fixture.pushLast(array[a]);
		}
		checkSequence(array, fixture);
	}

	@Test
	public final void testPushFirst()
	{
		TestListElem[] array = createTestArray(10);
		for (int a = array.length; a-- > 0;)
		{
			fixture.pushFirst(array[a]);
		}
		checkSequence(array, fixture);
	}

	@Test
	public final void testInsertAfter()
	{
		TestListElem[] array = createTestArray(10);
		for (int a = array.length; a-- > 0;)
		{
			fixture.pushFirst(array[a]);
		}
		fixture.remove(array[5]);
		fixture.insertAfter(array[5], array[4]);
		checkSequence(array, fixture);
	}

	@Test
	public final void testInsertBefore()
	{
		TestListElem[] array = createTestArray(10);
		for (int a = array.length; a-- > 0;)
		{
			fixture.pushFirst(array[a]);
		}
		fixture.remove(array[5]);
		fixture.insertBefore(array[5], array[6]);
		checkSequence(array, fixture);
	}

	@Test
	public final void testPopFirst()
	{
		TestListElem[] array = createTestArray(10);
		for (int a = array.length; a-- > 0;)
		{
			fixture.pushFirst(array[a]);
		}
		fixture.popFirst();
		fixture.pushFirst(array[0]);
		checkSequence(array, fixture);
	}

	@Test
	public final void testPopLast()
	{
		TestListElem[] array = createTestArray(10);
		for (int a = array.length; a-- > 0;)
		{
			fixture.pushFirst(array[a]);
		}
		fixture.popLast();
		fixture.pushLast(array[array.length - 1]);
		checkSequence(array, fixture);
	}

	@Test
	public final void testGetFirstElem()
	{
		Assert.assertNull(fixture.first());
		TestListElem[] array = createTestArray(10);
		for (int a = array.length; a-- > 0;)
		{
			fixture.pushFirst(array[a]);
		}
		Assert.assertSame(array[0], fixture.first());
	}

	@Test
	public final void testGetLastElem()
	{
		Assert.assertNull(fixture.last());
		TestListElem[] array = createTestArray(10);
		for (int a = array.length; a-- > 0;)
		{
			fixture.pushFirst(array[a]);
		}
		Assert.assertSame(array[array.length - 1], fixture.last());
	}

	@Test
	public final void testSize()
	{
		Assert.assertEquals(0, fixture.size());
		TestListElem[] array = createTestArray(10);
		for (int a = array.length; a-- > 0;)
		{
			fixture.pushFirst(array[a]);
		}
		Assert.assertEquals(array.length, fixture.size());
	}

	@Test
	public final void testClear()
	{
		TestListElem[] array = createTestArray(10);
		for (int a = array.length; a-- > 0;)
		{
			fixture.pushFirst(array[a]);
		}
		fixture.clear();
		checkSequence(new TestListElem[0], fixture);
		Assert.assertEquals(0, fixture.size());
	}

	@Test
	public final void testRemove()
	{
		TestListElem[] array = createTestArray(10);
		for (int a = array.length; a-- > 0;)
		{
			fixture.pushFirst(array[a]);
		}
		fixture.popFirst();
		fixture.pushFirst(array[0]);
		fixture.popLast();
		fixture.pushLast(array[array.length - 1]);
		fixture.remove(array[3]);
		fixture.remove(array[4]);
		fixture.remove(array[6]);
		fixture.insertAfter(array[3], array[2]);
		fixture.insertAfter(array[4], array[3]);
		fixture.insertAfter(array[6], array[5]);
		checkSequence(array, fixture);
	}

	@Test
	public final void testHasListElem()
	{
		TestListElem[] array = createTestArray(10);
		for (int a = array.length; a-- > 0;)
		{
			fixture.pushFirst(array[a]);
		}
		Assert.assertTrue(fixture.hasListElem(array[0]));
		Assert.assertTrue(fixture.hasListElem(array[array.length - 1]));
		Assert.assertTrue(fixture.hasListElem(array[7]));
		Assert.assertFalse(fixture.hasListElem(new TestListElem()));
	}
}
