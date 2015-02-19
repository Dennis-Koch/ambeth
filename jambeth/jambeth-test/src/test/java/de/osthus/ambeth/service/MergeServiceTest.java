package de.osthus.ambeth.service;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import net.sf.cglib.proxy.Factory;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import de.osthus.ambeth.cache.CacheFactoryDirective;
import de.osthus.ambeth.cache.ChildCache;
import de.osthus.ambeth.cache.ICache;
import de.osthus.ambeth.cache.ICacheFactory;
import de.osthus.ambeth.cache.IRootCache;
import de.osthus.ambeth.cache.rootcachevalue.RootCacheValue;
import de.osthus.ambeth.change.ILinkChangeCommand;
import de.osthus.ambeth.change.ITableChange;
import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.collections.ILinkedMap;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.collections.LinkedHashMap;
import de.osthus.ambeth.config.ServiceConfigurationConstants;
import de.osthus.ambeth.ioc.MergeServerModule;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.merge.IEntityMetaDataProvider;
import de.osthus.ambeth.merge.IMergeServiceExtension;
import de.osthus.ambeth.merge.IProxyHelper;
import de.osthus.ambeth.merge.IValueObjectConfig;
import de.osthus.ambeth.merge.model.ICUDResult;
import de.osthus.ambeth.merge.model.IChangeContainer;
import de.osthus.ambeth.merge.model.IEntityMetaData;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.merge.model.IOriCollection;
import de.osthus.ambeth.merge.model.IPrimitiveUpdateItem;
import de.osthus.ambeth.merge.model.IRelationUpdateItem;
import de.osthus.ambeth.merge.transfer.CUDResult;
import de.osthus.ambeth.merge.transfer.CreateContainer;
import de.osthus.ambeth.merge.transfer.DeleteContainer;
import de.osthus.ambeth.merge.transfer.DirectObjRef;
import de.osthus.ambeth.merge.transfer.ObjRef;
import de.osthus.ambeth.merge.transfer.PrimitiveUpdateItem;
import de.osthus.ambeth.merge.transfer.RelationUpdateItem;
import de.osthus.ambeth.merge.transfer.UpdateContainer;
import de.osthus.ambeth.persistence.xml.TestServicesModule;
import de.osthus.ambeth.persistence.xml.model.Address;
import de.osthus.ambeth.persistence.xml.model.Employee;
import de.osthus.ambeth.persistence.xml.model.EmployeeType;
import de.osthus.ambeth.persistence.xml.model.Project;
import de.osthus.ambeth.proxy.ICascadedInterceptor;
import de.osthus.ambeth.testutil.AbstractInformationBusWithPersistenceTest;
import de.osthus.ambeth.testutil.SQLData;
import de.osthus.ambeth.testutil.SQLStructure;
import de.osthus.ambeth.testutil.TestModule;
import de.osthus.ambeth.testutil.TestProperties;
import de.osthus.ambeth.testutil.TestPropertiesList;

@SQLData("../persistence/xml/Relations_data.sql")
@SQLStructure("../persistence/xml/Relations_structure.sql")
@TestPropertiesList({ @TestProperties(name = ServiceConfigurationConstants.mappingFile, value = "de/osthus/ambeth/persistence/xml/orm.xml"),
		@TestProperties(name = ServiceConfigurationConstants.valueObjectFile, value = "de/osthus/ambeth/persistence/xml/value-object.xml"),
		@TestProperties(name = ServiceConfigurationConstants.GenericTransferMapping, value = "true") })
@TestModule(TestServicesModule.class)
public class MergeServiceTest extends AbstractInformationBusWithPersistenceTest
{
	@Autowired
	protected ICacheFactory cacheFactory;

	@Autowired
	protected ICache cache;

	@Autowired
	protected IProxyHelper proxyHelper;

	protected IMergeServiceExtension fixtureProxy;

	protected PersistenceMergeServiceExtension fixture;

	protected ChildCache childCache;

	protected IRootCache rootCache;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		super.afterPropertiesSet();

		fixtureProxy = beanContext.getService(MergeServerModule.MERGE_SERVICE_SERVER, IMergeServiceExtension.class);
		Factory proxy = (Factory) fixtureProxy;
		ICascadedInterceptor inter = (ICascadedInterceptor) proxy.getCallbacks()[0];
		fixture = (PersistenceMergeServiceExtension) inter.getTarget();

		childCache = (ChildCache) cacheFactory.create(CacheFactoryDirective.SubscribeGlobalDCE, "test");
		rootCache = ((IRootCache) childCache.getParent()).getCurrentRootCache();
	}

	@Test
	public void testMergeService()
	{
		Assert.assertNotNull(fixtureProxy);
	}

	@Test
	public void testGetMetaData()
	{
		IEntityMetaDataProvider entityMetaDataProvider = beanContext.getService(IEntityMetaDataProvider.class);
		IEntityMetaData expected = entityMetaDataProvider.getMetaData(Employee.class);
		assertNotNull(expected);
		List<IEntityMetaData> actuals = fixtureProxy.getMetaData(Arrays.asList(new Class<?>[] { Employee.class }));
		assertEquals(1, actuals.size());
		assertSame(expected, actuals.get(0));
	}

	@Test
	public void testGetValueObjectConfig()
	{
		IEntityMetaDataProvider entityMetaDataProvider = beanContext.getService(IEntityMetaDataProvider.class);
		IValueObjectConfig expected = entityMetaDataProvider.getValueObjectConfig(EmployeeType.class);
		assertNotNull(expected);
		IValueObjectConfig actual = fixtureProxy.getValueObjectConfig(EmployeeType.class);
		assertNotNull(actual);
		assertSame(expected, actual);
	}

	@Test
	public void testMerge_empty()
	{
		List<IChangeContainer> allChanges = new ArrayList<IChangeContainer>();
		List<Object> originalRefs = new ArrayList<Object>();
		ICUDResult cudResult = new CUDResult(allChanges, originalRefs);
		IOriCollection actual = fixtureProxy.merge(cudResult, null);
		assertTrue(actual.getAllChangeORIs().isEmpty());
		assertEquals("anonymous", actual.getChangedBy());
		assertTrue(System.currentTimeMillis() - actual.getChangedOn() < 500);
	}

	@Test
	public void testMerge_create()
	{
		Employee toCreate = entityFactory.createEntity(Employee.class);
		toCreate.setName("testMerge_create");

		List<IChangeContainer> allChanges = new ArrayList<IChangeContainer>();
		IObjRef expected = new DirectObjRef(Employee.class, toCreate);
		CreateContainer container = generateCreateContainer(expected, toCreate.getName());
		allChanges.add(container);

		ICUDResult cudResult = new CUDResult(allChanges, null);
		IOriCollection actual = fixtureProxy.merge(cudResult, null);
		assertTrue(System.currentTimeMillis() - actual.getChangedOn() < 500);
		assertEquals("anonymous", actual.getChangedBy());
		assertEquals(1, actual.getAllChangeORIs().size());
		IObjRef ori = actual.getAllChangeORIs().get(0);
		assertSame(expected, ori);
		assertEquals(Employee.class, ori.getRealType());
		assertNotNull(ori.getId());
		assertTrue(0 > Integer.valueOf(0).compareTo((Integer) ori.getId()));
		assertEquals(1, ((Short) ori.getVersion()).intValue());
	}

	@Test
	public void testMerge_createCombined()
	{
		Employee toCreate = entityFactory.createEntity(Employee.class);
		toCreate.setName("testMerge_create");

		List<IChangeContainer> allChanges = new ArrayList<IChangeContainer>();
		IObjRef expected = new DirectObjRef(Employee.class, toCreate);
		CreateContainer container = new CreateContainer();
		container.setReference(expected);
		PrimitiveUpdateItem pui = new PrimitiveUpdateItem();
		pui.setMemberName("Name");
		pui.setNewValue(toCreate.getName());
		container.setPrimitives(new IPrimitiveUpdateItem[] { pui });
		RelationUpdateItem rui1 = new RelationUpdateItem();
		rui1.setMemberName("PrimaryAddress");
		rui1.setAddedORIs(new IObjRef[] { new ObjRef(Address.class, 15, 1) });
		container.setRelations(new IRelationUpdateItem[] { rui1 });
		allChanges.add(container);

		UpdateContainer container2 = new UpdateContainer();
		container2.setReference(expected);
		PrimitiveUpdateItem pui2 = new PrimitiveUpdateItem();
		pui2.setMemberName("Name");
		pui2.setNewValue("testMerge_createCombined");
		container2.setPrimitives(new IPrimitiveUpdateItem[] { pui2 });
		RelationUpdateItem rui2 = new RelationUpdateItem();
		rui2.setMemberName("PrimaryProject");
		rui2.setAddedORIs(new IObjRef[] { new ObjRef(Project.class, 22, 1) });
		container2.setRelations(new IRelationUpdateItem[] { rui2 });
		allChanges.add(container2);

		ICUDResult cudResult = new CUDResult(allChanges, null);
		IOriCollection actual = fixtureProxy.merge(cudResult, null);
		assertTrue(System.currentTimeMillis() - actual.getChangedOn() < 500);
		assertEquals("anonymous", actual.getChangedBy());
		assertEquals(2, actual.getAllChangeORIs().size());
		assertSame(expected, actual.getAllChangeORIs().get(1));
		IObjRef ori = actual.getAllChangeORIs().get(0);
		assertSame(expected, ori);
		assertEquals(Employee.class, ori.getRealType());
		assertNotNull(ori.getId());
		assertTrue(0 > Integer.valueOf(0).compareTo((Integer) ori.getId()));
		assertEquals(1, ((Short) ori.getVersion()).intValue());

		Employee loaded = cache.getObject(Employee.class, ori.getId());
		assertEquals("testMerge_createCombined", loaded.getName());
	}

	@Test
	public void testFillOriList()
	{
		List<IObjRef> oriList = new ArrayList<IObjRef>();
		List<IChangeContainer> allChanges = new ArrayList<IChangeContainer>();
		IList<IObjRef> toLoadForDeletion = new de.osthus.ambeth.collections.ArrayList<IObjRef>();
		fixture.fillOriList(oriList, allChanges, toLoadForDeletion);

		assertTrue(oriList.isEmpty());
		assertTrue(toLoadForDeletion.isEmpty());

		IObjRef[] expected = { new DirectObjRef(), null, new ObjRef(Employee.class, 42, 2) };
		IObjRef toDelete = new ObjRef(Project.class, 23, 1);
		IChangeContainer change = new CreateContainer();
		change.setReference(expected[0]);
		allChanges.add(change);
		change = new DeleteContainer();
		change.setReference(toDelete);
		allChanges.add(change);
		change = new UpdateContainer();
		change.setReference(expected[2]);
		allChanges.add(change);
		fixture.fillOriList(oriList, allChanges, toLoadForDeletion);

		assertArrayEquals(expected, oriList.toArray());
		assertEquals(1, toLoadForDeletion.size());
		assertSame(toDelete, toLoadForDeletion.get(0));
	}

	@Test(expected = RuntimeException.class)
	public void testGetEnsureTable_exception()
	{
		fixture.getEnsureTable(fixture.database, String.class);
	}

	@Test
	public void testLoadEntitiesForDeletion()
	{
		IList<IObjRef> toLoadForDeletion = new de.osthus.ambeth.collections.ArrayList<IObjRef>();
		IMap<IObjRef, RootCacheValue> toDeleteMap = new HashMap<IObjRef, RootCacheValue>();
		fixture.loadEntitiesForDeletion(toLoadForDeletion, toDeleteMap, rootCache);
		assertTrue(toLoadForDeletion.isEmpty());
		assertTrue(toDeleteMap.isEmpty());

		toLoadForDeletion.add(new ObjRef(Employee.class, 1, 1));
		toLoadForDeletion.add(new ObjRef(Project.class, 21, 1));
		toLoadForDeletion.add(new ObjRef(Address.class, 13, 1));
		fixture.loadEntitiesForDeletion(toLoadForDeletion, toDeleteMap, rootCache);
		assertEquals(3, toLoadForDeletion.size());
		assertEquals(4, toDeleteMap.size()); // 3 + 1 alternate ID entry for the Employee
		assertEquals(3, new HashSet<Object>(toDeleteMap.values()).size()); // proving 3 + 1 theory

		for (IObjRef ori : toLoadForDeletion)
		{
			RootCacheValue actual = toDeleteMap.get(ori);
			assertEquals(ori.getRealType(), actual.getEntityType());
		}
	}

	@Test
	@Ignore
	public void testConvertChangeContainersToCommands_oneCreateContainer()
	{
		Employee toCreate = entityFactory.createEntity(Employee.class);
		toCreate.setName("testMerge_create");

		List<IChangeContainer> allChanges = new ArrayList<IChangeContainer>();
		IObjRef expected = new DirectObjRef(Employee.class, toCreate);
		CreateContainer container = generateCreateContainer(expected, toCreate.getName());
		allChanges.add(container);

		IMap<String, ITableChange> tableChangeMap = new HashMap<String, ITableChange>();
		ILinkedMap<Class<?>, IList<IObjRef>> typeToIdlessReferenceMap = new LinkedHashMap<Class<?>, IList<IObjRef>>();
		ILinkedMap<ITableChange, IList<ILinkChangeCommand>> linkChangeCommands = new LinkedHashMap<ITableChange, IList<ILinkChangeCommand>>();
		IMap<IObjRef, RootCacheValue> toDeleteMap = new HashMap<IObjRef, RootCacheValue>();
		IMap<IObjRef, IChangeContainer> objRefToChangeContainerMap = new HashMap<IObjRef, IChangeContainer>();

		fixture.convertChangeContainersToCommands(fixture.database.getCurrent(), allChanges, tableChangeMap, typeToIdlessReferenceMap, linkChangeCommands,
				toDeleteMap, objRefToChangeContainerMap, (IRootCache) ((ChildCache) cache.getCurrentCache()).getParent().getCurrentCache(), null);

		fail("Not yet implemented"); // TODO
	}

	public CreateContainer generateCreateContainer(IObjRef ori, String name)
	{
		CreateContainer container = new CreateContainer();
		container.setReference(ori);
		PrimitiveUpdateItem pui = new PrimitiveUpdateItem();
		pui.setMemberName("Name");
		pui.setNewValue(name);
		container.setPrimitives(new IPrimitiveUpdateItem[] { pui });
		RelationUpdateItem rui1 = new RelationUpdateItem();
		rui1.setMemberName("PrimaryAddress");
		rui1.setAddedORIs(new IObjRef[] { new ObjRef(Address.class, 14, 1) });
		RelationUpdateItem rui2 = new RelationUpdateItem();
		rui2.setMemberName("PrimaryProject");
		rui2.setAddedORIs(new IObjRef[] { new ObjRef(Project.class, 22, 1) });
		container.setRelations(new IRelationUpdateItem[] { rui1, rui2 });

		return container;
	}
}
