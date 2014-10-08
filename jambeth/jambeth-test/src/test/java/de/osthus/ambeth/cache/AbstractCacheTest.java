package de.osthus.ambeth.cache;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import de.osthus.ambeth.cache.AbstractCacheTest.AbstractCacheTestModule;
import de.osthus.ambeth.cache.model.ILoadContainer;
import de.osthus.ambeth.cache.model.IObjRelation;
import de.osthus.ambeth.cache.model.IObjRelationResult;
import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.ioc.IInitializingModule;
import de.osthus.ambeth.ioc.annotation.FrameworkModule;
import de.osthus.ambeth.ioc.config.IBeanConfiguration;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.merge.model.IEntityMetaData;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.merge.transfer.ObjRef;
import de.osthus.ambeth.model.Material;
import de.osthus.ambeth.model.Unit;
import de.osthus.ambeth.service.ICacheRetrieverExtendable;
import de.osthus.ambeth.testutil.AbstractInformationBusTest;
import de.osthus.ambeth.testutil.TestFrameworkModule;
import de.osthus.ambeth.testutil.TestRebuildContext;

@TestFrameworkModule(AbstractCacheTestModule.class)
@TestRebuildContext
public class AbstractCacheTest extends AbstractInformationBusTest
{
	@FrameworkModule
	public static class AbstractCacheTestModule implements IInitializingModule
	{
		@Override
		public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
		{
			IBeanConfiguration cacheRetrieverFakeBC = beanContextFactory.registerAnonymousBean(CacheRetrieverFake.class);
			beanContextFactory.link(cacheRetrieverFakeBC).to(ICacheRetrieverExtendable.class).with(Material.class);
			beanContextFactory.link(cacheRetrieverFakeBC).to(ICacheRetrieverExtendable.class).with(Unit.class);
		}
	}

	private IList<Object> fakeResults;

	private AbstractCache<AbstractCacheValue> fixture;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		super.afterPropertiesSet();
	}

	/**
	 * IMPORTANT TO KNOW:
	 * 
	 * The abstract method getObjects(List<IObjRef>, Set<CacheDirective>) is overridden to return a list containing the content of the static list
	 * 'fakeResults', the ObjRef list provided as search values and the given set of CacheDirectives.
	 * 
	 * @throws Exception
	 */
	@Before
	public void setUp() throws Exception
	{
		this.fixture = new AbstractCache<AbstractCacheValue>()
		{
			@Override
			public ICache getCurrentCache()
			{
				return this;
			}

			@Override
			public boolean isPrivileged()
			{
				return true;
			}

			@Override
			public IList<Object> getObjects(List<IObjRef> orisToGet, Set<CacheDirective> cacheDirective)
			{
				IList<Object> results = new ArrayList<Object>(fakeResults);
				results.addAll(orisToGet);
				results.add(cacheDirective);
				return results;
			}

			@Override
			public IList<IObjRelationResult> getObjRelations(List<IObjRelation> objRels, Set<CacheDirective> cacheDirective)
			{
				throw new UnsupportedOperationException();
			}

			@Override
			protected CacheKey[] getAlternateCacheKeysFromCacheValue(IEntityMetaData metaData, AbstractCacheValue cacheValue)
			{
				return new CacheKey[0];
			}

			@Override
			public <E> IList<E> getObjects(Class<E> type, List<?> ids)
			{
				return null;
			}

			@Override
			public <E> E getObject(Class<E> type, Object id, Set<CacheDirective> cacheDirective)
			{
				return null;
			}

			@Override
			protected Object getIdOfCacheValue(IEntityMetaData metaData, AbstractCacheValue cacheValue)
			{
				return cacheValue.getId();
			}

			@Override
			protected Object getVersionOfCacheValue(IEntityMetaData metaData, AbstractCacheValue cacheValue)
			{
				return cacheValue.getVersion();
			}

			@Override
			protected void setIdOfCacheValue(IEntityMetaData metaData, AbstractCacheValue cacheValue, Object id)
			{
				cacheValue.setId(id);
			}

			@Override
			protected void setVersionOfCacheValue(IEntityMetaData metaData, AbstractCacheValue cacheValue, Object version)
			{
				cacheValue.setVersion(version);
			}

			@Override
			public AbstractCacheValue createCacheValueInstance(final IEntityMetaData metaData, Object obj)
			{
				return new AbstractCacheValue()
				{
					protected Object id;

					protected Object version;

					@Override
					public Object getId()
					{
						return id;
					}

					@Override
					public void setId(Object id)
					{
						this.id = id;
					}

					@Override
					public Object getVersion()
					{
						return version;
					}

					@Override
					public void setVersion(Object version)
					{
						this.version = version;
					}

					@Override
					public Class<?> getEntityType()
					{
						return metaData.getEntityType();
					}

					@Override
					public Object getPrimitive(int primitiveIndex)
					{
						return null;
					}

					@Override
					public Object[] getPrimitives()
					{
						return new Object[0];
					}
				};
			}

			@Override
			protected void putInternObjRelation(AbstractCacheValue cacheValue, IEntityMetaData metaData, IObjRelation objRelation, IObjRef[] relationsOfMember)
			{
				// Intended blank
			}

			@Override
			protected void putIntern(ILoadContainer loadContainer)
			{
				throw new UnsupportedOperationException();
			}
		};
		this.fixture = beanContext.registerExternalBean(fixture).finish();
		fakeResults = new ArrayList<Object>();
	}

	@After
	public void tearDown() throws Exception
	{
		fakeResults = null;
	}

	@Test
	public void testAfterPropertiesSet()
	{
		this.fixture.afterPropertiesSet();
	}

	@Test
	@Ignore
	public void testGetObjectClassOfEObject()
	{
		Object actual = this.fixture.getObject(Material.class, 1);
		assertNotNull(actual);
		assertTrue(actual instanceof ObjRef);
	}

	@Test
	public void testGetObjectIObjRef()
	{
		IObjRef ori = new ObjRef(Material.class, 1, 1);
		Object actual = this.fixture.getObject(ori, Collections.<CacheDirective> emptySet());
		assertNotNull(actual);
		assertSame(ori, actual);
	}

	@Test
	@Ignore
	public void testGetObjectClassOfEObjectSetOfCacheDirective()
	{
		Object actual = this.fixture.getObject(Material.class, 1, Collections.<CacheDirective> emptySet());
		assertNotNull(actual);
		assertTrue(actual instanceof ObjRef);
	}

	@Test
	public void testGetObjectIObjRefSetOfCacheDirective()
	{
		IObjRef ori = new ObjRef(Material.class, 1, 1);
		Object actual = this.fixture.getObject(ori, Collections.<CacheDirective> emptySet());
		assertNotNull(actual);
		assertSame(ori, actual);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testGetObjectsClassOfEObjectArray()
	{
		IList<?> actual;
		actual = this.fixture.getObjects(Material.class, new Object[0]);
		assertNotNull(actual);
		assertEquals(1, actual.size());
		assertEquals(0, ((Set<CacheDirective>) actual.get(0)).size());

		fakeResults.add(1);
		Object[] ids = new Object[] { 2, 5 };
		actual = this.fixture.getObjects(Material.class, ids);
		assertNotNull(actual);
		assertEquals(4, actual.size());
		assertEquals(1, actual.get(0));
		assertTrue(actual.get(1) instanceof ObjRef);
		// Does not make sense because the ObjRef instances where already put back in the OC
		// assertTrue(Arrays.asList(ids).contains(((ObjRef) actual.get(1)).getId()));
		assertTrue(actual.get(2) instanceof ObjRef);
		// Does not make sense because the ObjRef instances where already put back in the OC
		// assertTrue(Arrays.asList(ids).contains(((ObjRef) actual.get(2)).getId()));
		assertEquals(0, ((Set<CacheDirective>) actual.get(3)).size());
	}

	@SuppressWarnings("unchecked")
	@Test
	@Ignore
	public void testGetObjectsClassOfEListOfObject()
	{
		IList<?> actual;
		actual = this.fixture.getObjects(Material.class, Collections.<Object> emptyList());
		assertNotNull(actual);
		assertEquals(1, actual.size());
		assertTrue(((Set<CacheDirective>) actual.get(0)).contains(CacheDirective.None));

		fakeResults.add(1);
		List<Object> ids = Arrays.asList(new Object[] { 2, 4 });
		actual = this.fixture.getObjects(Material.class, ids);
		assertNotNull(actual);
		assertEquals(4, actual.size());
		assertEquals(1, actual.get(0));
		assertTrue(actual.get(1) instanceof ObjRef);
		assertTrue(ids.contains(((ObjRef) actual.get(1)).getId()));
		assertTrue(actual.get(2) instanceof ObjRef);
		assertTrue(ids.contains(((ObjRef) actual.get(2)).getId()));
		assertTrue(((Set<CacheDirective>) actual.get(3)).contains(CacheDirective.None));
	}

	@SuppressWarnings("unchecked")
	@Test
	@Ignore
	public void testGetObjectsIObjRefArraySetOfCacheDirective()
	{
		IList<Object> actual;
		IObjRef[] orisToGetArray = ObjRef.EMPTY_ARRAY;
		actual = this.fixture.getObjects(orisToGetArray, Collections.<CacheDirective> emptySet());
		assertNotNull(actual);
		assertEquals(1, actual.size());
		assertTrue(((Set<CacheDirective>) actual.get(0)).contains(CacheDirective.None));

		fakeResults.add(1);
		orisToGetArray = new IObjRef[] { new ObjRef(Material.class, 2, null), new ObjRef(Material.class, 4, null) };
		actual = this.fixture.getObjects(orisToGetArray, Collections.<CacheDirective> emptySet());
		assertNotNull(actual);
		assertEquals(4, actual.size());
		assertEquals(1, actual.get(0));
		assertTrue(actual.get(1) instanceof ObjRef);
		assertTrue(Arrays.asList(orisToGetArray).contains(actual.get(1)));
		assertTrue(actual.get(2) instanceof ObjRef);
		assertTrue(Arrays.asList(orisToGetArray).contains(actual.get(2)));
		assertTrue(((Set<CacheDirective>) actual.get(3)).contains(CacheDirective.None));
	}

	@Test(expected = NullPointerException.class)
	public void testGetObjectsIObjRefArraySetOfCacheDirective_null()
	{
		this.fixture.getObjects((IObjRef[]) null, Collections.<CacheDirective> emptySet());
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testGetContent()
	{
		this.fixture.getContent(null);
	}
}
