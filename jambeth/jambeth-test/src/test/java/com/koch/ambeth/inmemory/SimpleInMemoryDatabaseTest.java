package com.koch.ambeth.inmemory;

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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CyclicBarrier;

import javax.persistence.OptimisticLockException;

import org.junit.Assert;
import org.junit.Test;

import com.koch.ambeth.cache.config.CacheNamedBeans;
import com.koch.ambeth.cache.service.ICacheRetrieverExtendable;
import com.koch.ambeth.event.IEventListenerExtendable;
import com.koch.ambeth.inmemory.SimpleInMemoryDatabaseTest.SimpleInMemoryDatabaseTestModule;
import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.config.IBeanConfiguration;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.ioc.threadlocal.IThreadLocalCleanupController;
import com.koch.ambeth.ioc.util.IMultithreadingHelper;
import com.koch.ambeth.merge.IMergeServiceExtensionExtendable;
import com.koch.ambeth.merge.cache.ICache;
import com.koch.ambeth.merge.cache.ICacheProvider;
import com.koch.ambeth.merge.server.inmemory.SimpleInMemoryDatabase;
import com.koch.ambeth.persistence.api.IDatabase;
import com.koch.ambeth.persistence.api.database.DatabaseCallback;
import com.koch.ambeth.persistence.config.PersistenceConfigurationConstants;
import com.koch.ambeth.persistence.event.DatabaseAcquireEvent;
import com.koch.ambeth.persistence.event.DatabaseFailEvent;
import com.koch.ambeth.persistence.event.DatabasePreCommitEvent;
import com.koch.ambeth.persistence.jdbc.alternateid.AlternateIdEntity;
import com.koch.ambeth.persistence.jdbc.alternateid.BaseEntity;
import com.koch.ambeth.persistence.jdbc.alternateid.BaseEntity2;
import com.koch.ambeth.persistence.jdbc.alternateid.IAlternateIdEntityService;
import com.koch.ambeth.persistence.jdbc.alternateid.AlternateIdTest.AlternateIdModule;
import com.koch.ambeth.service.config.ServiceConfigurationConstants;
import com.koch.ambeth.service.merge.IEntityMetaDataProvider;
import com.koch.ambeth.service.merge.model.IEntityMetaData;
import com.koch.ambeth.testutil.AbstractInformationBusWithPersistenceTest;
import com.koch.ambeth.testutil.TestModule;
import com.koch.ambeth.testutil.TestProperties;
import com.koch.ambeth.testutil.TestPropertiesList;
import com.koch.ambeth.util.collections.ILinkedMap;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;

@TestModule({ AlternateIdModule.class, SimpleInMemoryDatabaseTestModule.class })
@TestProperties(name = ServiceConfigurationConstants.mappingFile, value = "com/koch/ambeth/inmemory/simpleinmemory_orm.xml")
public class SimpleInMemoryDatabaseTest extends AbstractInformationBusWithPersistenceTest
{
	public static class SimpleInMemoryDatabaseTestModule implements IInitializingModule
	{
		@Override
		public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
		{
			IBeanConfiguration inMemoryDatabase = beanContextFactory.registerBean(SimpleInMemoryDatabase.class);

			// beanContextFactory.link(inMemoryDatabase).to(ITransactionListenerExtendable.class);
			beanContextFactory.link(inMemoryDatabase, "handleDatabaseAcquire").to(IEventListenerExtendable.class).with(DatabaseAcquireEvent.class);
			// beanContextFactory.link(inMemoryDatabase).to(IEventListenerExtendable.class).with(DatabaseCommitEvent.class);
			beanContextFactory.link(inMemoryDatabase, "handleDatabasePreCommit").to(IEventListenerExtendable.class).with(DatabasePreCommitEvent.class);
			beanContextFactory.link(inMemoryDatabase, "handleDatabaseFail").to(IEventListenerExtendable.class).with(DatabaseFailEvent.class);

			beanContextFactory.link(inMemoryDatabase).to(ICacheRetrieverExtendable.class).with(AlternateIdEntity.class);
			beanContextFactory.link(inMemoryDatabase).to(IMergeServiceExtensionExtendable.class).with(AlternateIdEntity.class);

			beanContextFactory.link(inMemoryDatabase).to(ICacheRetrieverExtendable.class).with(BaseEntity.class);
			beanContextFactory.link(inMemoryDatabase).to(IMergeServiceExtensionExtendable.class).with(BaseEntity.class);

			beanContextFactory.link(inMemoryDatabase).to(ICacheRetrieverExtendable.class).with(BaseEntity2.class);
			beanContextFactory.link(inMemoryDatabase).to(IMergeServiceExtensionExtendable.class).with(BaseEntity2.class);
		}
	}

	protected String name = "myNameIs";

	@Autowired
	protected IMultithreadingHelper multithreadingHelper;

	@Autowired
	protected IAlternateIdEntityService service;

	@Autowired(CacheNamedBeans.CacheProviderSingleton)
	protected ICacheProvider cacheProvider;

	protected AlternateIdEntity createEntity()
	{
		AlternateIdEntity aie = entityFactory.createEntity(AlternateIdEntity.class);
		aie.setName(name);

		service.updateAlternateIdEntity(aie);
		return aie;
	}

	@Test
	public void createAlternateIdEntity()
	{
		AlternateIdEntity aie = createEntity();

		Assert.assertFalse("Wrong id", aie.getId() == 0);
		Assert.assertEquals("Wrong version!", (short) 1, aie.getVersion());
	}

	@Test
	public void createAlternateIdEntity_emptyAlternateId()
	{
		AlternateIdEntity aie = entityFactory.createEntity(AlternateIdEntity.class);

		service.updateAlternateIdEntity(aie);

		Assert.assertFalse("Wrong id", aie.getId() == 0);
		Assert.assertEquals("Wrong version!", (short) 1, aie.getVersion());
	}

	@Test
	public void selectByPrimitive()
	{
		String name = createEntity().getName();
		AlternateIdEntity aieReloaded = service.getAlternateIdEntityByName(name);
		Assert.assertNotNull("Entity must be valid", aieReloaded);
	}

	@Test
	public void alternateIdSimpleRead()
	{
		AlternateIdEntity entity = createEntity();

		ICache cache = cacheProvider.getCurrentCache();

		AlternateIdEntity entityFromCacheById = cache.getObject(entity.getClass(), entity.getId());
		AlternateIdEntity entityFromCacheById2 = cache.getObject(entity.getClass(), "Id", entity.getId());
		AlternateIdEntity entityFromCacheByName = cache.getObject(entity.getClass(), "Name", entity.getName());

		Assert.assertSame(entityFromCacheById, entityFromCacheById2);
		Assert.assertSame(entityFromCacheById, entityFromCacheByName);
	}

	@Test
	public void alternateIdChange()
	{
		AlternateIdEntity entity = createEntity();

		ICache cache = cacheProvider.getCurrentCache();
		// rootCache.clear();
		entity.setName(entity.getName() + "_2");

		AlternateIdEntity entityFromCacheById = cache.getObject(entity.getClass(), entity.getId());

		service.updateAlternateIdEntity(entity);

		AlternateIdEntity entityFromCacheByIdAfterChange = cache.getObject(entity.getClass(), entity.getId());

		Assert.assertSame(entityFromCacheById, entityFromCacheByIdAfterChange);
	}

	@Test
	@TestPropertiesList({ @TestProperties(name = PersistenceConfigurationConstants.DatabasePoolMaxUsed, value = "5"),
			@TestProperties(name = PersistenceConfigurationConstants.DatabasePoolMaxUnused, value = "5") })
	public void isolationLevel()
	{
		final ICache cache = beanContext.getService(ICache.class);
		final String name1;
		final int id;
		{
			AlternateIdEntity entity = createEntity();
			id = entity.getId();
			name1 = entity.getName();
			beanContext.getService(IThreadLocalCleanupController.class).cleanupThreadLocal();
		}
		final CyclicBarrier[] barrier = new CyclicBarrier[6];
		for (int a = barrier.length; a-- > 0;)
		{
			barrier[a] = new CyclicBarrier(3);
		}
		Runnable run1 = new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					final AlternateIdEntity entity = cache.getObject(AlternateIdEntity.class, id);
					barrier[0].await();
					transaction.processAndCommit(new DatabaseCallback()
					{
						@Override
						public void callback(ILinkedMap<Object, IDatabase> persistenceUnitToDatabaseMap) throws Throwable
						{
							entity.setName(name1 + "1");
							service.updateAlternateIdEntity(entity);
							entity.setName(name1 + "11");
							service.updateAlternateIdEntity(entity);
							barrier[1].await();
							barrier[2].await();
						}
					});
					barrier[3].await();
					barrier[4].await();
					barrier[5].await();
				}
				catch (Throwable e)
				{
					throw RuntimeExceptionUtil.mask(e);
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
					final AlternateIdEntity entity = cache.getObject(AlternateIdEntity.class, id);
					barrier[0].await();
					barrier[1].await();
					try
					{
						transaction.processAndCommit(new DatabaseCallback()
						{
							@Override
							public void callback(ILinkedMap<Object, IDatabase> persistenceUnitToDatabaseMap) throws Throwable
							{
								entity.setName(name1 + "2");
								service.updateAlternateIdEntity(entity);
							}
						});
						throw new IllegalStateException(OptimisticLockException.class.getSimpleName() + " expected");
					}
					catch (OptimisticLockException e)
					{
						// intended blank
					}
					barrier[2].await();
					barrier[3].await();
					barrier[4].await();
					try
					{
						service.updateAlternateIdEntity(entity);
						throw new IllegalStateException(OptimisticLockException.class.getSimpleName() + " expected");
					}
					catch (OptimisticLockException e)
					{
						// intended blank
					}
					barrier[5].await();
				}
				catch (Throwable e)
				{
					throw RuntimeExceptionUtil.mask(e);
				}
			}
		};
		Runnable run3 = new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					barrier[0].await();
					barrier[1].await();
					barrier[2].await();
					barrier[3].await();
					barrier[4].await();
					barrier[5].await();
					AlternateIdEntity entity = cache.getObject(AlternateIdEntity.class, id);
					entity.setName(name1 + "3");
					service.updateAlternateIdEntity(entity);
				}
				catch (Throwable e)
				{
					throw RuntimeExceptionUtil.mask(e);
				}
			}
		};
		multithreadingHelper.invokeInParallel(beanContext, run1, run2, run3);
	}

	@Test
	public void selectByArray()
	{
		String name = createEntity().getName();
		AlternateIdEntity aieReloaded2 = service.getAlternateIdEntityByNames(name);
		Assert.assertNotNull("Entity must be valid", aieReloaded2);
	}

	@Test
	public void selectByList()
	{
		String name = createEntity().getName();
		ArrayList<String> namesList = new ArrayList<String>();
		namesList.add(name);
		AlternateIdEntity aieReloaded3 = service.getAlternateIdEntityByNames(namesList);
		Assert.assertNotNull("Entity must be valid", aieReloaded3);
	}

	@Test
	public void selectBySet()
	{
		String name = createEntity().getName();
		HashSet<String> namesSet = new HashSet<String>();
		namesSet.add(name);
		AlternateIdEntity aieReloaded4 = service.getAlternateIdEntityByNames(namesSet);
		Assert.assertNotNull("Entity must be valid", aieReloaded4);
	}

	@Test
	public void selectListByArray()
	{
		String name = createEntity().getName();
		List<AlternateIdEntity> list = service.getAlternateIdEntitiesByNamesReturnList(name);
		Assert.assertNotNull("List must be valid", list);
		Assert.assertEquals("Size is wrong", 1, list.size());
		Assert.assertNotNull("Entity must be valid", list.get(0));
	}

	@Test
	public void selectSetByArray()
	{
		String name = createEntity().getName();
		Set<AlternateIdEntity> set = service.getAlternateIdEntitiesByNamesReturnSet(name);
		Assert.assertNotNull("List must be valid", set);
		Assert.assertEquals("Size is wrong", 1, set.size());
		Assert.assertNotNull("Entity must be valid", set.iterator().next());
	}

	@Test
	public void selectArrayByArray()
	{
		String name = createEntity().getName();
		AlternateIdEntity[] array = service.getAlternateIdEntitiesByNamesReturnArray(name);
		Assert.assertNotNull("Array must be valid", array);
		Assert.assertEquals("Size is wrong", 1, array.length);
		Assert.assertNotNull("Entity must be valid", array[0]);
	}

	/**
	 * BaseEntity2 has two unique fields (aka alternate id fields). One of them is a foreign key field and so should not be used as an alternate id field.
	 */
	@Test
	public void testBaseEntity2()
	{
		IEntityMetaData metaData = beanContext.getService(IEntityMetaDataProvider.class).getMetaData(BaseEntity2.class);

		Assert.assertEquals(1, metaData.getAlternateIdMembers().length);
	}
}
