package com.koch.ambeth.cache;

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

import java.util.EnumSet;

import org.junit.Assert;
import org.junit.Test;

import com.koch.ambeth.cache.IRootCache;
import com.koch.ambeth.cache.config.CacheConfigurationConstants;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.util.IMultithreadingHelper;
import com.koch.ambeth.merge.cache.CacheDirective;
import com.koch.ambeth.merge.transfer.ObjRef;
import com.koch.ambeth.persistence.xml.RelationsTest;
import com.koch.ambeth.persistence.xml.model.Employee;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.testutil.TestProperties;
import com.koch.ambeth.testutil.TestPropertiesList;
import com.koch.ambeth.util.collections.IdentityHashSet;

@TestPropertiesList({ @TestProperties(name = CacheConfigurationConstants.SecondLevelCacheActive, value = "false"),
		@TestProperties(name = CacheConfigurationConstants.FirstLevelCacheType, value = "PROTOTYPE") })
public class SecondLevelCacheTest extends RelationsTest
{
	@Autowired
	protected IMultithreadingHelper multithreadingHelper;

	@Autowired
	protected IRootCache rootCache;

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
		multithreadingHelper.invokeInParallel(beanContext, runnables);

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
