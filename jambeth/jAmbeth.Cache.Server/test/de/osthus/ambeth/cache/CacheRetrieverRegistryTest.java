package de.osthus.ambeth.cache;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

import de.osthus.ambeth.cache.CacheRetrieverRegistryTest.CacheRetrieverTestModule;
import de.osthus.ambeth.cache.model.ILoadContainer;
import de.osthus.ambeth.cache.model.IObjRelation;
import de.osthus.ambeth.cache.transfer.ObjRelation;
import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.ILinkedMap;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.ioc.IInitializingModule;
import de.osthus.ambeth.ioc.exception.ExtendableException;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.merge.EntityMetaDataFake;
import de.osthus.ambeth.merge.IEntityMetaDataProvider;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.merge.transfer.ObjRef;
import de.osthus.ambeth.service.ICacheRetriever;
import de.osthus.ambeth.service.ICacheRetrieverExtendable;
import de.osthus.ambeth.testutil.AbstractIocTest;
import de.osthus.ambeth.testutil.TestModule;
import de.osthus.ambeth.testutil.TestRebuildContext;
import de.osthus.ambeth.typeinfo.IRelationInfoItem;
import de.osthus.ambeth.typeinfo.ITypeInfoItem;
import de.osthus.ambeth.typeinfo.MethodPropertyInfo;
import de.osthus.ambeth.typeinfo.PropertyInfoItem;
import de.osthus.ambeth.util.ParamChecker;

@TestModule(CacheRetrieverTestModule.class)
@TestRebuildContext(true)
public class CacheRetrieverRegistryTest extends AbstractIocTest
{
	public static class CacheRetrieverTestModule implements IInitializingModule
	{
		@Override
		public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
		{
			EntityMetaDataFake entityMetaDataProvider = new EntityMetaDataFake();
			entityMetaDataProvider.addMetaData(Date.class, new PropertyInfoItem(new MethodPropertyInfo(Date.class, "Time", Date.class.getMethod("getTime"),
					Date.class.getMethod("setTime", long.class))), null, new ITypeInfoItem[0], new IRelationInfoItem[0]);

			beanContextFactory.registerWithLifecycle(entityMetaDataProvider).autowireable(IEntityMetaDataProvider.class);

			beanContextFactory.registerBean("cacheRetrieverRegistry", CacheRetrieverRegistry.class).propertyRef("DefaultCacheRetriever", "cr1")
					.autowireable(ICacheRetriever.class, ICacheRetrieverExtendable.class, CacheRetrieverRegistry.class);

			CacheRetrieverFake cr1 = new CacheRetrieverFake();
			CacheRetrieverFake cr2 = new CacheRetrieverFake();

			for (int i = 0; i < 2; i++)
			{
				cr1.entities.put(objRefs[i], new LoadContainerFake(objRefs[i], null, null));
			}
			for (int i = 2; i < 4; i++)
			{
				cr2.entities.put(objRefs[i], new LoadContainerFake(objRefs[i], null, null));
			}

			beanContextFactory.registerWithLifecycle("cr1", cr1);
			beanContextFactory.registerWithLifecycle("cr2", cr2);

			beanContextFactory.link("cr2").to(ICacheRetrieverExtendable.class).with(Integer.class);
			beanContextFactory.link("cr2").to(ICacheRetrieverExtendable.class).with(Date.class);
		}
	}

	private static final IObjRef[] objRefs = { new ObjRef(String.class, 1, 1), new ObjRef(String.class, 2, 1), new ObjRef(Integer.class, 1, 1),
			new ObjRef(Date.class, 4, 1), };

	private static final IObjRelation[] objRels = { new ObjRelation(new IObjRef[] { objRefs[0] }, "Member1"),
			new ObjRelation(new IObjRef[] { objRefs[1] }, "Member1"), new ObjRelation(new IObjRef[] { objRefs[2] }, "Member1"),
			new ObjRelation(new IObjRef[] { objRefs[3] }, "Member1"), new ObjRelation(new IObjRef[] { objRefs[3] }, "Member2") };

	private ICacheRetriever cacheRetriever1;

	private ICacheRetriever cacheRetriever2;

	protected CacheRetrieverRegistry fixture;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		super.afterPropertiesSet();

		ParamChecker.assertNotNull(fixture, "fixture");

		cacheRetriever1 = beanContext.getService("cr1", ICacheRetriever.class);
		cacheRetriever2 = beanContext.getService("cr2", ICacheRetriever.class);
	}

	public void setFixture(CacheRetrieverRegistry fixture)
	{
		this.fixture = fixture;
	}

	@Test
	public void testRegisterCacheRetriever()
	{
		assertNull("Fix test: CacheRetriever for Long.class already registered", fixture.typeToCacheRetrieverMap.getExtensions().get(Long.class));

		fixture.registerCacheRetriever(cacheRetriever2, Long.class);
		assertSame("Registration failed!", cacheRetriever2, fixture.typeToCacheRetrieverMap.getExtensions().get(Long.class));
	}

	@Test(expected = ExtendableException.class)
	public void testRegisterCacheRetriever_duplicatRegistration()
	{
		fixture.registerCacheRetriever(cacheRetriever2, Date.class);
	}

	@Test(expected = ExtendableException.class)
	public void testRegisterCacheRetriever_alreadyRegistered()
	{
		fixture.registerCacheRetriever(cacheRetriever1, Date.class);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testRegisterCacheRetriever_noService()
	{
		fixture.registerCacheRetriever(null, Date.class);
	}

	@Test
	public void testUnregisterCacheRetriever()
	{
		assertNotNull("Fix test: CacheRetriever for Integer.class not registered", fixture.typeToCacheRetrieverMap.getExtensions().get(Integer.class));

		fixture.unregisterCacheRetriever(cacheRetriever2, Integer.class);
		assertFalse("Unregistration failed!", fixture.typeToCacheRetrieverMap.getExtensions().containsKey(Integer.class));

		fixture.registerCacheRetriever(cacheRetriever2, Integer.class);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testUnregisterCacheRetriever_wrongService()
	{
		fixture.unregisterCacheRetriever(cacheRetriever1, Integer.class);
	}

	@Test
	public void testGetEntities()
	{
		List<IObjRef> orisToLoad = Arrays.asList(objRefs);
		List<ILoadContainer> actual = fixture.getEntities(orisToLoad);
		assertNotNull(actual);
		assertEquals(objRefs.length, actual.size());
		for (int i = actual.size(); i-- > 0;)
		{
			IObjRef ref = actual.get(i).getReference();
			assertTrue("Object missing: " + ref.getRealType() + ", " + ref.getId(), orisToLoad.contains(ref));
		}

		orisToLoad = new ArrayList<IObjRef>();
		actual = fixture.getEntities(orisToLoad);
		assertNotNull(actual);
		assertTrue(actual.isEmpty());

		orisToLoad.add(new ObjRef(Double.class, 23, 2));
		actual = fixture.getEntities(orisToLoad);
		assertNotNull(actual);
		assertTrue(actual.isEmpty());

		orisToLoad.add(objRefs[0]);
		actual = fixture.getEntities(orisToLoad);
		assertNotNull(actual);
		assertEquals(1, actual.size());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetEntities_nullPointer()
	{
		fixture.getEntities(null);
	}

	@Test
	@Ignore
	public void testGetRelations()
	{
		// TODO
		fail("Not yet implemented");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetRelations_nullPointer()
	{
		fixture.getRelations(null);
	}

	@Test
	public void testGetServiceForType()
	{
		assertNull(fixture.getRetrieverForType(null));

		assertSame(cacheRetriever1, fixture.getRetrieverForType(String.class));
		assertSame(cacheRetriever2, fixture.getRetrieverForType(Integer.class));
		assertSame(cacheRetriever2, fixture.getRetrieverForType(Date.class));

		assertFalse("Fix test: Should test for not assigned type.", fixture.typeToCacheRetrieverMap.getExtensions().containsKey(Long.class));
		assertSame(cacheRetriever1, fixture.getRetrieverForType(Long.class));
	}

	@Test(expected = IllegalStateException.class)
	public void testGetServiceForType_noDefaultCacheRetrievers() throws Throwable
	{
		fixture.setDefaultCacheRetriever(null);
		fixture.getRetrieverForType(String.class);
	}

	@Test
	public void testSortIObjRefs()
	{
		ILinkedMap<Class<?>, IList<IObjRef>> actual = fixture.bucketSortObjRefs(Arrays.asList(objRefs));
		assertNotNull(actual);
		assertEquals(3, actual.size());

		assertNotNull(actual.get(String.class));
		assertEquals(2, actual.get(String.class).size());

		assertNotNull(actual.get(Integer.class));
		assertEquals(1, actual.get(Integer.class).size());

		assertNotNull(actual.get(Date.class));
		assertEquals(1, actual.get(Date.class).size());
	}

	@Test
	public void testAssignObjRefsToCacheRetriever()
	{
		ILinkedMap<Class<?>, IList<IObjRef>> sortedIObjRefs = fixture.bucketSortObjRefs(Arrays.asList(objRefs));
		ILinkedMap<ICacheRetriever, IList<IObjRef>> actual = fixture.assignObjRefsToCacheRetriever(sortedIObjRefs);
		assertNotNull(actual);
		assertEquals(2, actual.size());

		assertNotNull(actual.get(cacheRetriever1));
		assertEquals(2, actual.get(cacheRetriever1).size());

		assertNotNull(actual.get(cacheRetriever2));
		assertEquals(2, actual.get(cacheRetriever2).size());
	}

	@Test
	@Ignore
	public void testAssignObjRelsToCacheRetriever()
	{
		ILinkedMap<Class<?>, IList<IObjRelation>> sortedIObjRels = fixture.bucketSortObjRels(Arrays.asList(objRels));
		ILinkedMap<ICacheRetriever, IList<IObjRelation>> actual = fixture.assignObjRelsToCacheRetriever(sortedIObjRels);
		assertNotNull(actual);
		assertEquals(2, actual.size());

		assertNotNull(actual.get(cacheRetriever1));
		assertEquals(2, actual.get(cacheRetriever1).size());

		assertNotNull(actual.get(cacheRetriever2));
		assertEquals(2, actual.get(cacheRetriever2).size());
	}

}
