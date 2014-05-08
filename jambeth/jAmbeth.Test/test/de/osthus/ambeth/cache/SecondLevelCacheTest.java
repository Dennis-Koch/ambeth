package de.osthus.ambeth.cache;

import java.util.EnumSet;

import junit.framework.Assert;

import org.junit.Test;

import de.osthus.ambeth.cache.config.CacheConfigurationConstants;
import de.osthus.ambeth.collections.IdentityHashSet;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.merge.transfer.ObjRef;
import de.osthus.ambeth.persistence.xml.RelationsTest;
import de.osthus.ambeth.persistence.xml.model.Employee;
import de.osthus.ambeth.testutil.TestProperties;
import de.osthus.ambeth.testutil.TestPropertiesList;
import de.osthus.ambeth.util.MultithreadingHelper;
import de.osthus.ambeth.util.ParamChecker;

@TestPropertiesList({ @TestProperties(name = CacheConfigurationConstants.SecondLevelCacheActive, value = "false"),
		@TestProperties(name = CacheConfigurationConstants.FirstLevelCacheType, value = "PROTOTYPE") })
public class SecondLevelCacheTest extends RelationsTest
{
	protected IRootCache rootCache;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		super.afterPropertiesSet();
		ParamChecker.assertNotNull(rootCache, "rootCache");
	}

	public void setRootCache(IRootCache rootCache)
	{
		this.rootCache = rootCache;
	}

	@Test
	public void testInactiveSecondLevelCache() throws Throwable
	{
		int workerCount = 2;
		final Object[] objects = new Object[workerCount];
		final Object[] cacheValues = new Object[workerCount];

		Runnable[] runnables = new Runnable[workerCount];
		for (int a = runnables.length; a-- > 0;)
		{
			final int workerId = a;
			Runnable runnable = new Runnable()
			{
				@Override
				public void run()
				{
					Employee steveSmith = employeeService.getByName("Steve Smith");
					Employee steveSmith2 = employeeService.getByName("Steve Smith");

					// Check for non-identical objects (because of PROTOTYPE config for the first level cache)
					Assert.assertNotSame(steveSmith, steveSmith2);

					// Check for equality (because it is the same DB entity referenced by two objects)
					Assert.assertEquals(steveSmith, steveSmith2);

					objects[workerId] = steveSmith;

					IObjRef objRef = new ObjRef(Employee.class, steveSmith.getId(), steveSmith.getVersion());
					Object cacheValue = rootCache.getObject(objRef, EnumSet.<CacheDirective> of(CacheDirective.CacheValueResult));

					cacheValues[workerId] = cacheValue;
				}
			};
			runnables[a] = runnable;
		}
		MultithreadingHelper.invokeInParallel(beanContext, runnables);

		IdentityHashSet<Object> alreadyReferencedObjects = new IdentityHashSet<Object>();

		for (int workerId = cacheValues.length; workerId-- > 0;)
		{
			Object cacheValue = cacheValues[workerId];
			Object obj = objects[workerId];
			Assert.assertNotNull(cacheValue);
			Assert.assertNotNull(obj);

			// Check that each object is non-identical to all others to prove isolation fact of 2nd level cache
			Assert.assertTrue(alreadyReferencedObjects.add(cacheValue));
			Assert.assertTrue(alreadyReferencedObjects.add(obj));
		}
	}

	// @Override
	// @Test
	// public void testNullableToOne() throws Throwable
	// {
	// relationsTest.testNullableToOne();
	// }
	//
	// @Override
	// @Test
	// public void testNotNullableToOne() throws Throwable
	// {
	// relationsTest.testNotNullableToOne();
	// }
	//
	// @Override
	// @Test(expected = PersistenceException.class)
	// public void testNotNullableToOne_setToNull() throws Throwable
	// {
	// relationsTest.testNotNullableToOne_setToNull();
	// }
	//
	// @Override
	// @Test
	// public void testToMany() throws Throwable
	// {
	// relationsTest.testToMany();
	// }
	//
	// @Override
	// @Test
	// public void testNewToMany() throws Throwable
	// {
	// relationsTest.testNewToMany();
	// }
	//
	// @Override
	// @Test
	// public void testCascadDelete() throws Throwable
	// {
	// relationsTest.testCascadDelete();
	// }
	//
	// @Override
	// @Test
	// public void testCascadDeleteAfterUnlink() throws Throwable
	// {
	// relationsTest.testCascadDeleteAfterUnlink();
	// }
	//
	// @Override
	// @Test
	// public void testListDelete() throws Throwable
	// {
	// relationsTest.testListDelete();
	// }
	//
	// @Override
	// @Test
	// public void testAlternateIdDelete() throws Throwable
	// {
	// relationsTest.testAlternateIdDelete();
	// }
	//
	// @Override
	// @Test
	// public void testSetDelete() throws Throwable
	// {
	// relationsTest.testSetDelete();
	// }
	//
	// @Override
	// @Test
	// public void testArrayDelete() throws Throwable
	// {
	// relationsTest.testArrayDelete();
	// }
	//
	// @Override
	// @Test
	// public void testCascadedRetrieve() throws Throwable
	// {
	// relationsTest.testCascadedRetrieve();
	// }
	//
	// @Override
	// @Test
	// public void testMultipleChanges() throws Throwable
	// {
	// relationsTest.testMultipleChanges();
	// }
	//
	// @Override
	// @Test
	// public void testRelationUnlinkSameTable()
	// {
	// relationsTest.testRelationUnlinkSameTable();
	// }
	//
	// @Override
	// @Test
	// public void testRelationUnlinkOtherTable()
	// {
	// relationsTest.testRelationUnlinkOtherTable();
	// }
	//
	// @Override
	// @Test
	// public void testBidirectionalToOneRelation()
	// {
	// relationsTest.testBidirectionalToOneRelation();
	// }
	//
	// @Override
	// @Test
	// public void testBidirectionalToManyRelation()
	// {
	// relationsTest.testBidirectionalToManyRelation();
	// }
	//
	// @Override
	// @Test
	// public void testVersionUpdateOnFKRelation()
	// {
	// relationsTest.testVersionUpdateOnFKRelation();
	// }
	//
	// @Override
	// @Test
	// public void testVersionUpdateOnLTRelation()
	// {
	// relationsTest.testVersionUpdateOnLTRelation();
	// }
	//
	// @Override
	// @Test
	// public void testOptimisticLockWithDelete()
	// {
	// relationsTest.testOptimisticLockWithDelete();
	// }
	//
	// @Override
	// @Test(expected = OptimisticLockException.class)
	// public void testOptimisticLockWithDelete_lock()
	// {
	// relationsTest.testOptimisticLockWithDelete_lock();
	// }
	//
	// @Override
	// @Test
	// public void testOptimisticLockWithDelete_lock_checkIdAndVersion()
	// {
	// relationsTest.testOptimisticLockWithDelete_lock_checkIdAndVersion();
	// }
}
