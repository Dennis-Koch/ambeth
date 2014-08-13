package de.osthus.ambeth.inmemory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import de.osthus.ambeth.cache.ICache;
import de.osthus.ambeth.cache.ICacheProvider;
import de.osthus.ambeth.cache.config.CacheNamedBeans;
import de.osthus.ambeth.config.ServiceConfigurationConstants;
import de.osthus.ambeth.database.ITransactionListenerExtendable;
import de.osthus.ambeth.event.DatabaseAcquireEvent;
import de.osthus.ambeth.event.DatabaseFailEvent;
import de.osthus.ambeth.event.IEventListenerExtendable;
import de.osthus.ambeth.inmemory.SimpleInMemoryDatabaseTest.SimpleInMemoryDatabaseTestModule;
import de.osthus.ambeth.ioc.IInitializingModule;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.ioc.config.IBeanConfiguration;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.merge.IEntityMetaDataProvider;
import de.osthus.ambeth.merge.IMergeServiceExtensionExtendable;
import de.osthus.ambeth.merge.model.IEntityMetaData;
import de.osthus.ambeth.persistence.jdbc.alternateid.AlternateIdEntity;
import de.osthus.ambeth.persistence.jdbc.alternateid.AlternateIdTest.AlternateIdModule;
import de.osthus.ambeth.persistence.jdbc.alternateid.BaseEntity;
import de.osthus.ambeth.persistence.jdbc.alternateid.BaseEntity2;
import de.osthus.ambeth.persistence.jdbc.alternateid.IAlternateIdEntityService;
import de.osthus.ambeth.service.ICacheRetrieverExtendable;
import de.osthus.ambeth.testutil.AbstractPersistenceTest;
import de.osthus.ambeth.testutil.TestModule;
import de.osthus.ambeth.testutil.TestProperties;

@TestModule({ AlternateIdModule.class, SimpleInMemoryDatabaseTestModule.class })
@TestProperties(name = ServiceConfigurationConstants.mappingFile, value = "de/osthus/ambeth/inmemory/simpleinmemory_orm.xml")
public class SimpleInMemoryDatabaseTest extends AbstractPersistenceTest
{
	public static class SimpleInMemoryDatabaseTestModule implements IInitializingModule
	{
		@Override
		public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
		{
			IBeanConfiguration inMemoryDatabase = beanContextFactory.registerAnonymousBean(SimpleInMemoryDatabase.class);

			beanContextFactory.link(inMemoryDatabase).to(ITransactionListenerExtendable.class);
			beanContextFactory.link(inMemoryDatabase).to(IEventListenerExtendable.class).with(DatabaseAcquireEvent.class);
			// beanContextFactory.link(inMemoryDatabase).to(IEventListenerExtendable.class).with(DatabaseCommitEvent.class);
			beanContextFactory.link(inMemoryDatabase).to(IEventListenerExtendable.class).with(DatabaseFailEvent.class);

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
	protected IAlternateIdEntityService service;

	@Autowired(CacheNamedBeans.CacheProviderSingleton)
	protected ICacheProvider cacheProvider;

	protected AlternateIdEntity createEntity()
	{
		AlternateIdEntity aie = entityFactory.createEntity(AlternateIdEntity.class);
		aie.setName(name);

		this.service.updateAlternateIdEntity(aie);
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

		this.service.updateAlternateIdEntity(aie);

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

		this.service.updateAlternateIdEntity(entity);

		AlternateIdEntity entityFromCacheByIdAfterChange = cache.getObject(entity.getClass(), entity.getId());

		Assert.assertSame(entityFromCacheById, entityFromCacheByIdAfterChange);
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
