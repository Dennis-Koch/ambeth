package de.osthus.ambeth.merge.mergecontroller;

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

import de.osthus.ambeth.bytecode.EntityEnhancementHint;
import de.osthus.ambeth.bytecode.IBytecodeEnhancer;
import de.osthus.ambeth.cache.ClearAllCachesEvent;
import de.osthus.ambeth.cache.ICache;
import de.osthus.ambeth.config.ServiceConfigurationConstants;
import de.osthus.ambeth.event.IEventDispatcher;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.merge.IEntityMetaDataProvider;
import de.osthus.ambeth.merge.IMergeController;
import de.osthus.ambeth.merge.IMergeProcess;
import de.osthus.ambeth.model.IDataObject;
import de.osthus.ambeth.testutil.AbstractPersistenceTest;
import de.osthus.ambeth.testutil.SQLData;
import de.osthus.ambeth.testutil.SQLStructure;
import de.osthus.ambeth.testutil.TestModule;
import de.osthus.ambeth.testutil.TestProperties;
import de.osthus.ambeth.testutil.TestPropertiesList;
import de.osthus.ambeth.util.MultithreadingHelper;
import de.osthus.ambeth.util.ReflectUtil;

@SQLStructure("MergeControllerTest_structure.sql")
@SQLData("MergeControllerTest_data.sql")
@TestModule(MergeControllerTestModule.class)
@TestPropertiesList({ @TestProperties(name = ServiceConfigurationConstants.mappingFile, value = "de/osthus/ambeth/merge/mergecontroller/MergeControllerTest-orm.xml") })
public class MergeControllerClientTest extends AbstractPersistenceTest
{
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

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		super.afterPropertiesSet();

		// To simulate client behavior the entityMetaDataProvider has to return an entityPersistOrder
		IEntityMetaDataProvider orderedEntityMetaDataProvider = new OrderedEntityMetaDataServer(entityMetaDataProvider);
		ReflectUtil.getDeclaredField(mergeController.getClass(), "entityMetaDataProvider").set(mergeController, orderedEntityMetaDataProvider);
	}

	@Test
	public void testChangeChild()
	{
		Parent parent = cache.getObject(Parent.class, 1);
		Child child = parent.getChild();

		child.setName(child.getName() + " 2");

		parentService.save(parent);
	}

	@Test
	public void testChangeOtherChildren()
	{
		Parent parent = cache.getObject(Parent.class, 1);
		Child child = parent.getOtherChildren().get(0);

		child.setName(child.getName() + " 2");

		parentService.save(parent);
	}

	@Test
	public void testChangeChildMultithreaded()
	{
		final int parentId = 1;
		final int childId = 11;

		final CyclicBarrier childLoadedCondition = new CyclicBarrier(2);
		final CyclicBarrier childUpdatedCondition = new CyclicBarrier(2);

		Runnable parentModifierRunnable = new Runnable()
		{
			@Override
			public void run()
			{
				Parent parent = cache.getObject(Parent.class, parentId);
				parent.getChild().getName(); // Just to initialize it

				try
				{
					childLoadedCondition.await();
					childUpdatedCondition.await();
				}
				catch (Throwable e)
				{
					throw RuntimeExceptionUtil.mask(e);
				}

				parent.setName(parent.getName() + "p");
				beanContext.getService(IMergeProcess.class).process(parent, null, null, null);
			}
		};
		Runnable childModifierRunnable = new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					childLoadedCondition.await();

					Child child = cache.getObject(Child.class, childId);
					child.setName(child.getName() + "c");
					beanContext.getService(IMergeProcess.class).process(child, null, null, null);
					childUpdatedCondition.await();
				}
				catch (Throwable e)
				{
					throw RuntimeExceptionUtil.mask(e);
				}
			}
		};
		MultithreadingHelper.invokeInParallel(beanContext, parentModifierRunnable, childModifierRunnable);
	}

	@Test
	public void testChangeOtherChildrenMultithreaded()
	{
		final int parentId = 1;
		final int childId = 13;

		final CyclicBarrier childLoadedCondition = new CyclicBarrier(2);
		final CyclicBarrier childUpdatedCondition = new CyclicBarrier(2);

		Runnable parentModifierRunnable = new Runnable()
		{
			@Override
			public void run()
			{
				Parent parent = cache.getObject(Parent.class, parentId);
				parent.getOtherChildren().get(0).getName(); // Just to initialize it

				try
				{
					childLoadedCondition.await();
					childUpdatedCondition.await();
				}
				catch (Throwable e)
				{
					throw RuntimeExceptionUtil.mask(e);
				}

				parent.setName(parent.getName() + "p");
				beanContext.getService(IMergeProcess.class).process(parent, null, null, null);
			}
		};
		Runnable childModifierRunnable = new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					childLoadedCondition.await();

					Child child = cache.getObject(Child.class, childId);
					child.setName(child.getName() + "c");
					beanContext.getService(IMergeProcess.class).process(child, null, null, null);
					childUpdatedCondition.await();
				}
				catch (Throwable e)
				{
					throw RuntimeExceptionUtil.mask(e);
				}
			}
		};
		MultithreadingHelper.invokeInParallel(beanContext, parentModifierRunnable, childModifierRunnable);
	}

	@Test
	public void testSetAnotherChild()
	{
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
	public void testSetAnotherOtherChildren()
	{
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
	public void testSetAnotherOtherChildren2()
	{
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
	public void testAddChild()
	{
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
	public void testRemoveChild()
	{
		Parent parent = cache.getObject(Parent.class, 1);

		parent.setChild(null);

		parentService.save(parent);
		eventDispatcher.dispatchEvent(ClearAllCachesEvent.getInstance());

		parent = cache.getObject(Parent.class, 1);
		assertNull(parent.getChild());
	}

	@Test
	public void testGenericElementOfList() throws Throwable
	{
		Class<?> parentType = bytecodeEnhancer.getEnhancedType(Parent.class, EntityEnhancementHint.Instance);
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
