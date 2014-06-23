package de.osthus.ambeth.cache;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import de.osthus.ambeth.ObjectMother;
import de.osthus.ambeth.cache.AbstractCacheTest.AbstractCacheTestModule;
import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.collections.HashSet;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.collections.IdentityHashSet;
import de.osthus.ambeth.config.ServiceConfigurationConstants;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.merge.IEntityFactory;
import de.osthus.ambeth.merge.IEntityMetaDataProvider;
import de.osthus.ambeth.merge.IObjRefHelper;
import de.osthus.ambeth.merge.IProxyHelper;
import de.osthus.ambeth.merge.model.IEntityMetaData;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.merge.transfer.ObjRef;
import de.osthus.ambeth.model.Material;
import de.osthus.ambeth.model.Unit;
import de.osthus.ambeth.testutil.AbstractInformationBusTest;
import de.osthus.ambeth.testutil.TestFrameworkModule;
import de.osthus.ambeth.testutil.TestProperties;
import de.osthus.ambeth.testutil.TestRebuildContext;
import de.osthus.ambeth.typeinfo.IRelationInfoItem;
import de.osthus.ambeth.util.CacheHelperFake;
import de.osthus.ambeth.util.CachePath;
import de.osthus.ambeth.util.ICacheHelper;
import de.osthus.ambeth.util.ReflectUtil;

@TestProperties(name = ServiceConfigurationConstants.mappingFile, value = "de/osthus/ambeth/model/material-materialgroup-unit-orm.xml")
@TestFrameworkModule(AbstractCacheTestModule.class)
@TestRebuildContext
public class ChildCacheTest extends AbstractInformationBusTest
{
	protected ChildCache childCache;

	@Autowired
	protected ICacheHelper cacheHelper;

	@Autowired
	protected IEntityFactory entityFactory;

	@Autowired
	protected IEntityMetaDataProvider entityMetaDataProvider;

	@Autowired
	protected IRootCache parent;

	@Autowired
	protected IObjRefHelper oriHelper;

	@Autowired
	protected IProxyHelper proxyHelper;

	@Before
	public void setUp() throws Exception
	{
		childCache = beanContext.registerAnonymousBean(ChildCache.class).propertyValue("Parent", parent).finish();
	}

	@After
	public void tearDown() throws Exception
	{
		childCache = null;
	}

	@Test
	public void testAfterPropertiesSet()
	{
		childCache.afterPropertiesSet();
	}

	@Test
	public void testSize()
	{
		assertEquals(0, childCache.size());

		parent.put(ObjectMother.getNewMaterial(entityFactory, 1, 1, "Manual material"));

		assertEquals(0, childCache.size());

		childCache.getObject(new ObjRef(Material.class, 1, 1), Collections.<CacheDirective> emptySet());
		assertEquals(1, childCache.size());
	}

	@Test
	public void testClear()
	{
		parent.put(ObjectMother.getNewMaterial(entityFactory, 1, 1, "Manual material"));

		childCache.getObject(new ObjRef(Material.class, 1, 1), Collections.<CacheDirective> emptySet());
		assertEquals(1, childCache.size());

		childCache.clear();
		assertEquals(0, childCache.size());
	}

	@Test
	public void testGetContent()
	{
		Material material = ObjectMother.getNewMaterial(entityFactory, 1, 1, "Manual material");
		parent.put(material);
		final AtomicInteger size = new AtomicInteger(0);
		final AtomicReference<List<Object>> content = new AtomicReference<List<Object>>(new ArrayList<Object>());

		HandleContentDelegate delegate = new HandleContentDelegate()
		{
			@Override
			public void invoke(Class<?> entityType, byte idIndex, Object id, Object value)
			{
				size.getAndIncrement();
				content.get().add(value);
			}
		};

		childCache.getContent(delegate);
		assertEquals(0, size.get());
		assertEquals(0, content.get().size());

		childCache.getObject(new ObjRef(Material.class, 1, 1), Collections.<CacheDirective> emptySet());
		childCache.getContent(delegate);
		assertEquals(1, size.get());
		assertEquals(1, content.get().size());
		assertTrue(material.equals(content.get().get(0)));
	}

	// TODO JH @DeK Ich verstehe die Aufgabe der Methode nicht.
	@Test
	public void testCascadeLoadPath()
	{
		assertNull(childCache.membersToInitialize);

		CacheHelperFake fakeCacheHelper = new CacheHelperFake();
		try
		{
			ReflectUtil.getDeclaredFieldInHierarchy(childCache.getClass(), "cacheHelper")[0].set(childCache, fakeCacheHelper);
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}

		Class<?> entityType = Material.class;
		String cascadeLoadPath = "";
		childCache.cascadeLoadPath(entityType, cascadeLoadPath);

		assertSame(entityType, fakeCacheHelper.entityType);
		assertSame(cascadeLoadPath, fakeCacheHelper.memberToInitialize);
		assertNotNull(childCache.membersToInitialize);
		List<CachePath> cachePaths = childCache.membersToInitialize.get(entityType);
		assertNotNull(cachePaths);
		assertSame(cachePaths, fakeCacheHelper.cachePaths);

		childCache.cascadeLoadPath(entityType, cascadeLoadPath);
	}

	@Test
	@Ignore
	public void testGetCurrent()
	{
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testIsResultCloned()
	{
		assertFalse("A childcache never clones objects by itself", childCache.isResultCloned());
	}

	@Test
	public void testGetMembersToInitialize()
	{
		assertNull(childCache.getMembersToInitialize());

		HashMap<Class<?>, List<CachePath>> expected = new HashMap<Class<?>, List<CachePath>>();
		childCache.membersToInitialize = expected;

		assertNotNull(childCache.getMembersToInitialize());
		assertSame(expected, childCache.getMembersToInitialize());
	}

	@Test
	public void testGetObjectsListOfIObjRefSetOfCacheDirective()
	{
		assertNotNull(childCache.getObjects((List<IObjRef>) null, null));
		assertNotNull(childCache.getObjects((List<IObjRef>) null, CacheDirective.none()));

		List<IObjRef> orisToGet = new ArrayList<IObjRef>();
		assertTrue(childCache.getObjects(orisToGet, Collections.<CacheDirective> emptySet()).isEmpty());

		orisToGet.add(new ObjRef(Material.class, 1, 1));

		assertTrue(childCache.getObjects(orisToGet, CacheDirective.failEarly()).isEmpty());
		assertTrue(childCache.getObjects(orisToGet, Collections.<CacheDirective> emptySet()).isEmpty());

		IList<Object> actual = childCache.getObjects(orisToGet, CacheDirective.returnMisses());
		assertEquals(1, actual.size());
		assertNull(actual.get(0));

		Material material = ObjectMother.getNewMaterial(entityFactory, 1, 1, "Manual material");
		parent.put(material);

		assertTrue(childCache.getObjects(orisToGet, CacheDirective.failEarly()).isEmpty());
		actual = childCache.getObjects(orisToGet, CacheDirective.returnMisses());
		assertEquals(1, actual.size());
		assertTrue("Materials are not equal!", material.equals(actual.get(0)));
		assertFalse(childCache.getObjects(orisToGet, CacheDirective.failEarly()).isEmpty());
	}

	@Test
	public void testGetObjectsListOfIObjRefICacheInternSetOfCacheDirective()
	{
		List<IObjRef> orisToGet;

		assertNotNull(childCache.getObjects((List<IObjRef>) null, null));
		assertNotNull(childCache.getObjects((List<IObjRef>) null, CacheDirective.none()));

		orisToGet = new ArrayList<IObjRef>();
		assertTrue(childCache.getObjects(orisToGet, Collections.<CacheDirective> emptySet()).isEmpty());

		orisToGet.add(new ObjRef(Material.class, 1, 1));
		assertTrue(childCache.getObjects(orisToGet, CacheDirective.failEarly()).isEmpty());
		assertTrue(childCache.getObjects(orisToGet, Collections.<CacheDirective> emptySet()).isEmpty());
		IList<Object> actual = childCache.getObjects(orisToGet, CacheDirective.returnMisses());
		assertEquals(1, actual.size());
		assertNull(actual.get(0));

		Material material = ObjectMother.getNewMaterial(entityFactory, 1, 1, "Manual material");
		parent.put(material);

		assertTrue(childCache.getObjects(orisToGet, CacheDirective.failEarly()).isEmpty());
		actual = childCache.getObjects(orisToGet, CacheDirective.returnMisses());
		assertEquals(1, actual.size());
		assertTrue("Materials are not equal!", material.equals(actual.get(0)));
		assertFalse(childCache.getObjects(orisToGet, CacheDirective.failEarly()).isEmpty());
	}

	@Test
	public void testGetCacheValue()
	{
		Material material1 = ObjectMother.getNewMaterial(entityFactory, 1, 1, "Material 1");
		Material material2 = ObjectMother.getNewMaterial(entityFactory, 2, 1, "Material 2");

		IEntityMetaData metaData = childCache.entityMetaDataProvider.getMetaData(Material.class);

		assertNull(childCache.getCacheValue(metaData, ObjRef.PRIMARY_KEY_INDEX, 1));

		parent.put(material1);
		parent.put(material2);
		assertNull(childCache.getCacheValue(metaData, ObjRef.PRIMARY_KEY_INDEX, 1));
		assertNull(childCache.getCacheValue(metaData, ObjRef.PRIMARY_KEY_INDEX, 2));

		Material cachedMaterial1 = childCache.getObject(Material.class, 1);

		Object cachedValue1 = childCache.getCacheValue(metaData, ObjRef.PRIMARY_KEY_INDEX, 1);
		assertNull(childCache.getCacheValue(metaData, ObjRef.PRIMARY_KEY_INDEX, 2));
		assertNull(childCache.getCacheValue(metaData, ObjRef.PRIMARY_KEY_INDEX, 3));

		assertNotNull(cachedValue1);
		assertSame(cachedMaterial1, cachedValue1);
	}

	@Test
	public void testAddDirect()
	{
		IEntityMetaData metaData = childCache.entityMetaDataProvider.getMetaData(Material.class);

		Object id = 2;
		Object version = 1;
		Material primitiveFilledObject = entityFactory.createEntity(Material.class);
		IObjRef[][] relations = new IObjRef[][] { {}, {} };

		Object[] primitives = cacheHelper.extractPrimitives(metaData, primitiveFilledObject);
		assertNull(childCache.getCacheValue(metaData, ObjRef.PRIMARY_KEY_INDEX, id));

		childCache.addDirect(metaData, id, version, primitiveFilledObject, primitives, relations);
		Object actual = childCache.getCacheValue(metaData, ObjRef.PRIMARY_KEY_INDEX, id);
		assertNotNull(actual);
		assertSame(primitiveFilledObject, actual);
		IRelationInfoItem unitMember = (IRelationInfoItem) entityMetaDataProvider.getMetaData(Material.class).getMemberByName("Unit");
		assertTrue(!proxyHelper.isInitialized(primitiveFilledObject, unitMember));
		assertTrue(proxyHelper.getObjRefs(primitiveFilledObject, unitMember).length == 0);

		int unitIndex = metaData.getIndexByRelationName("Unit");
		relations[unitIndex] = new IObjRef[] { new ObjRef(Unit.class, 4, 2) };
		childCache.addDirect(metaData, id, version, primitiveFilledObject, primitives, relations);
		actual = childCache.getCacheValue(metaData, ObjRef.PRIMARY_KEY_INDEX, id);
		assertNotNull(actual);
		assertSame(primitiveFilledObject, actual);
		assertTrue(!proxyHelper.isInitialized(primitiveFilledObject, unitMember));
		assertTrue(proxyHelper.getObjRefs(primitiveFilledObject, unitMember) == relations[unitIndex]);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testAddDirect_noId()
	{
		IEntityMetaData metaData = childCache.entityMetaDataProvider.getMetaData(Material.class);

		Object id = null;
		Object version = 1;
		Material primitiveFilledObject = entityFactory.createEntity(Material.class);
		IObjRef[][] relations = new IObjRef[][] { {}, {} };
		Object[] primitives = cacheHelper.extractPrimitives(metaData, primitiveFilledObject);

		childCache.addDirect(metaData, id, version, primitiveFilledObject, primitives, relations);
	}

	@Test(expected = RuntimeException.class)
	public void testAddDirect_noWrongInstance()
	{
		IEntityMetaData metaData = childCache.entityMetaDataProvider.getMetaData(Material.class);

		Object id = 2;
		Object version = 1;
		Object primitiveFilledObject = entityFactory.createEntity(Material.class);
		IObjRef[][] relations = new IObjRef[][] { {}, {} };
		Object[] primitives = cacheHelper.extractPrimitives(metaData, primitiveFilledObject);

		try
		{
			childCache.addDirect(metaData, id, version, primitiveFilledObject, primitives, relations);
		}
		catch (Exception e)
		{
			fail("Thrown to soon!");
		}
		Material otherMaterial = entityFactory.createEntity(Material.class);
		Object[] otherPrimitives = cacheHelper.extractPrimitives(metaData, primitiveFilledObject);
		childCache.addDirect(metaData, id, version, otherMaterial, otherPrimitives, relations);
	}

	@Ignore(value = "Method is never actually used!")
	@Test
	public void testPutIntern()
	{
		Object objectToCache = null;
		IdentityHashSet<Object> alreadyHandledSet = new IdentityHashSet<Object>();

		childCache.putIntern(objectToCache, new ArrayList<Object>(), alreadyHandledSet, new HashSet<IObjRef>());
		assertEquals(0, alreadyHandledSet.size());

		objectToCache = ObjectMother.getNewMaterial(entityFactory, 5, 2, "testPutIntern");

		// TODO Test mit Liste und Iterable

		fail("Not yet implemented"); // TODO
	}

	@Ignore(value = "Method is never actually used!")
	@Test(expected = IllegalArgumentException.class)
	public void testPutIntern_noId()
	{
		Object objectToCache = ObjectMother.getNewMaterial(entityFactory, null, 2, "testPutIntern");
		IdentityHashSet<Object> alreadyHandledSet = new IdentityHashSet<Object>();

		try
		{
			childCache.putIntern(objectToCache, new ArrayList<Object>(), alreadyHandledSet, new HashSet<IObjRef>());
		}
		catch (IllegalArgumentException e)
		{
			assertEquals(1, alreadyHandledSet.size());
			throw e;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			fail("Wrong exception thrown!");
		}
	}

	@Test
	public void testRemoveIObjRef()
	{
		Material material1 = ObjectMother.getNewMaterial(entityFactory, 5, 3, "testRemoveIObjRef 1");
		Material material2 = ObjectMother.getNewMaterial(entityFactory, 54, 1, "testRemoveIObjRef 2");
		IObjRef ori1 = oriHelper.entityToObjRef(material1);
		IObjRef ori2 = oriHelper.entityToObjRef(material2);

		parent.put(material1);
		parent.put(material2);
		assertNotNull(childCache.getObject(ori1, Collections.<CacheDirective> emptySet()));
		assertNotNull(childCache.getObject(ori2, Collections.<CacheDirective> emptySet()));
		assertEquals(2, childCache.keyToCacheValueDict.size());

		childCache.remove(ori1);
		assertEquals(1, childCache.keyToCacheValueDict.size());
		assertNotNull(childCache.getObject(ori1, Collections.<CacheDirective> emptySet()));
		assertEquals(2, childCache.keyToCacheValueDict.size());

		childCache.remove(ori1);
		assertEquals(1, childCache.keyToCacheValueDict.size());

		childCache.remove(ori1);
		assertEquals(1, childCache.keyToCacheValueDict.size());

		childCache.remove(ori2);
		assertEquals(0, childCache.keyToCacheValueDict.size());
	}

	@Test
	public void testRemoveClassOfQObject()
	{
		Material material1 = ObjectMother.getNewMaterial(entityFactory, 5, 3, "testRemoveIObjRef 1");
		Material material2 = ObjectMother.getNewMaterial(entityFactory, 54, 1, "testRemoveIObjRef 2");
		IObjRef ori1 = oriHelper.entityToObjRef(material1);
		IObjRef ori2 = oriHelper.entityToObjRef(material2);

		parent.put(material1);
		parent.put(material2);
		assertNotNull(childCache.getObject(ori1, Collections.<CacheDirective> emptySet()));
		assertNotNull(childCache.getObject(ori2, Collections.<CacheDirective> emptySet()));
		assertEquals(2, childCache.keyToCacheValueDict.size());

		childCache.remove(material1.getClass(), material1.getId());
		assertEquals(1, childCache.keyToCacheValueDict.size());
		assertNotNull(childCache.getObject(ori1, Collections.<CacheDirective> emptySet()));
		assertEquals(2, childCache.keyToCacheValueDict.size());

		childCache.remove(material1.getClass(), material1.getId());
		assertEquals(1, childCache.keyToCacheValueDict.size());

		childCache.remove(material1.getClass(), material1.getId());
		assertEquals(1, childCache.keyToCacheValueDict.size());

		childCache.remove(material2.getClass(), material2.getId());
		assertEquals(0, childCache.keyToCacheValueDict.size());
	}

	@Test
	public void testRemoveListOfIObjRef()
	{
		List<IObjRef> oris = Collections.<IObjRef> emptyList();
		childCache.remove(oris);
		assertEquals(0, childCache.keyToCacheValueDict.size());

		Material material1 = ObjectMother.getNewMaterial(entityFactory, 5, 3, "testRemoveList 1");
		Material material2 = ObjectMother.getNewMaterial(entityFactory, 54, 1, "testRemoveList 2");
		Material material3 = ObjectMother.getNewMaterial(entityFactory, 57, 3, "testRemoveList 3");
		IObjRef ori1 = oriHelper.entityToObjRef(material1);
		IObjRef ori2 = oriHelper.entityToObjRef(material2);
		IObjRef ori3 = oriHelper.entityToObjRef(material3);
		IObjRef ori4 = new ObjRef(Unit.class, 57, 3);
		oris = new ArrayList<IObjRef>(Arrays.asList(ori1, ori2, ori4));

		parent.put(material1);
		parent.put(material2);
		parent.put(material3);
		assertNotNull(childCache.getObject(ori1, Collections.<CacheDirective> emptySet()));
		assertNotNull(childCache.getObject(ori2, Collections.<CacheDirective> emptySet()));
		assertNotNull(childCache.getObject(ori3, Collections.<CacheDirective> emptySet()));
		assertNull(childCache.getObject(ori4, Collections.<CacheDirective> emptySet()));
		assertEquals(3, childCache.keyToCacheValueDict.size());

		childCache.remove(oris);
		assertEquals(1, childCache.keyToCacheValueDict.size());
	}

	@Test
	public void testGetReadLock()
	{
		assertTrue(parent.getReadLock() != childCache.getReadLock());
	}

	@Test
	public void testGetWriteLock()
	{
		assertTrue(parent.getWriteLock() != childCache.getWriteLock());
	}
}
