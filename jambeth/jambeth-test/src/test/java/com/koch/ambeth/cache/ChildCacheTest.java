package com.koch.ambeth.cache;

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

import com.koch.ambeth.ObjectMother;
import com.koch.ambeth.cache.ChildCache;
import com.koch.ambeth.cache.IRootCache;
import com.koch.ambeth.cache.AbstractCacheTest.AbstractCacheTestModule;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.merge.IEntityFactory;
import com.koch.ambeth.merge.IObjRefHelper;
import com.koch.ambeth.merge.IProxyHelper;
import com.koch.ambeth.merge.cache.CacheDirective;
import com.koch.ambeth.merge.cache.HandleContentDelegate;
import com.koch.ambeth.merge.proxy.IObjRefContainer;
import com.koch.ambeth.merge.transfer.ObjRef;
import com.koch.ambeth.merge.util.ICacheHelper;
import com.koch.ambeth.model.Material;
import com.koch.ambeth.model.Unit;
import com.koch.ambeth.service.config.ServiceConfigurationConstants;
import com.koch.ambeth.service.merge.IEntityMetaDataProvider;
import com.koch.ambeth.service.merge.model.IEntityMetaData;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.testutil.AbstractInformationBusTest;
import com.koch.ambeth.testutil.TestFrameworkModule;
import com.koch.ambeth.testutil.TestProperties;
import com.koch.ambeth.testutil.TestRebuildContext;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.HashSet;
import com.koch.ambeth.util.collections.IList;
import com.koch.ambeth.util.collections.IdentityHashSet;

@TestProperties(name = ServiceConfigurationConstants.mappingFile, value = "com/koch/ambeth/model/material-materialgroup-unit-orm.xml")
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
		childCache = beanContext.registerBean(ChildCache.class)//
				.propertyValue("Parent", parent)//
				.propertyValue("Privileged", Boolean.FALSE)//
				.finish();
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

	@Test
	@Ignore
	public void testGetCurrent()
	{
		fail("Not yet implemented"); // TODO
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
		IObjRefContainer vhc = (IObjRefContainer) entityFactory.createEntity(Material.class);
		IObjRef[][] relations = new IObjRef[][] { {}, {} };

		Object[] primitives = cacheHelper.extractPrimitives(metaData, vhc);
		assertNull(childCache.getCacheValue(metaData, ObjRef.PRIMARY_KEY_INDEX, id));

		childCache.addDirect(metaData, id, version, vhc, primitives, relations);
		Object actual = childCache.getCacheValue(metaData, ObjRef.PRIMARY_KEY_INDEX, id);
		assertNotNull(actual);
		assertSame(vhc, actual);
		int unitIndex = metaData.getIndexByRelationName("Unit");
		assertTrue(!vhc.is__Initialized(unitIndex));
		assertTrue(vhc.get__ObjRefs(unitIndex).length == 0);

		relations[unitIndex] = new IObjRef[] { new ObjRef(Unit.class, 4, 2) };
		childCache.addDirect(metaData, id, version, vhc, primitives, relations);
		actual = childCache.getCacheValue(metaData, ObjRef.PRIMARY_KEY_INDEX, id);
		assertNotNull(actual);
		assertSame(vhc, actual);
		assertTrue(!vhc.is__Initialized(unitIndex));
		assertTrue(vhc.get__ObjRefs(unitIndex) == relations[unitIndex]);
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
