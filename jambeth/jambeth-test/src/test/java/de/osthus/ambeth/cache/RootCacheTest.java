package de.osthus.ambeth.cache;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Ignore;
import org.junit.Test;

import de.osthus.ambeth.ObjectMother;
import de.osthus.ambeth.cache.AbstractCacheTest.AbstractCacheTestModule;
import de.osthus.ambeth.cache.config.CacheConfigurationConstants;
import de.osthus.ambeth.cache.model.ILoadContainer;
import de.osthus.ambeth.cache.rootcachevalue.DefaultRootCacheValue;
import de.osthus.ambeth.cache.rootcachevalue.RootCacheValue;
import de.osthus.ambeth.cache.transfer.LoadContainer;
import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.collections.IdentityHashSet;
import de.osthus.ambeth.collections.LinkedHashSet;
import de.osthus.ambeth.config.ServiceConfigurationConstants;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.CacheModule;
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
import de.osthus.ambeth.testutil.TestModule;
import de.osthus.ambeth.testutil.TestProperties;
import de.osthus.ambeth.testutil.TestPropertiesList;
import de.osthus.ambeth.testutil.TestRebuildContext;
import de.osthus.ambeth.util.DirectValueHolderRef;
import de.osthus.ambeth.util.ReflectUtil;

@TestPropertiesList({ @TestProperties(name = CacheConfigurationConstants.SecondLevelCacheWeakActive, value = "false"),
		@TestProperties(name = ServiceConfigurationConstants.mappingFile, value = "de/osthus/ambeth/model/material-materialgroup-unit-orm.xml") })
@TestModule(AbstractCacheTestModule.class)
@TestRebuildContext
public class RootCacheTest extends AbstractInformationBusTest
{
	@Autowired
	protected ICacheFactory cacheFactory;

	@Autowired
	protected IEntityFactory entityFactory;

	@Autowired
	protected IEntityMetaDataProvider entityMetaDataProvider;

	@Autowired
	protected IFirstLevelCacheManager firstLevelCacheManager;

	@Autowired(CacheModule.COMMITTED_ROOT_CACHE)
	protected RootCache fixture;

	@Autowired
	protected IObjRefHelper oriHelper;

	@Autowired
	protected IProxyHelper proxyHelper;

	@Test
	public final void testSize()
	{
		assertEquals("Should be empty!", 0, fixture.size());

		fixture.put(ObjectMother.getNewMaterial(entityFactory, 1, 2, "Test Material"));
		assertEquals("Should contain one element!", 1, fixture.size());
	}

	@Test
	public final void testClear()
	{
		fixture.put(ObjectMother.getNewMaterial(entityFactory, 1, 2, "Test Material"));
		assertEquals("Should contain one element!", 1, fixture.size());

		fixture.clear();
		assertEquals("Should be empty!", 0, fixture.size());
	}

	@Test
	public final void testGetContent()
	{
		final AtomicBoolean locked = new AtomicBoolean(false);
		final AtomicInteger size = new AtomicInteger(0);
		final AtomicReference<List<Object>> content = new AtomicReference<List<Object>>(new ArrayList<Object>());

		HandleContentDelegate delegate = new HandleContentDelegate()
		{
			@Override
			public void invoke(Class<?> entityType, byte idIndex, Object id, Object value)
			{
				locked.set(fixture.getWriteLock().isWriteLockHeld());
				size.getAndIncrement();
				content.get().add(value);
			}
		};

		assertFalse("WriteLock is already held!", fixture.getWriteLock().isWriteLockHeld());
		fixture.getContent(delegate);
		assertFalse("WriteLock is still held!", fixture.getWriteLock().isWriteLockHeld());
		assertFalse("Delegate should not have run!", locked.get());
		assertEquals(0, size.get());
		assertEquals(0, content.get().size());

		Material material = ObjectMother.getNewMaterial(entityFactory, 1, 1, "Manual material");
		fixture.put(material);

		assertFalse("WriteLock is already held!", fixture.getWriteLock().isWriteLockHeld());
		fixture.getContent(delegate);
		assertFalse("WriteLock is still held!", fixture.getWriteLock().isWriteLockHeld());
		assertTrue("WriteLock was not held!", locked.get());
		assertEquals(1, size.get());
		assertEquals(1, content.get().size());
		equalsMaterialRootCacheValue(material, (RootCacheValue) content.get().get(0));
	}

	@Test
	public final void testGetCacheValue()
	{
		Material material1 = ObjectMother.getNewMaterial(entityFactory, 1, 1, "Material 1");
		Material material2 = ObjectMother.getNewMaterial(entityFactory, 2, 1, "Material 2");

		IEntityMetaData metaData = fixture.entityMetaDataProvider.getMetaData(Material.class);

		assertNull(fixture.getCacheValue(metaData, ObjRef.PRIMARY_KEY_INDEX, 1));

		fixture.put(material1);
		fixture.put(material2);
		RootCacheValue cachedMaterial1 = fixture.getCacheValue(metaData, ObjRef.PRIMARY_KEY_INDEX, 1);
		RootCacheValue cachedMaterial2 = fixture.getCacheValue(metaData, ObjRef.PRIMARY_KEY_INDEX, 2);
		assertNull(fixture.getCacheValue(metaData, ObjRef.PRIMARY_KEY_INDEX, 3));

		equalsMaterialRootCacheValue(material1, cachedMaterial1);
		equalsMaterialRootCacheValue(material2, cachedMaterial2);
	}

	@Test
	public final void testCreate()
	{
		ICache actual = cacheFactory.create(CacheFactoryDirective.NoDCE);
		assertNotNull("Returned null!", actual);
		assertTrue("Should be a ChildCache!", actual instanceof ChildCache);
		assertNotSame("Parent cache should be a proxy of any explicit root cache!", fixture, ((ChildCache) actual).getParent());
	}

	@Test
	public final void testExists()
	{
		fixture.put(ObjectMother.getNewMaterial(entityFactory, 3, 5, "Test Material"));

		assertTrue(fixture.exists(new ObjRef(Material.class, 3, 5)));
		assertTrue(fixture.exists(new ObjRef(Material.class, 3, 4)));
		assertTrue(fixture.exists(new ObjRef(Material.class, 3, null)));

		assertFalse(fixture.exists(new ObjRef(Unit.class, 3, 5)));
		assertFalse(fixture.exists(new ObjRef(Unit.class, 3, null)));

		assertFalse(fixture.exists(new ObjRef(Material.class, 2, 5)));
		assertFalse(fixture.exists(new ObjRef(Material.class, 2, null)));

		assertFalse(fixture.exists(new ObjRef(Material.class, 3, 6)));
	}

	@Test
	public final void testGetObjectsListOfIObjRefSetOfCacheDirective()
	{
		IList<Object> actual;
		List<IObjRef> orisToGetArray = Collections.<IObjRef> emptyList();
		actual = fixture.getObjects(orisToGetArray, CacheDirective.none());
		assertNotNull(actual);
		assertEquals(0, actual.size());

		Material material1 = ObjectMother.getNewMaterial(entityFactory, 2, 1, "Material ID 2");
		Material material2 = ObjectMother.getNewMaterial(entityFactory, 3, 1, "Material ID 3");
		Material material3 = ObjectMother.getNewMaterial(entityFactory, 4, 1, "Material ID 4");
		orisToGetArray = Arrays.asList(new IObjRef[] { oriHelper.entityToObjRef(material1), oriHelper.entityToObjRef(material3) });
		actual = fixture.getObjects(orisToGetArray, CacheDirective.none());
		assertNotNull(actual);
		assertEquals(0, actual.size());

		fixture.put(material1);
		fixture.put(material2);
		fixture.put(material3);
		actual = fixture.getObjects(orisToGetArray, CacheDirective.none());
		assertNotNull(actual);
		assertEquals(2, actual.size());
		assertEquals(material1, actual.get(0));
		assertEquals(material1.getName(), ((Material) actual.get(0)).getName());
		assertEquals(material1.getUnit(), ((Material) actual.get(0)).getUnit());
		assertNotSame(material1, actual.get(0));
		assertEquals(material3, actual.get(1));
		assertEquals(material3.getName(), ((Material) actual.get(1)).getName());
		assertEquals(material3.getUnit(), ((Material) actual.get(1)).getUnit());
		assertNotSame(material3, actual.get(1));

		assertNull(fixture.getObjects(orisToGetArray, EnumSet.of(CacheDirective.NoResult)));
	}

	/*
	 * The parameter targetCache is only used as parameter for createResult. Variations are tested there.
	 */
	@Test
	public final void testGetObjectsListOfIObjRefICacheInternSetOfCacheDirective()
	{
		Material material1 = ObjectMother.getNewMaterial(entityFactory, 3, 2, "Material 3");
		Material material2 = ObjectMother.getNewMaterial(entityFactory, 6, 1, "Material 6");
		Unit unit = ObjectMother.getNewUnit(entityFactory, 5, 1, "Unit 5");
		material1.setUnit(unit);

		IObjRef material1ObjRef = oriHelper.entityToObjRef(material1);
		List<IObjRef> orisToGet = new ArrayList<IObjRef>(
				new IObjRef[] { material1ObjRef, oriHelper.entityToObjRef(material2), new ObjRef(Material.class, 2, 1) });

		CacheRetrieverFake cacheRetriever = new CacheRetrieverFake();
		cacheRetriever.entities.put(material1ObjRef, entityToLoadContainer(material1));
		try
		{
			ReflectUtil.getDeclaredField(fixture.getClass(), "cacheRetriever").set(fixture, cacheRetriever);
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}

		EnumSet<CacheDirective> containerEarly = EnumSet.of(CacheDirective.LoadContainerResult, CacheDirective.FailEarly);
		assertEquals(0, fixture.getObjects(orisToGet, null, containerEarly).size());

		fixture.put(material2);
		IList<Object> actual = fixture.getObjects(orisToGet, null, containerEarly);
		assertEquals(1, actual.size());
		assertEquals(LoadContainer.class, actual.get(0).getClass());
		assertEquals(material2.getId(), ((LoadContainer) actual.get(0)).getReference().getId());

		assertEquals(2, fixture.getObjects(orisToGet, null, CacheDirective.loadContainerResult()).size());
	}

	@Test
	public final void testCreateResult()
	{
		Material material1 = ObjectMother.getNewMaterial(entityFactory, 3, 2, "Material 3");
		Material material2 = ObjectMother.getNewMaterial(entityFactory, 6, 1, "Material 6");
		List<IObjRef> orisToGet = new ArrayList<IObjRef>(new IObjRef[] { oriHelper.entityToObjRef(material1), oriHelper.entityToObjRef(material2),
				new ObjRef(Material.class, 2, 1) });

		assertEquals(0, fixture.createResult(orisToGet, null, CacheDirective.loadContainerResult(), null, true).size());

		fixture.put(material1);
		fixture.put(material2);

		assertNull(fixture.createResult(orisToGet, null, Collections.<CacheDirective> emptySet(), null, true));
		IList<Object> actual = fixture.createResult(orisToGet, null, CacheDirective.loadContainerResult(), null, true);
		assertNotNull(actual);
		assertEquals(2, actual.size());
		assertEquals("Wrong result type!", LoadContainer.class, actual.get(0).getClass());
		assertEquals("Wrong result type!", LoadContainer.class, actual.get(1).getClass());

		actual = fixture.createResult(orisToGet, null, EnumSet.of(CacheDirective.LoadContainerResult, CacheDirective.ReturnMisses), null, true);
		assertNotNull(actual);
		assertEquals(3, actual.size());

		actual = fixture.createResult(orisToGet, null, Collections.<CacheDirective> emptySet(),
				(ICacheIntern) cacheFactory.create(CacheFactoryDirective.NoDCE), true);
		assertNotNull(actual);
		assertEquals(2, actual.size());
		assertEquals("Wrong result type!", Material.class, proxyHelper.getRealType(actual.get(0).getClass()));
		assertEquals("Wrong result type!", Material.class, proxyHelper.getRealType(actual.get(1).getClass()));

		actual = fixture.createResult(orisToGet, null, CacheDirective.loadContainerResult(), (ICacheIntern) cacheFactory.create(CacheFactoryDirective.NoDCE),
				true);
		assertNotNull(actual);
		assertEquals(2, actual.size());
		assertEquals("Wrong result type!", LoadContainer.class, actual.get(0).getClass());
		assertEquals("Wrong result type!", LoadContainer.class, actual.get(1).getClass());
	}

	@Test
	public final void testCreateObjectFromScratch()
	{
		Class<?> entityType = Unit.class;
		Integer id = 3;
		Long version = 2l;
		String name = "test unit name";
		IObjRef[][] relations = ObjRef.EMPTY_ARRAY_ARRAY;

		ChildCache targetCache = beanContext.registerAnonymousBean(ChildCache.class).propertyValue("Parent", fixture).finish();

		IEntityMetaData metaData = entityMetaDataProvider.getMetaData(entityType);
		Object[] primitives = new Object[metaData.getPrimitiveMembers().length];
		primitives[metaData.getIndexByPrimitiveName("Name")] = name;

		RootCacheValue rcv = new DefaultRootCacheValue(metaData);
		rcv.setId(id);
		rcv.setVersion(version);
		rcv.setPrimitives(primitives);
		rcv.setRelations(relations);

		ArrayList<IObjRef> tempObjRefList = new ArrayList<IObjRef>(1);
		tempObjRefList.add(new ObjRef());

		Object actual = fixture.createObjectFromScratch(metaData, rcv, targetCache, tempObjRefList, false, null);
		assertNotNull("Returned null!", actual);
		assertEquals("Wrong type!", entityType, proxyHelper.getRealType(actual.getClass()));
		Unit castedActual = (Unit) actual;
		assertEquals("Wrong id!", id, castedActual.getId());
		assertEquals("Wrong version!", version, new Long(castedActual.getVersion()));
		assertEquals("Wrong name!", name, castedActual.getName());
	}

	@Test
	public final void testUpdateExistingObject()
	{
		ICacheIntern targetCache = (ICacheIntern) cacheFactory.create(CacheFactoryDirective.NoDCE);
		Material material = ObjectMother.getNewMaterial(entityFactory, 5, 2, "Update test");
		ILoadContainer container = entityToLoadContainer(material);
		Object obj = entityFactory.createEntity(Material.class);
		IEntityMetaData metaData = entityMetaDataProvider.getMetaData(Material.class);
		RootCacheValue rcv = new DefaultRootCacheValue(metaData);
		rcv.setId(material.getId());
		rcv.setVersion(material.getVersion());
		rcv.setPrimitives(container.getPrimitives());
		rcv.setRelations(container.getRelations());

		fixture.updateExistingObject(metaData, rcv, obj, targetCache, false, null);

		assertTrue(material.equals(obj));
	}

	@Test(expected = ClassCastException.class)
	public final void testUpdateExistingObject_wrongType() throws Throwable
	{
		ICacheIntern targetCache = (ICacheIntern) cacheFactory.create(CacheFactoryDirective.NoDCE);
		Material material = ObjectMother.getNewMaterial(entityFactory, 5, 2, "Update test");
		ILoadContainer container = entityToLoadContainer(material);
		Object obj = entityFactory.createEntity(Unit.class);
		IEntityMetaData metaData = entityMetaDataProvider.getMetaData(Material.class);
		RootCacheValue rcv = new DefaultRootCacheValue(metaData);
		rcv.setId(2);
		rcv.setVersion(2);
		rcv.setPrimitives(container.getPrimitives());
		rcv.setRelations(container.getRelations());
		try
		{
			fixture.updateExistingObject(metaData, rcv, obj, targetCache, false, null);
		}
		catch (Throwable e)
		{
			if (e.getCause() != null)
			{
				throw e.getCause();
			}
			else
			{
				throw e;
			}
		}
	}

	@Test(expected = UnsupportedOperationException.class)
	public final void testAddDirect()
	{
		IEntityMetaData metaData = entityMetaDataProvider.getMetaData(Material.class);
		fixture.addDirect(metaData, 1, 1, new Object(), ObjRef.EMPTY_ARRAY, ObjRef.EMPTY_ARRAY_ARRAY);
	}

	@Test
	public final void testPut()
	{
		Material material = ObjectMother.getNewMaterial(entityFactory, 5, 3, "testPut");
		ILoadContainer container = entityToLoadContainer(material);

		Set<CacheDirective> noneSet = CacheDirective.none();

		assertEquals(0, fixture.keyToCacheValueDict.size());

		fixture.put(material);
		assertEquals(1, fixture.keyToCacheValueDict.size());
		Object valueR = fixture.keyToCacheValueDict.iterator().next().getValue();
		RootCacheValue value = fixture.getCacheValueFromReference(valueR);
		assertEquals(container.getReference().getRealType(), value.getEntityType());
		assertEquals(container.getReference().getId(), value.getId());
		assertEquals(container.getReference().getVersion(), value.getVersion());
		assertArrayEquals(container.getPrimitives(), value.getPrimitives());

		Material cachedMaterial = fixture.getObject(Material.class, material.getId(), noneSet);
		assertTrue(material.equals(cachedMaterial));

		Material material2 = ObjectMother.getNewMaterial(entityFactory, 54, 1, "testRemoveIObjRef 2");
		fixture.put(material2);
		assertEquals(2, fixture.keyToCacheValueDict.size());
	}

	@Test
	public final void testPutInternObjectSetOfObjectSetOfIObjRef_single()
	{
		Material material = ObjectMother.getNewMaterial(entityFactory, 5, 3, "testPut");

		assertEquals(0, fixture.keyToCacheValueDict.size());

		IdentityHashSet<Object> alreadyHandledSet = new IdentityHashSet<Object>();
		fixture.putIntern(null, new ArrayList<Object>(), alreadyHandledSet, null);
		assertEquals(0, fixture.keyToCacheValueDict.size());

		alreadyHandledSet.add(material);
		fixture.putIntern(material, new ArrayList<Object>(), alreadyHandledSet, null);
		assertEquals(0, fixture.keyToCacheValueDict.size());

		alreadyHandledSet.clear();
		fixture.putIntern(material, new ArrayList<Object>(), alreadyHandledSet, null);
		assertEquals(1, fixture.keyToCacheValueDict.size());
	}

	@Test
	@Ignore
	public final void testPutInternObjectSetOfObjectSetOfIObjRef_noId()
	{
		// TODO Ohne ID testen --> Exception
	}

	@Test
	@Ignore
	public final void testPutInternObjectSetOfObjectSetOfIObjRef_list()
	{
		// TODO Mit Liste und anderer Collection testen
	}

	@Test
	@Ignore
	public final void testPutInternObjectSetOfObjectSetOfIObjRef_collection()
	{
		// TODO Mit Liste und anderer Collection testen
	}

	@Test
	public final void testPutInternClassOfQObjectObjectObjectArrayIObjRefArrayArray()
	{
		Material material = ObjectMother.getNewMaterial(entityFactory, 5, 3, "testPut");
		ILoadContainer container = entityToLoadContainer(material);

		IEntityMetaData metaData = fixture.entityMetaDataProvider.getMetaData(Material.class);

		CacheKey[] alternateCacheKeys = fixture.extractAlternateCacheKeys(metaData, container.getPrimitives());

		assertEquals(0, fixture.keyToCacheValueDict.size());

		fixture.putIntern(metaData, material, material.getId(), material.getVersion(), alternateCacheKeys, container.getPrimitives(), container.getRelations());
		assertEquals(1, fixture.keyToCacheValueDict.size());
		Object valueR = fixture.keyToCacheValueDict.iterator().next().getValue();
		RootCacheValue value = fixture.getCacheValueFromReference(valueR);
		assertEquals(material.getVersion(), value.getVersion());

		fixture.putIntern(metaData, material, material.getId(), material.getVersion() + 1, alternateCacheKeys, container.getPrimitives(),
				container.getRelations());
		assertEquals(1, fixture.keyToCacheValueDict.size());
		assertSame(value, fixture.keyToCacheValueDict.iterator().next().getValue());
		assertEquals((short) (material.getVersion() + 1), value.getVersion());
	}

	@Test
	@TestProperties(name = ServiceConfigurationConstants.ToOneDefaultCascadeLoadMode, value = "EAGER")
	public final void testEnsureRelationsExist()
	{
		IEntityMetaData metaData = entityMetaDataProvider.getMetaData(Material.class);
		IObjRef unitRef = new ObjRef(Unit.class, 9, 2);
		IObjRef[][] relations = ObjRef.EMPTY_ARRAY_ARRAY;
		LinkedHashSet<IObjRef> cascadeNeededORIs = null;
		ArrayList<DirectValueHolderRef> pendingValueHolders = new ArrayList<DirectValueHolderRef>();

		RootCacheValue cacheValue = new DefaultRootCacheValue(metaData);
		cacheValue.setId(1);
		cacheValue.setVersion(1);
		cacheValue.setRelations(relations);
		fixture.ensureRelationsExist(cacheValue, metaData, cascadeNeededORIs, pendingValueHolders);

		cacheValue.setRelations(new IObjRef[][] { null });
		fixture.ensureRelationsExist(cacheValue, metaData, cascadeNeededORIs, pendingValueHolders);

		cacheValue.setRelations(new IObjRef[][] { {} });
		fixture.ensureRelationsExist(cacheValue, metaData, cascadeNeededORIs, pendingValueHolders);

		cacheValue.setRelations(new IObjRef[][] { new IObjRef[] { unitRef } });
		cascadeNeededORIs = new LinkedHashSet<IObjRef>();
		fixture.ensureRelationsExist(cacheValue, metaData, cascadeNeededORIs, pendingValueHolders);
		assertEquals(3, pendingValueHolders.size());
		DirectValueHolderRef vhr = pendingValueHolders.get(2);
		int relationIndex = metaData.getIndexByRelation(vhr.getMember());
		assertNotNull(vhr.getVhc().get__ObjRefs(relationIndex));
		assertEquals(1, vhr.getVhc().get__ObjRefs(relationIndex).length);
		assertEquals(unitRef, vhr.getVhc().get__ObjRefs(relationIndex)[0]);
	}

	@Test
	public final void testRemoveIObjRef()
	{
		Material material1 = ObjectMother.getNewMaterial(entityFactory, 5, 3, "testRemoveIObjRef 1");
		Material material2 = ObjectMother.getNewMaterial(entityFactory, 54, 1, "testRemoveIObjRef 2");
		IObjRef ori1 = oriHelper.entityToObjRef(material1);
		IObjRef ori2 = oriHelper.entityToObjRef(material2);

		fixture.put(material1);
		fixture.put(material2);
		assertEquals(2, fixture.keyToCacheValueDict.size());

		fixture.remove(ori1);
		assertEquals(1, fixture.keyToCacheValueDict.size());

		fixture.remove(ori1);
		assertEquals(1, fixture.keyToCacheValueDict.size());

		fixture.remove(ori2);
		assertEquals(0, fixture.keyToCacheValueDict.size());
	}

	@Test
	public final void testRemoveClassOfQObject()
	{
		Material material1 = ObjectMother.getNewMaterial(entityFactory, 5, 3, "testRemoveIObjRef 1");
		Material material2 = ObjectMother.getNewMaterial(entityFactory, 54, 1, "testRemoveIObjRef 2");

		fixture.put(material1);
		fixture.put(material2);
		assertEquals(2, fixture.keyToCacheValueDict.size());

		fixture.remove(Unit.class, 5);
		assertEquals(2, fixture.keyToCacheValueDict.size());

		fixture.remove(Material.class, 6);
		assertEquals(2, fixture.keyToCacheValueDict.size());

		fixture.remove(Material.class, 5);
		assertEquals(1, fixture.keyToCacheValueDict.size());
	}

	@Test
	public final void testRemoveListOfIObjRef()
	{
		List<IObjRef> oris = Collections.<IObjRef> emptyList();
		fixture.remove(oris);
		assertEquals(0, fixture.keyToCacheValueDict.size());

		Material material1 = ObjectMother.getNewMaterial(entityFactory, 5, 3, "testRemoveList 1");
		Material material2 = ObjectMother.getNewMaterial(entityFactory, 54, 1, "testRemoveList 2");
		Material material3 = ObjectMother.getNewMaterial(entityFactory, 57, 3, "testRemoveList 3");
		IObjRef ori1 = oriHelper.entityToObjRef(material1);
		IObjRef ori2 = oriHelper.entityToObjRef(material2);
		IObjRef ori4 = new ObjRef(Unit.class, 57, 3);
		oris = new ArrayList<IObjRef>(Arrays.asList(ori1, ori2, ori4));

		fixture.put(material1);
		fixture.put(material2);
		fixture.put(material3);
		assertEquals(3, fixture.keyToCacheValueDict.size());

		fixture.remove(oris);
		assertEquals(1, fixture.keyToCacheValueDict.size());
	}

	// TODO JH @DeK Ich verstehe die Aufgabe der Methode nicht.
	@Ignore
	@Test
	public final void testApplyValues()
	{
		fixture.applyValues(null, null);

		Set<CacheDirective> noDirective = CacheDirective.none();
		Material material = ObjectMother.getNewMaterial(entityFactory, 5, 3, "testApplyValues");
		IObjRef materialRef = oriHelper.entityToObjRef(material);
		fixture.put(material);

		IEntityMetaData metaData = fixture.entityMetaDataProvider.getMetaData(Material.class);

		Material cachedMaterial = fixture.getObject(Material.class, 5, noDirective);
		Material independentMaterial = fixture.getObject(Material.class, 5, noDirective);
		assertNotSame(material, cachedMaterial);
		assertNotSame(material, independentMaterial);
		assertTrue(material.equals(cachedMaterial));
		assertTrue(material.equals(independentMaterial));

		ICacheIntern targetCache = (ICacheIntern) cacheFactory.create(CacheFactoryDirective.NoDCE);
		fixture.applyValues(cachedMaterial, targetCache);
		assertTrue(material.equals(targetCache.getObject(materialRef, noDirective)));
		assertTrue(material.equals(fixture.getObject(materialRef, noDirective)));

		fixture.getCacheValue(metaData, ObjRef.PRIMARY_KEY_INDEX, material.getId()).getPrimitives()[0] = "new name";
		assertFalse(material.equals(fixture.getObject(materialRef, noDirective)));
		assertTrue(material.equals(targetCache.getObject(materialRef, noDirective)));

		fixture.applyValues(cachedMaterial, targetCache);
		assertFalse(material.equals(fixture.getObject(materialRef, noDirective)));
		assertFalse(material.equals(targetCache.getObject(materialRef, noDirective)));
	}

	@Test(expected = IllegalArgumentException.class)
	public final void testApplyValues_notInCache()
	{
		Material material = ObjectMother.getNewMaterial(entityFactory, 5, 3, "testApplyValues_notInCache");
		if (!fixture.applyValues(material, null))
		{
			throw new IllegalArgumentException();
		}
	}

	private static void equalsMaterialRootCacheValue(Material material, RootCacheValue value)
	{
		assertEquals(Material.class, value.getEntityType());
		assertEquals(material.getId(), value.getId());
		assertEquals(material.getVersion(), value.getVersion());
		assertEquals(material.getName(), value.getPrimitives()[0]);

		// TODO Relations with ValueHolderInterceptor
	}

	private ILoadContainer entityToLoadContainer(Object entity)
	{
		IObjRef ori = oriHelper.entityToObjRef(entity);
		boolean existed = fixture.exists(ori);
		if (!existed)
		{
			fixture.put(entity);
		}
		ILoadContainer loadContainer = (ILoadContainer) fixture.getObject(ori, CacheDirective.loadContainerResult());
		if (!existed)
		{
			fixture.clear();
			// fixture.remove(ori);
		}
		return loadContainer;
	}
}
