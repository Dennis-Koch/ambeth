package de.osthus.ambeth.persistence.jdbc.alternateid;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import de.osthus.ambeth.cache.CacheFactoryDirective;
import de.osthus.ambeth.cache.ICache;
import de.osthus.ambeth.cache.ICacheContext;
import de.osthus.ambeth.cache.ICacheFactory;
import de.osthus.ambeth.cache.ICacheProvider;
import de.osthus.ambeth.cache.ISingleCacheRunnable;
import de.osthus.ambeth.cache.config.CacheNamedBeans;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.ioc.IInitializingModule;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.merge.IEntityMetaDataProvider;
import de.osthus.ambeth.merge.IMergeProcess;
import de.osthus.ambeth.merge.IProxyHelper;
import de.osthus.ambeth.merge.model.IEntityMetaData;
import de.osthus.ambeth.persistence.jdbc.alternateid.AlternateIdTest.AlternateIdModule;
import de.osthus.ambeth.query.IQuery;
import de.osthus.ambeth.query.IQueryBuilder;
import de.osthus.ambeth.service.config.ConfigurationConstants;
import de.osthus.ambeth.testutil.AbstractPersistenceTest;
import de.osthus.ambeth.testutil.SQLData;
import de.osthus.ambeth.testutil.SQLStructure;
import de.osthus.ambeth.testutil.TestModule;
import de.osthus.ambeth.testutil.TestProperties;
import de.osthus.ambeth.typeinfo.IRelationInfoItem;
import de.osthus.ambeth.util.ParamChecker;

@SQLData("alternateid_data.sql")
@SQLStructure("alternateid_structure.sql")
@TestModule(AlternateIdModule.class)
@TestProperties(name = ConfigurationConstants.mappingFile, value = "de/osthus/ambeth/persistence/jdbc/alternateid/alternateid_orm.xml")
public class AlternateIdTest extends AbstractPersistenceTest
{
	public static class AlternateIdModule implements IInitializingModule
	{
		@Override
		public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
		{
			beanContextFactory.registerAutowireableBean(IAlternateIdEntityService.class, AlternateIdEntityService.class);
		}
	}

	protected String name = "myNameIs";

	protected IAlternateIdEntityService service;

	protected ICacheProvider cacheProvider;

	protected IProxyHelper proxyHelper;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		super.afterPropertiesSet();

		ParamChecker.assertNotNull(proxyHelper, "ProxyHelper");

		cacheProvider = beanContext.getService(CacheNamedBeans.CacheProviderSingleton, ICacheProvider.class);
	}

	public void setProxyHelper(IProxyHelper proxyHelper)
	{
		this.proxyHelper = proxyHelper;
	}

	public void setService(IAlternateIdEntityService service)
	{
		this.service = service;
	}

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

	@Test
	public void testLazyValueHolderReferringToAlternateId() throws Throwable
	{
		ICacheFactory cacheFactory = beanContext.getService(ICacheFactory.class);
		ICacheContext cacheContext = beanContext.getService(ICacheContext.class);

		final AlternateIdEntity aeEntity = entityFactory.createEntity(AlternateIdEntity.class);
		BaseEntity2 be2 = entityFactory.createEntity(BaseEntity2.class);
		aeEntity.getBaseEntities2().add(be2);

		aeEntity.setName("AE_1");
		be2.setName("BE_2");
		cacheContext.executeWithCache(cacheFactory.create(CacheFactoryDirective.NoDCE), new ISingleCacheRunnable<Object>()
		{
			@Override
			public Object run() throws Throwable
			{
				IMergeProcess mergeProcess = beanContext.getService(IMergeProcess.class);

				mergeProcess.process(aeEntity, null, null, null);
				return null;
			}
		});
		cacheContext.executeWithCache(cacheFactory.create(CacheFactoryDirective.NoDCE), new ISingleCacheRunnable<Object>()
		{
			@Override
			public Object run() throws Throwable
			{
				IQueryBuilder<AlternateIdEntity> qb = queryBuilderFactory.create(AlternateIdEntity.class);
				IQuery<AlternateIdEntity> query = qb.build(qb.isEqualTo(qb.property("Id"), qb.value(aeEntity.getId())));
				IList<AlternateIdEntity> result = query.retrieve();
				Assert.assertEquals(1, result.size());
				AlternateIdEntity item = result.get(0);
				IEntityMetaData metaData = entityMetaDataProvider.getMetaData(AlternateIdEntity.class);
				IRelationInfoItem member = (IRelationInfoItem) metaData.getMemberByName("BaseEntities2");
				Assert.assertTrue(!proxyHelper.isInitialized(item, member));
				List<BaseEntity2> baseEntities2 = item.getBaseEntities2();
				BaseEntity2 baseEntity2 = baseEntities2.get(0);
				Assert.assertNotNull(baseEntity2);
				return null;
			}
		});
	}
}