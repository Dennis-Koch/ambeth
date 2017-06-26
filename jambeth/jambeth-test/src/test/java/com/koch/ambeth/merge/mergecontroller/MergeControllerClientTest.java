package com.koch.ambeth.merge.mergecontroller;

/*-
 * #%L
 * jambeth-test
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.CyclicBarrier;

import org.junit.Assert;
import org.junit.Test;

import com.koch.ambeth.event.IEventDispatcher;
import com.koch.ambeth.informationbus.persistence.setup.SQLData;
import com.koch.ambeth.informationbus.persistence.setup.SQLStructure;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.bytecode.IBytecodeEnhancer;
import com.koch.ambeth.ioc.util.IMultithreadingHelper;
import com.koch.ambeth.merge.IMergeController;
import com.koch.ambeth.merge.IMergeProcess;
import com.koch.ambeth.merge.bytecode.EntityEnhancementHint;
import com.koch.ambeth.merge.cache.ICache;
import com.koch.ambeth.service.cache.ClearAllCachesEvent;
import com.koch.ambeth.service.config.ServiceConfigurationConstants;
import com.koch.ambeth.service.merge.IEntityMetaDataProvider;
import com.koch.ambeth.testutil.AbstractInformationBusWithPersistenceTest;
import com.koch.ambeth.testutil.TestModule;
import com.koch.ambeth.testutil.TestProperties;
import com.koch.ambeth.testutil.TestPropertiesList;
import com.koch.ambeth.util.ReflectUtil;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
import com.koch.ambeth.util.model.IDataObject;

@SQLStructure("MergeControllerTest_structure.sql")
@SQLData("MergeControllerTest_data.sql")
@TestModule(MergeControllerTestModule.class)
@TestPropertiesList({@TestProperties(name = ServiceConfigurationConstants.mappingFile,
		value = "com/koch/ambeth/merge/mergecontroller/MergeControllerTest-orm.xml")})
public class MergeControllerClientTest extends AbstractInformationBusWithPersistenceTest {
	@Autowired
	protected ICache cache;

	@Autowired
	protected IBytecodeEnhancer bytecodeEnhancer;

	@Autowired
	protected IEventDispatcher eventDispatcher;

	@Autowired
	protected IParentService parentService;

	@Autowired
	protected IMergeController mergeController;

	@Autowired
	protected IMultithreadingHelper multithreadingHelper;

	@Override
	public void afterPropertiesSet() throws Throwable {
		super.afterPropertiesSet();

		// To simulate client behavior the entityMetaDataProvider has to return an entityPersistOrder
		IEntityMetaDataProvider orderedEntityMetaDataProvider =
				new OrderedEntityMetaDataServer(entityMetaDataProvider);
		ReflectUtil.getDeclaredField(mergeController.getClass(), "entityMetaDataProvider")
				.set(mergeController, orderedEntityMetaDataProvider);
	}

	@Test
	public void testChangeChild() {
		Parent parent = cache.getObject(Parent.class, 1);
		Child child = parent.getChild();

		child.setName(child.getName() + " 2");

		parentService.save(parent);
	}

	@Test
	public void testChangeOtherChildren() {
		Parent parent = cache.getObject(Parent.class, 1);
		Child child = parent.getOtherChildren().get(0);

		child.setName(child.getName() + " 2");

		parentService.save(parent);
	}

	@Test
	public void testChangeChildMultithreaded() {
		final int parentId = 1;
		final int childId = 11;

		final CyclicBarrier childLoadedCondition = new CyclicBarrier(2);
		final CyclicBarrier childUpdatedCondition = new CyclicBarrier(2);

		Runnable parentModifierRunnable = new Runnable() {
			@Override
			public void run() {
				Parent parent = cache.getObject(Parent.class, parentId);
				parent.getChild().getName(); // Just to initialize it

				try {
					childLoadedCondition.await();
					childUpdatedCondition.await();
				}
				catch (Exception e) {
					throw RuntimeExceptionUtil.mask(e);
				}

				parent.setName(parent.getName() + "p");
				beanContext.getService(IMergeProcess.class).process(parent, null, null, null);
			}
		};
		Runnable childModifierRunnable = new Runnable() {
			@Override
			public void run() {
				try {
					childLoadedCondition.await();

					Child child = cache.getObject(Child.class, childId);
					child.setName(child.getName() + "c");
					beanContext.getService(IMergeProcess.class).process(child, null, null, null);
					childUpdatedCondition.await();
				}
				catch (Exception e) {
					throw RuntimeExceptionUtil.mask(e);
				}
			}
		};
		multithreadingHelper.invokeInParallel(beanContext, parentModifierRunnable,
				childModifierRunnable);
	}

	@Test
	public void testChangeOtherChildrenMultithreaded() {
		final int parentId = 1;
		final int childId = 13;

		final CyclicBarrier childLoadedCondition = new CyclicBarrier(2);
		final CyclicBarrier childUpdatedCondition = new CyclicBarrier(2);

		Runnable parentModifierRunnable = new Runnable() {
			@Override
			public void run() {
				Parent parent = cache.getObject(Parent.class, parentId);
				parent.getOtherChildren().get(0).getName(); // Just to initialize it

				try {
					childLoadedCondition.await();
					childUpdatedCondition.await();
				}
				catch (Exception e) {
					throw RuntimeExceptionUtil.mask(e);
				}

				parent.setName(parent.getName() + "p");
				beanContext.getService(IMergeProcess.class).process(parent, null, null, null);
			}
		};
		Runnable childModifierRunnable = new Runnable() {
			@Override
			public void run() {
				try {
					childLoadedCondition.await();

					Child child = cache.getObject(Child.class, childId);
					child.setName(child.getName() + "c");
					beanContext.getService(IMergeProcess.class).process(child, null, null, null);
					childUpdatedCondition.await();
				}
				catch (Exception e) {
					throw RuntimeExceptionUtil.mask(e);
				}
			}
		};
		multithreadingHelper.invokeInParallel(beanContext, parentModifierRunnable,
				childModifierRunnable);
	}

	@Test
	public void deleteUnpersistedEntity() {
		Parent parent = entityFactory.createEntity(Parent.class);
		((IDataObject) parent).setToBeDeleted(true);
		parentService.save(parent);
	}

	@Test
	public void testSetAnotherChild() {
		Parent parent = cache.getObject(Parent.class, 1);
		Child newChild = cache.getObject(Child.class, 12);
		Child child = parent.getChild();

		assertNotNull(child);
		assertTrue(newChild.getId() != child.getId());
		Assert.assertFalse(((IDataObject) parent).isToBeUpdated());

		parent.setChild(newChild);

		Assert.assertTrue(((IDataObject) parent).isToBeUpdated());

		parentService.save(parent);
		eventDispatcher.dispatchEvent(ClearAllCachesEvent.getInstance());

		parent = cache.getObject(Parent.class, 1);
		child = parent.getChild();
		assertNotNull(child);
		assertEquals(newChild.getId(), child.getId());
	}

	@Test
	public void testSetAnotherOtherChildren() {
		Parent parent = cache.getObject(Parent.class, 1);
		Child newChild = cache.getObject(Child.class, 12);
		Child child = parent.getOtherChildren().get(0);

		assertNotNull(child);
		assertTrue(newChild.getId() != child.getId());
		Assert.assertFalse(((IDataObject) parent).isToBeUpdated());

		parent.getOtherChildren().set(0, newChild);

		Assert.assertTrue(((IDataObject) parent).isToBeUpdated());

		parentService.save(parent);
		eventDispatcher.dispatchEvent(ClearAllCachesEvent.getInstance());

		parent = cache.getObject(Parent.class, 1);
		child = parent.getOtherChildren().get(0);
		assertNotNull(child);
		assertEquals(newChild.getId(), child.getId());
	}

	@Test
	public void testSetAnotherOtherChildren2() {
		Parent parent = cache.getObject(Parent.class, 1);
		Child newChild = cache.getObject(Child.class, 12);
		Child child = parent.getOtherChildren2().iterator().next();

		assertNotNull(child);
		assertTrue(newChild.getId() != child.getId());
		Assert.assertFalse(((IDataObject) parent).isToBeUpdated());

		parent.getOtherChildren2().clear();
		parent.getOtherChildren2().add(newChild);

		Assert.assertTrue(((IDataObject) parent).isToBeUpdated());

		parentService.save(parent);
		eventDispatcher.dispatchEvent(ClearAllCachesEvent.getInstance());

		parent = cache.getObject(Parent.class, 1);
		child = parent.getOtherChildren2().iterator().next();
		assertNotNull(child);
		assertEquals(newChild.getId(), child.getId());
	}

	@Test
	public void testAddChild() {
		Parent parent = cache.getObject(Parent.class, 2);
		Child newChild = cache.getObject(Child.class, 12);
		assertNull(parent.getChild());

		parent.setChild(newChild);

		parentService.save(parent);
		eventDispatcher.dispatchEvent(ClearAllCachesEvent.getInstance());

		parent = cache.getObject(Parent.class, 2);
		Child child = parent.getChild();
		assertNotNull(child);
		assertEquals(newChild.getId(), child.getId());
	}

	@Test
	public void testRemoveChild() {
		Parent parent = cache.getObject(Parent.class, 1);

		parent.setChild(null);

		parentService.save(parent);
		eventDispatcher.dispatchEvent(ClearAllCachesEvent.getInstance());

		parent = cache.getObject(Parent.class, 1);
		assertNull(parent.getChild());
	}

	@Test
	public void testGenericElementOfList() throws Throwable {
		Class<?> parentType =
				bytecodeEnhancer.getEnhancedType(Parent.class, EntityEnhancementHint.Instance);
		{
			Method originalMethod = Parent.class.getMethod("getOtherChildren");
			Method method = parentType.getMethod("getOtherChildren");

			Type originalGenericReturnType = originalMethod.getGenericReturnType();
			Type genericReturnType = method.getGenericReturnType();

			Assert.assertTrue(originalGenericReturnType instanceof ParameterizedType);
			Assert.assertTrue(genericReturnType instanceof ParameterizedType);
		}

		{
			Method originalMethod = Parent.class.getMethod("setOtherChildren", List.class);
			Method method = parentType.getMethod("setOtherChildren", List.class);

			Type originalGenericReturnType = originalMethod.getGenericParameterTypes()[0];
			Type genericReturnType = method.getGenericParameterTypes()[0];

			Assert.assertTrue(originalGenericReturnType instanceof ParameterizedType);
			Assert.assertTrue(genericReturnType instanceof ParameterizedType);
		}
	}
}
