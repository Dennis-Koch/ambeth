package com.koch.ambeth.ioc.extendable;

/*-
 * #%L
 * jambeth-ioc-test
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
import com.koch.ambeth.util.ParamHolder;

public class ClassExtendableContainerTest {
	protected ClassExtendableContainer<Object> fixture;

	@Before
	public void setUp() throws Exception {
		fixture = new ClassExtendableContainer<>("object", "type", false);
	}

	@After
	public void tearDown() throws Exception {
		fixture = null;
	}

	@Test(expected = IllegalArgumentException.class)
	public void extensionNull() {
		fixture.register(null, ArrayList.class);
	}

	@Test(expected = IllegalArgumentException.class)
	public void typeNull() {
		fixture.register(new Object(), null);
	}

	@Test
	public void distanceAsExpected() {
		Assert.assertEquals(0,
				ClassExtendableContainer.getDistanceForType(ArrayList.class, ArrayList.class));
		Assert.assertEquals(ClassExtendableContainer.NO_VALID_DISTANCE,
				ClassExtendableContainer.getDistanceForType(ArrayList.class, LinkedList.class));
		Assert.assertEquals(10000,
				ClassExtendableContainer.getDistanceForType(ArrayList.class, List.class));
		Assert.assertEquals(10000,
				ClassExtendableContainer.getDistanceForType(LinkedList.class, List.class));
		Assert.assertEquals(10003,
				ClassExtendableContainer.getDistanceForType(LinkedList.class, Collection.class));
	}

	@Test
	public void registerSimple() {
		Object obj1 = new Object();
		fixture.register(obj1, ArrayList.class);
		Assert.assertSame("Registration failed", obj1, fixture.getExtension(ArrayList.class));
		Assert.assertEquals("Registration failed", 1, fixture.getExtensions().size());
		Assert.assertSame("Registration failed", obj1, fixture.getExtensions().get(ArrayList.class));
	}

	@Test(expected = ExtendableException.class)
	public void duplicateStrong() {
		fixture.register(new Object(), ArrayList.class);
		fixture.register(new Object(), ArrayList.class);
	}

	@Test
	public void strongAndWeakSimple() {
		Object obj1 = new Object();
		fixture.register(obj1, Collection.class);

		Assert.assertSame("String registration failed", obj1, fixture.getExtension(ArrayList.class));
		Assert.assertSame("Weak registration to parent class failed", obj1,
				fixture.getExtension(AbstractList.class));
		Assert.assertSame("Weak registration to interface failed", obj1,
				fixture.getExtension(List.class));
		Assert.assertSame("Weak registration to parent interface failed", obj1,
				fixture.getExtension(Collection.class));
	}

	@Test
	public void strongAndWeak() {
		Object obj1 = new Object();
		Object obj2 = new Object();
		fixture.register(obj1, ArrayList.class);
		fixture.register(obj2, LinkedList.class);

		Assert.assertSame("Registration failed somehow", obj1, fixture.getExtension(ArrayList.class));
		Assert.assertSame("Registration failed somehow", obj2, fixture.getExtension(LinkedList.class));
		Assert.assertSame("Weak registration failed", obj2, fixture.getExtension(LinkedList.class));
	}

	/**
	 * Checks that unregistering an extension from a specific type does interfere neither ith other
	 * specific types of same extension nor with other extensions
	 */
	@Test
	public void strongIsolated() {
		fixture = new ClassExtendableContainer<>("object", "type", true);

		Object obj1 = new Object();
		Object obj2 = new Object();
		fixture.register(obj1, ArrayList.class);
		fixture.register(obj1, List.class);
		fixture.register(obj1, Collection.class);
		fixture.register(obj2, LinkedList.class);
		fixture.register(obj2, Collection.class);

		Assert.assertSame("Registration failed somehow", obj2, fixture.getExtension(LinkedList.class));
		Assert.assertSame("Registration failed somehow", obj1, fixture.getExtension(ArrayList.class));
		Assert.assertSame("Registration failed somehow", obj1, fixture.getExtension(List.class));
		Assert.assertSame("Registration failed somehow", obj1, fixture.getExtension(Collection.class));

		fixture.unregister(obj1, List.class);

		Assert.assertSame("Registration failed somehow", obj1, fixture.getExtension(ArrayList.class));
		Assert.assertSame("Registration failed somehow", obj1, fixture.getExtension(List.class));
		Assert.assertSame("Registration failed somehow", obj1, fixture.getExtension(Collection.class));

		fixture.unregister(obj1, ArrayList.class);

		Assert.assertSame("Registration failed somehow", obj1, fixture.getExtension(ArrayList.class));
		Assert.assertSame("Registration failed somehow", obj1, fixture.getExtension(List.class));
		Assert.assertSame("Registration failed somehow", obj1, fixture.getExtension(Collection.class));

		fixture.unregister(obj1, Collection.class);

		Assert.assertSame("Registration failed somehow", obj2, fixture.getExtension(ArrayList.class));
		Assert.assertSame("Registration failed somehow", obj2, fixture.getExtension(List.class));
		Assert.assertSame("Registration failed somehow", obj2, fixture.getExtension(Collection.class));
	}

	@Test
	public void multiThreaded() throws Throwable {
		final Object obj1 = new Object();
		final Object obj2 = new Object();

		final CyclicBarrier barrier1 = new CyclicBarrier(2);
		final CyclicBarrier barrier2 = new CyclicBarrier(2);
		final CyclicBarrier barrier3 = new CyclicBarrier(2);
		final CyclicBarrier barrier4 = new CyclicBarrier(2);
		final CyclicBarrier barrier5 = new CyclicBarrier(2);

		final CountDownLatch finishLatch = new CountDownLatch(2);

		final ParamHolder<Throwable> ex = new ParamHolder<>();

		Runnable run1 = new Runnable() {
			@Override
			public void run() {
				try {
					barrier1.await();

					fixture.register(obj1, ArrayList.class);
					fixture.register(obj2, List.class);
					fixture.register(obj1, Collection.class);

					barrier2.await();

					barrier3.await();

					fixture.unregister(obj2, List.class);
					barrier4.await();
					barrier5.await();
				}
				catch (Throwable e) {
					ex.setValue(e);
					while (finishLatch.getCount() > 0) {
						finishLatch.countDown();
					}
				}
				finally {
					finishLatch.countDown();
				}
			}
		};

		Runnable run2 = new Runnable() {
			@Override
			public void run() {
				try {
					barrier1.await();
					barrier2.await();

					Assert.assertSame("Registration failed somehow", obj2,
							fixture.getExtension(LinkedList.class));
					Assert.assertSame("Registration failed somehow", obj1,
							fixture.getExtension(ArrayList.class));
					Assert.assertSame("Registration failed somehow", obj2, fixture.getExtension(List.class));
					Assert.assertSame("Registration failed somehow", obj1,
							fixture.getExtension(Collection.class));

					barrier3.await();
					barrier4.await();
					Assert.assertSame("Registration failed somehow", obj1,
							fixture.getExtension(LinkedList.class));
					Assert.assertSame("Registration failed somehow", obj1,
							fixture.getExtension(ArrayList.class));
					Assert.assertSame("Registration failed somehow", obj1, fixture.getExtension(List.class));
					Assert.assertSame("Registration failed somehow", obj1,
							fixture.getExtension(Collection.class));
					barrier5.await();
				}
				catch (Throwable e) {
					ex.setValue(e);
					while (finishLatch.getCount() > 0) {
						finishLatch.countDown();
					}
				}
				finally {
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

		if (ex.getValue() != null) {
			throw ex.getValue();
		}
	}
}
