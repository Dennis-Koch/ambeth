package com.koch.ambeth.util;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.koch.ambeth.ioc.exception.ExtendableException;
import com.koch.ambeth.ioc.util.ClassTupleExtendableContainer;
import com.koch.ambeth.ioc.util.ConversionKey;
import com.koch.ambeth.testutil.AbstractIocTest;
import com.koch.ambeth.util.ParamHolder;

public class ClassTupleExtendableContainerTest extends AbstractIocTest
{
	protected ClassTupleExtendableContainer<Object> fixture;

	@Before
	public void setUp() throws Exception
	{
		fixture = new ClassTupleExtendableContainer<Object>("object", "type", false);
	}

	@After
	public void tearDown() throws Exception
	{
		fixture = null;
	}

	@Test(expected = IllegalArgumentException.class)
	public void extensionNull()
	{
		fixture.register(null, ArrayList.class, Collection.class);
	}

	@Test(expected = IllegalArgumentException.class)
	public void typeNull()
	{
		fixture.register(new Object(), null, null);
	}

	@Test
	public void registerSimple()
	{
		Object obj1 = new Object();
		fixture.register(obj1, ArrayList.class, byte[].class);
		Assert.assertSame("Registration failed", obj1, fixture.getExtension(ArrayList.class, byte[].class));
		Assert.assertSame("Registration failed", 1, fixture.getExtensions().size());
		Assert.assertSame("Registration failed", obj1, fixture.getExtensions().get(new ConversionKey(ArrayList.class, byte[].class)));
	}

	@Test(expected = ExtendableException.class)
	public void duplicateStrong()
	{
		fixture.register(new Object(), ArrayList.class, byte[].class);
		fixture.register(new Object(), ArrayList.class, byte[].class);
	}

	@Test
	public void strongAndWeakSimple()
	{
		Object obj1 = new Object();
		fixture.register(obj1, Collection.class, String.class);

		Assert.assertSame("String registration failed", obj1, fixture.getExtension(ArrayList.class, String.class));
		Assert.assertSame("Weak registration to parent class failed", obj1, fixture.getExtension(AbstractList.class, String.class));
		Assert.assertSame("Weak registration to interface failed", obj1, fixture.getExtension(List.class, String.class));
		Assert.assertSame("Weak registration to parent interface failed", obj1, fixture.getExtension(Collection.class, String.class));

		Assert.assertSame("String registration failed", obj1, fixture.getExtension(ArrayList.class, CharSequence.class));
		Assert.assertSame("Weak registration to parent class failed", obj1, fixture.getExtension(AbstractList.class, CharSequence.class));
		Assert.assertSame("Weak registration to interface failed", obj1, fixture.getExtension(List.class, CharSequence.class));
		Assert.assertSame("Weak registration to parent interface failed", obj1, fixture.getExtension(Collection.class, CharSequence.class));
	}

	@Test
	public void strongAndWeak()
	{
		Object obj1 = new Object();
		Object obj2 = new Object();
		fixture.register(obj1, ArrayList.class, String.class);
		fixture.register(obj2, LinkedList.class, String.class);

		Assert.assertSame("Registration failed somehow", obj1, fixture.getExtension(ArrayList.class, String.class));
		Assert.assertSame("Registration failed somehow", obj2, fixture.getExtension(LinkedList.class, String.class));
	}

	/**
	 * Checks that unregistering an extension from a specific type does interfere neither with other specific types of same extension nor with other extensions
	 */
	@Test
	public void strongIsolated()
	{
		fixture = new ClassTupleExtendableContainer<Object>("object", "type", true);

		Object obj1 = "obj1";
		Object obj2 = "obj2";
		fixture.register(obj1, ArrayList.class, String.class);
		fixture.register(obj1, List.class, String.class);
		fixture.register(obj1, Collection.class, String.class);
		fixture.register(obj2, LinkedList.class, String.class);
		fixture.register(obj2, Collection.class, String.class);

		Assert.assertSame("Registration failed somehow", obj2, fixture.getExtension(LinkedList.class, String.class));
		Assert.assertSame("Registration failed somehow", obj1, fixture.getExtension(ArrayList.class, String.class));
		Assert.assertSame("Registration failed somehow", obj1, fixture.getExtension(List.class, String.class));
		Assert.assertSame("Registration failed somehow", obj1, fixture.getExtension(Collection.class, String.class));

		Assert.assertSame("Registration failed somehow", obj2, fixture.getExtension(LinkedList.class, CharSequence.class));
		Assert.assertSame("Registration failed somehow", obj1, fixture.getExtension(ArrayList.class, CharSequence.class));
		Assert.assertSame("Registration failed somehow", obj1, fixture.getExtension(List.class, CharSequence.class));
		Assert.assertSame("Registration failed somehow", obj1, fixture.getExtension(Collection.class, CharSequence.class));

		fixture.unregister(obj1, List.class, String.class);

		Assert.assertSame("Registration failed somehow", obj1, fixture.getExtension(ArrayList.class, String.class));
		Assert.assertSame("Registration failed somehow", obj1, fixture.getExtension(List.class, String.class));
		Assert.assertSame("Registration failed somehow", obj1, fixture.getExtension(Collection.class, String.class));

		Assert.assertSame("Registration failed somehow", obj1, fixture.getExtension(ArrayList.class, CharSequence.class));
		Assert.assertSame("Registration failed somehow", obj1, fixture.getExtension(List.class, CharSequence.class));
		Assert.assertSame("Registration failed somehow", obj1, fixture.getExtension(Collection.class, CharSequence.class));

		fixture.unregister(obj1, ArrayList.class, String.class);

		Assert.assertSame("Registration failed somehow", obj1, fixture.getExtension(ArrayList.class, String.class));
		Assert.assertSame("Registration failed somehow", obj1, fixture.getExtension(List.class, String.class));
		Assert.assertSame("Registration failed somehow", obj1, fixture.getExtension(Collection.class, String.class));

		Assert.assertSame("Registration failed somehow", obj1, fixture.getExtension(ArrayList.class, CharSequence.class));
		Assert.assertSame("Registration failed somehow", obj1, fixture.getExtension(List.class, CharSequence.class));
		Assert.assertSame("Registration failed somehow", obj1, fixture.getExtension(Collection.class, CharSequence.class));

		fixture.unregister(obj1, Collection.class, String.class);

		Assert.assertSame("Registration failed somehow", obj2, fixture.getExtension(ArrayList.class, String.class));
		Assert.assertSame("Registration failed somehow", obj2, fixture.getExtension(List.class, String.class));
		Assert.assertSame("Registration failed somehow", obj2, fixture.getExtension(Collection.class, String.class));

		Assert.assertSame("Registration failed somehow", obj2, fixture.getExtension(ArrayList.class, CharSequence.class));
		Assert.assertSame("Registration failed somehow", obj2, fixture.getExtension(List.class, CharSequence.class));
		Assert.assertSame("Registration failed somehow", obj2, fixture.getExtension(Collection.class, CharSequence.class));
	}

	@Test
	public void multiThreaded() throws Throwable
	{
		final Object obj1 = new Object();
		final Object obj2 = new Object();

		final CyclicBarrier barrier1 = new CyclicBarrier(2);
		final CyclicBarrier barrier2 = new CyclicBarrier(2);
		final CyclicBarrier barrier3 = new CyclicBarrier(2);
		final CyclicBarrier barrier4 = new CyclicBarrier(2);
		final CyclicBarrier barrier5 = new CyclicBarrier(2);

		final CountDownLatch finishLatch = new CountDownLatch(2);

		final ParamHolder<Throwable> ex = new ParamHolder<Throwable>();

		Runnable run1 = new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					barrier1.await();

					fixture.register(obj1, ArrayList.class, String.class);
					fixture.register(obj2, List.class, String.class);
					fixture.register(obj1, Collection.class, String.class);

					barrier2.await();

					barrier3.await();

					fixture.unregister(obj2, List.class, String.class);
					barrier4.await();
					barrier5.await();
				}
				catch (Throwable e)
				{
					ex.setValue(e);
					while (finishLatch.getCount() > 0)
					{
						finishLatch.countDown();
					}
				}
				finally
				{
					finishLatch.countDown();
				}
			}
		};

		Runnable run2 = new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					barrier1.await();
					barrier2.await();

					Assert.assertSame("Registration failed somehow", obj2, fixture.getExtension(LinkedList.class, String.class));
					Assert.assertSame("Registration failed somehow", obj1, fixture.getExtension(ArrayList.class, String.class));
					Assert.assertSame("Registration failed somehow", obj2, fixture.getExtension(List.class, String.class));
					Assert.assertSame("Registration failed somehow", obj1, fixture.getExtension(Collection.class, String.class));

					Assert.assertSame("Registration failed somehow", obj2, fixture.getExtension(LinkedList.class, CharSequence.class));
					Assert.assertSame("Registration failed somehow", obj1, fixture.getExtension(ArrayList.class, CharSequence.class));
					Assert.assertSame("Registration failed somehow", obj2, fixture.getExtension(List.class, CharSequence.class));
					Assert.assertSame("Registration failed somehow", obj1, fixture.getExtension(Collection.class, CharSequence.class));

					barrier3.await();
					barrier4.await();
					Assert.assertSame("Registration failed somehow", obj1, fixture.getExtension(LinkedList.class, String.class));
					Assert.assertSame("Registration failed somehow", obj1, fixture.getExtension(ArrayList.class, String.class));
					Assert.assertSame("Registration failed somehow", obj1, fixture.getExtension(List.class, String.class));
					Assert.assertSame("Registration failed somehow", obj1, fixture.getExtension(Collection.class, String.class));

					Assert.assertSame("Registration failed somehow", obj1, fixture.getExtension(LinkedList.class, CharSequence.class));
					Assert.assertSame("Registration failed somehow", obj1, fixture.getExtension(ArrayList.class, CharSequence.class));
					Assert.assertSame("Registration failed somehow", obj1, fixture.getExtension(List.class, CharSequence.class));
					Assert.assertSame("Registration failed somehow", obj1, fixture.getExtension(Collection.class, CharSequence.class));
					barrier5.await();
				}
				catch (Throwable e)
				{
					ex.setValue(e);
					while (finishLatch.getCount() > 0)
					{
						finishLatch.countDown();
					}
				}
				finally
				{
					finishLatch.countDown();
				}
			}
		};

		Thread thread1 = new Thread(run1);
		thread1.setContextClassLoader(Thread.currentThread().getContextClassLoader());
		thread1.setDaemon(true);
		thread1.start();

		Thread thread2 = new Thread(run2);
		thread2.setContextClassLoader(Thread.currentThread().getContextClassLoader());
		thread2.setDaemon(true);
		thread2.start();

		finishLatch.await();

		if (ex.getValue() != null)
		{
			throw ex.getValue();
		}
	}
}
