package de.osthus.ambeth.cache.valueholdercontainer;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Map.Entry;
import java.util.concurrent.CountDownLatch;

import org.junit.Assert;
import org.junit.Test;

import de.osthus.ambeth.cache.CacheFactoryDirective;
import de.osthus.ambeth.cache.ICacheFactory;
import de.osthus.ambeth.cache.ICacheIntern;
import de.osthus.ambeth.cache.ICacheModification;
import de.osthus.ambeth.cache.IDisposableCache;
import de.osthus.ambeth.cache.ValueHolderIEC;
import de.osthus.ambeth.cache.ValueHolderState;
import de.osthus.ambeth.cache.config.CacheConfigurationConstants;
import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.collections.ObservableArrayList;
import de.osthus.ambeth.ioc.BytecodeModule;
import de.osthus.ambeth.ioc.CacheBytecodeModule;
import de.osthus.ambeth.ioc.CacheDataChangeModule;
import de.osthus.ambeth.ioc.CacheModule;
import de.osthus.ambeth.ioc.CompositeIdModule;
import de.osthus.ambeth.ioc.EventModule;
import de.osthus.ambeth.ioc.MergeModule;
import de.osthus.ambeth.ioc.ObjectCopierModule;
import de.osthus.ambeth.ioc.ServiceModule;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.IEntityFactory;
import de.osthus.ambeth.merge.IEntityMetaDataProvider;
import de.osthus.ambeth.merge.IObjRefHelper;
import de.osthus.ambeth.merge.IProxyHelper;
import de.osthus.ambeth.merge.model.IEntityMetaData;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.merge.transfer.ObjRef;
import de.osthus.ambeth.model.IDataObject;
import de.osthus.ambeth.model.IEmbeddedType;
import de.osthus.ambeth.model.INotifyPropertyChanged;
import de.osthus.ambeth.model.INotifyPropertyChangedSource;
import de.osthus.ambeth.proxy.IEntityEquals;
import de.osthus.ambeth.proxy.IValueHolderContainer;
import de.osthus.ambeth.service.config.ConfigurationConstants;
import de.osthus.ambeth.template.ValueHolderContainerTemplate;
import de.osthus.ambeth.testutil.AbstractIocTest;
import de.osthus.ambeth.testutil.TestFrameworkModule;
import de.osthus.ambeth.testutil.TestProperties;
import de.osthus.ambeth.testutil.TestPropertiesList;
import de.osthus.ambeth.testutil.TestRebuildContext;
import de.osthus.ambeth.threading.IBackgroundWorkerDelegate;
import de.osthus.ambeth.threading.IGuiThreadHelper;
import de.osthus.ambeth.typeinfo.IRelationInfoItem;
import de.osthus.ambeth.util.ReflectUtil;

@TestPropertiesList({ @TestProperties(name = CacheConfigurationConstants.CacheServiceRegistryActive, value = "false"),
		@TestProperties(name = CacheConfigurationConstants.AsyncPropertyChangeActive, value = "false"),
		@TestProperties(name = ConfigurationConstants.IndependentMetaData, value = "true"),
		@TestProperties(name = ConfigurationConstants.mappingFile, value = "de/osthus/ambeth/cache/valueholdercontainer/orm.xml") })
@TestFrameworkModule({ ValueHolderContainerTestModule.class, BytecodeModule.class, CacheModule.class, CacheBytecodeModule.class, CacheDataChangeModule.class,
		CompositeIdModule.class, EventModule.class, MergeModule.class, ObjectCopierModule.class, ServiceModule.class })
@TestRebuildContext
public class ValueHolderContainerTest extends AbstractIocTest
{
	public static final String getIdName = "get__Id";

	@LogInstance
	private ILogger log;

	@Autowired
	protected ICacheFactory cacheFactory;

	@Autowired
	protected ICacheModification cacheModification;

	@Autowired
	protected IEntityFactory entityFactory;

	@Autowired
	protected IEntityMetaDataProvider entityMetaDataProvider;

	@Autowired
	protected IGuiThreadHelper guiThreadHelper;

	@Autowired
	protected IObjRefHelper oriHelper;

	@Autowired
	protected IProxyHelper proxyHelper;

	@Autowired
	protected ValueHolderContainerTemplate valueHolderContainerTemplate;

	// static int count = 10000000;
	//
	//
	// static Object[] objects;
	//
	// static
	// {
	// objects = new Object[count];
	// for (int a = count; a-- > 0; )
	// {
	// objects[a] = new Object();
	// }
	// }

	// [TestMethod]
	// public void test_Dictionary()
	// {
	// Dictionary<Object, Object> dict = new Dictionary<Object, Object>(count, new IdentityEqualityComparer<Object>());

	// //for (int a = count; a-- > 0; )
	// //{
	// // dict.add(a, objects[a]);
	// // dict.Remove(a);
	// // dict.add(a, objects[a]);
	// // dict.Remove(a);
	// // dict.add(a, objects[a]);
	// // dict.Remove(a);
	// // dict.add(a, objects[a]);
	// // dict.Remove(a);
	// // dict.add(a, objects[a]);
	// // dict.Remove(a);
	// //}
	// for (int a = count; a-- > 0; )
	// {
	// dict.add(a, objects[a]);
	// }
	// for (int a = 10; a-- > 0; )
	// {
	// foreach (KeyValuePair<Object, Object> entry in dict)
	// {
	// Object key = entry.Key;
	// Object value = entry.Value;
	// if (!Object.ReferenceEquals(objects[(int)key], value))
	// {
	// throw new Exception();
	// }
	// }
	// }
	// }

	// [TestMethod]
	// public void test_HashMap()
	// {
	// IdentityLinkedMap<Object, Object> dict = IdentityLinkedMap<Object, Object>.Create(count);

	// //for (int a = count; a-- > 0; )
	// //{
	// // dict.Put(a, objects[a]);
	// // dict.Remove(a);
	// // dict.Put(a, objects[a]);
	// // dict.Remove(a);
	// // dict.Put(a, objects[a]);
	// // dict.Remove(a);
	// // dict.Put(a, objects[a]);
	// // dict.Remove(a);
	// // dict.Put(a, objects[a]);
	// // dict.Remove(a);
	// //}
	// for (int a = count; a-- > 0; )
	// {
	// dict.Put(a, objects[a]);
	// }
	// for (int a = 10; a-- > 0; )
	// {
	// foreach (Entry<Object, Object> entry in dict)
	// {
	// Object key = entry.Key;
	// Object value = entry.Value;
	// if (!Object.ReferenceEquals(objects[(int)key], value))
	// {
	// throw new Exception();
	// }
	// }
	// }
	// }

	// [TestMethod]
	// public void test_PropertyInfo()
	// {
	// Material mat = new Material();

	// PropertyInfo pi = mat.getClass().GetProperty("Name");

	// DateTime prePi = DateTime.Now;
	// for (int a = count; a-- > 0; )
	// {
	// Object piValue = pi.GetValue(mat, null);
	// piValue = pi.GetValue(mat, null);
	// piValue = pi.GetValue(mat, null);
	// piValue = pi.GetValue(mat, null);
	// piValue = pi.GetValue(mat, null);
	// piValue = pi.GetValue(mat, null);
	// piValue = pi.GetValue(mat, null);
	// piValue = pi.GetValue(mat, null);
	// piValue = pi.GetValue(mat, null);
	// piValue = pi.GetValue(mat, null);
	// piValue = pi.GetValue(mat, null);
	// piValue = pi.GetValue(mat, null);
	// piValue = pi.GetValue(mat, null);
	// piValue = pi.GetValue(mat, null);
	// piValue = pi.GetValue(mat, null);
	// piValue = pi.GetValue(mat, null);
	// piValue = pi.GetValue(mat, null);
	// piValue = pi.GetValue(mat, null);
	// piValue = pi.GetValue(mat, null);
	// piValue = pi.GetValue(mat, null);
	// }
	// DateTime postPi = DateTime.Now;
	// long piTime = postPi.Ticks - prePi.Ticks;
	// Console.WriteLine(piTime);
	// }

	// [TestMethod]
	// public void test_GetDelegate()
	// {
	// Material mat = new Material();

	// MemberGetDelegate del = TypeUtility.GetMemberGetDelegate(mat.getClass(), "Name");

	// MemberSetDelegate setDel = TypeUtility.GetMemberSetDelegate(mat.getClass(), "Id");

	// setDel(mat, 5);

	// DateTime preDel = DateTime.Now;
	// for (int a = count; a-- > 0; )
	// {
	// Object delValue = del(mat);
	// delValue = del(mat);
	// delValue = del(mat);
	// delValue = del(mat);
	// delValue = del(mat);
	// delValue = del(mat);
	// delValue = del(mat);
	// delValue = del(mat);
	// delValue = del(mat);
	// delValue = del(mat);
	// delValue = del(mat);
	// delValue = del(mat);
	// delValue = del(mat);
	// delValue = del(mat);
	// delValue = del(mat);
	// delValue = del(mat);
	// delValue = del(mat);
	// delValue = del(mat);
	// delValue = del(mat);
	// delValue = del(mat);
	// }
	// DateTime postDel = DateTime.Now;

	// long delTime = postDel.Ticks - preDel.Ticks;
	// Console.WriteLine(delTime);
	// }

	protected void waitForUI()
	{
		guiThreadHelper.invokeInGuiAndWait(new IBackgroundWorkerDelegate()
		{
			@Override
			public void invoke() throws Throwable
			{
				// intended blank
			}
		});
	}

	@Test
	public void test_DataObject() throws Exception
	{
		Material obj = entityFactory.createEntity(Material.class);

		Assert.assertTrue(obj instanceof IDataObject);

		obj.setId(obj.getId());
		obj.setName(obj.getName());
		obj.setVersion(obj.getVersion());

		Assert.assertTrue(((IDataObject) obj).isToBeCreated());
		Assert.assertFalse(((IDataObject) obj).isToBeUpdated());
		Assert.assertFalse(((IDataObject) obj).isToBeDeleted());
		Assert.assertTrue(((IDataObject) obj).hasPendingChanges());

		obj.setId(0);
		obj.setName("name2");
		obj.setVersion(1);

		Object idNull = obj.getClass().getMethod(getIdName).invoke(obj, new Object[0]);
		Assert.assertNull(idNull);

		Assert.assertTrue(((IDataObject) obj).isToBeCreated());
		Assert.assertFalse(((IDataObject) obj).isToBeUpdated());
		Assert.assertFalse(((IDataObject) obj).isToBeDeleted());
		Assert.assertTrue(((IDataObject) obj).hasPendingChanges());

		obj.setId(1);

		Object idNotNull = obj.getClass().getMethod(getIdName).invoke(obj, new Object[0]);
		Assert.assertNotNull(idNotNull);

		Assert.assertFalse(((IDataObject) obj).isToBeCreated());
		Assert.assertTrue(((IDataObject) obj).isToBeUpdated());
		Assert.assertFalse(((IDataObject) obj).isToBeDeleted());
		Assert.assertTrue(((IDataObject) obj).hasPendingChanges());

		obj = entityFactory.createEntity(Material.class);
		boolean oldCacheModActive = cacheModification.isActive();
		cacheModification.setActive(true);
		try
		{
			obj.setId(1);
		}
		finally
		{
			cacheModification.setActive(oldCacheModActive);
		}
		idNotNull = obj.getClass().getMethod(getIdName).invoke(obj, new Object[0]);
		Assert.assertNotNull(idNotNull);

		Assert.assertFalse(((IDataObject) obj).isToBeCreated());
		Assert.assertFalse(((IDataObject) obj).isToBeUpdated());
		Assert.assertFalse(((IDataObject) obj).isToBeDeleted());
		Assert.assertFalse(((IDataObject) obj).hasPendingChanges());
	}

	@Test
	public void test_DataObject_Embedded()
	{
		Material obj = entityFactory.createEntity(Material.class);

		Assert.assertTrue(obj instanceof IDataObject);
		Assert.assertFalse(obj.getEmbMat() instanceof IDataObject);
		Assert.assertFalse(obj.getEmbMat().getEmbMat2() instanceof IDataObject);
		Assert.assertFalse(obj.getEmbMat3() instanceof IDataObject);

		obj.setId(1);
		((IDataObject) obj).setToBeUpdated(false);

		obj.getEmbMat().setName("Name");
		Assert.assertTrue(((IDataObject) obj).isToBeUpdated());
		((IDataObject) obj).setToBeUpdated(false);

		obj.getEmbMat().getEmbMat2().setName2("Name2");
		Assert.assertTrue(((IDataObject) obj).isToBeUpdated());
		((IDataObject) obj).setToBeUpdated(false);
	}

	@Test
	public void test_ValueHolderContainer_Embedded() throws Exception
	{
		Material obj = entityFactory.createEntity(Material.class);

		// Test EmbMat.EmbMatType
		Assert.assertTrue(obj.getEmbMat() instanceof IEmbeddedType);
		obj.getEmbMat().setName("Name2");
		Assert.assertEquals("Name2", obj.getEmbMat().getName());

		Assert.assertEquals(0, ReflectUtil.getDeclaredFieldInHierarchy(obj.getClass(), ValueHolderIEC.getInitializedFieldName("EmbMat.EmbMatType")).length);
		Assert.assertEquals(1, ReflectUtil.getDeclaredFieldInHierarchy(obj.getEmbMat().getClass(), ValueHolderIEC.getInitializedFieldName("EmbMatType")).length);

		IRelationInfoItem embMatTypeRI = (IRelationInfoItem) entityMetaDataProvider.getMetaData(obj.getClass()).getMemberByName("EmbMat.EmbMatType");

		Assert.assertFalse(proxyHelper.isInitialized(obj, embMatTypeRI));

		IObjRef[] emptyRefs = ObjRef.EMPTY_ARRAY;
		proxyHelper.setObjRefs(obj, embMatTypeRI, emptyRefs);
		IObjRef[] objRefs = proxyHelper.getObjRefs(obj, embMatTypeRI);
		Assert.assertSame(emptyRefs, objRefs);

		Assert.assertNull(obj.getEmbMat().getEmbMatType());
		Assert.assertTrue(proxyHelper.isInitialized(obj, embMatTypeRI));

		// Test EmbMat.getEmbMat2().EmbMatType2
		Assert.assertTrue(obj.getEmbMat().getEmbMat2() instanceof IEmbeddedType);
		obj.getEmbMat().getEmbMat2().setName2("Name3");
		Assert.assertEquals("Name3", obj.getEmbMat().getEmbMat2().getName2());

		Assert.assertEquals(0,
				ReflectUtil.getDeclaredFieldInHierarchy(obj.getClass(), ValueHolderIEC.getInitializedFieldName("EmbMat.EmbMat2.EmbMatType2")).length);
		Assert.assertEquals(0,
				ReflectUtil.getDeclaredFieldInHierarchy(obj.getEmbMat().getClass(), ValueHolderIEC.getInitializedFieldName("EmbMat2.EmbMatType2")).length);
		Assert.assertEquals(1,
				ReflectUtil.getDeclaredFieldInHierarchy(obj.getEmbMat().getEmbMat2().getClass(), ValueHolderIEC.getInitializedFieldName("EmbMatType2")).length);

		embMatTypeRI = (IRelationInfoItem) entityMetaDataProvider.getMetaData(obj.getClass()).getMemberByName("EmbMat.EmbMat2.EmbMatType2");

		Assert.assertFalse(proxyHelper.isInitialized(obj, embMatTypeRI));

		proxyHelper.setObjRefs(obj, embMatTypeRI, emptyRefs);
		objRefs = proxyHelper.getObjRefs(obj, embMatTypeRI);
		Assert.assertSame(emptyRefs, objRefs);

		Assert.assertNull(obj.getEmbMat().getEmbMat2().getEmbMatType2());
		Assert.assertTrue(proxyHelper.isInitialized(obj, embMatTypeRI));

		// Test EmbMat3.EmbMatType
		Assert.assertTrue(obj.getEmbMat3() instanceof IEmbeddedType);
	}

	protected PropertyChangeListener getPropertyChangeHandler(final IMap<String, Integer> propertyNameToHitCountMap)
	{
		PropertyChangeListener handler = new PropertyChangeListener()
		{
			@Override
			public void propertyChange(PropertyChangeEvent evt)
			{
				Integer hitCount = propertyNameToHitCountMap.get(evt.getPropertyName());
				if (hitCount == null)
				{
					hitCount = Integer.valueOf(0);
				}
				hitCount++;
				propertyNameToHitCountMap.put(evt.getPropertyName(), hitCount);
			}
		};
		return handler;
	}

	protected PropertyChangeListener getPropertyChangeHandlerForUI(final IMap<String, IMap<Thread, Integer>> propertyNameToHitCountMap)
	{
		PropertyChangeListener handler = new PropertyChangeListener()
		{
			@Override
			public void propertyChange(PropertyChangeEvent evt)
			{
				Thread currentThread = Thread.currentThread();
				// Assert.AreSame(syncContext, SynchronizationContext.Current);
				synchronized (propertyNameToHitCountMap)
				{
					IMap<Thread, Integer> threadMap = propertyNameToHitCountMap.get(evt.getPropertyName());
					if (threadMap == null)
					{
						threadMap = new HashMap<Thread, Integer>();
						propertyNameToHitCountMap.put(evt.getPropertyName(), threadMap);
					}
					Integer hitCount = threadMap.get(currentThread);
					if (hitCount == null)
					{
						hitCount = Integer.valueOf(0);
					}
					hitCount++;
					threadMap.put(currentThread, hitCount);
				}
			}
		};
		return handler;
	}

	@Test
	public void test_DataObject_PropertyChange()
	{
		MaterialType obj = entityFactory.createEntity(MaterialType.class);

		Assert.assertTrue(obj instanceof IDataObject);
		Assert.assertTrue(obj instanceof INotifyPropertyChanged);
		Assert.assertTrue(obj instanceof INotifyPropertyChangedSource);

		HashMap<String, Integer> propertyNameToHitCountMap = new HashMap<String, Integer>();
		PropertyChangeListener handler = getPropertyChangeHandler(propertyNameToHitCountMap);
		((INotifyPropertyChanged) obj).addPropertyChangeListener(handler);

		Assert.assertEquals(0, propertyNameToHitCountMap.size());
		obj.setId(1);
		waitForUI();

		Assert.assertEquals(4, propertyNameToHitCountMap.size());
		Assert.assertEquals(Integer.valueOf(1), propertyNameToHitCountMap.get("Id"));
		Assert.assertEquals(Integer.valueOf(1), propertyNameToHitCountMap.get("ToBeCreated"));
		Assert.assertEquals(Integer.valueOf(1), propertyNameToHitCountMap.get("ToBeUpdated"));
		Assert.assertEquals(Integer.valueOf(2), propertyNameToHitCountMap.get("HasPendingChanges"));
		((IDataObject) obj).setToBeUpdated(false);
		propertyNameToHitCountMap.clear();

		obj.setName("name2");
		waitForUI();

		Assert.assertEquals(5, propertyNameToHitCountMap.size());
		Assert.assertEquals(Integer.valueOf(1), propertyNameToHitCountMap.get("Name"));
		Assert.assertEquals(Integer.valueOf(1), propertyNameToHitCountMap.get("Temp1"));
		Assert.assertEquals(Integer.valueOf(1), propertyNameToHitCountMap.get("Temp2"));
		Assert.assertEquals(Integer.valueOf(1), propertyNameToHitCountMap.get("ToBeUpdated"));
		Assert.assertEquals(Integer.valueOf(1), propertyNameToHitCountMap.get("HasPendingChanges"));
		propertyNameToHitCountMap.clear();
	}

	@Test
	public void test_EntityEquals() throws Exception
	{
		MaterialType objNull1 = entityFactory.createEntity(MaterialType.class);
		Assert.assertTrue(objNull1 instanceof IEntityEquals);

		objNull1.setId(0);
		objNull1.setName("name2");
		objNull1.setVersion(1);

		Assert.assertEquals(objNull1, objNull1);

		MaterialType objNull2 = entityFactory.createEntity(MaterialType.class);
		objNull2.setId(0);
		objNull2.setName("name3");
		objNull2.setVersion(1);

		Assert.assertTrue(!objNull1.equals(objNull2));

		MaterialType obj1 = entityFactory.createEntity(MaterialType.class);
		obj1.setId(1);
		obj1.setName("name2");
		obj1.setVersion(1);

		Assert.assertEquals(obj1, obj1);

		MaterialType obj1_1 = entityFactory.createEntity(MaterialType.class);
		obj1_1.setId(1);
		obj1_1.setName("name3");
		obj1_1.setVersion(2);

		Assert.assertEquals(obj1, obj1_1);
		Assert.assertEquals(obj1.hashCode(), obj1_1.hashCode());

		MaterialType obj2 = entityFactory.createEntity(MaterialType.class);
		obj2.setId(2);
		obj2.setName("name2");
		obj2.setVersion(1);

		Object value1 = obj1.getClass().getMethod(getIdName).invoke(obj1, new Object[0]);
		Object value2 = obj2.getClass().getMethod(getIdName).invoke(obj2, new Object[0]);
		Assert.assertTrue(!obj1.equals(obj2));

		Material mat2 = entityFactory.createEntity(Material.class);
		mat2.setId(2);
		mat2.setName("name2");
		mat2.setVersion(1);

		Assert.assertTrue(!mat2.equals(obj2));
	}

	@Test
	public void test_PropertyChange_Registration()
	{
		HashMap<String, Integer> pceCounter = new HashMap<String, Integer>();
		PropertyChangeListener handler = getPropertyChangeHandler(pceCounter);

		{
			MaterialType obj = entityFactory.createEntity(MaterialType.class);

			Assert.assertTrue(obj instanceof INotifyPropertyChanged);
			Assert.assertTrue(obj instanceof INotifyPropertyChangedSource);

			((INotifyPropertyChanged) obj).addPropertyChangeListener(handler);

			obj.setName("name2");
			obj.setVersion(1);
			waitForUI();

			Assert.assertEquals(4, pceCounter.size());
			Assert.assertEquals(Integer.valueOf(1), pceCounter.get("Name"));
			Assert.assertEquals(Integer.valueOf(1), pceCounter.get("Temp1"));
			Assert.assertEquals(Integer.valueOf(1), pceCounter.get("Temp2"));
			Assert.assertEquals(Integer.valueOf(1), pceCounter.get("Version"));
		}
		{
			Material obj = entityFactory.createEntity(Material.class);

			Assert.assertTrue(obj instanceof INotifyPropertyChanged);
			Assert.assertTrue(obj instanceof INotifyPropertyChangedSource);
			Assert.assertTrue(obj instanceof IDataObject);

			obj.setId(1);
			((IDataObject) obj).setToBeUpdated(false);

			((INotifyPropertyChanged) obj).addPropertyChangeListener(handler);

			pceCounter.clear();
			Assert.assertTrue(obj.getNames() instanceof ObservableArrayList);
			obj.getNames().add("Item1");
			obj.getNames().add("Item2");
			waitForUI();

			Assert.assertEquals(2, pceCounter.size());
			Assert.assertEquals(Integer.valueOf(1), pceCounter.get("ToBeUpdated"));
			Assert.assertEquals(Integer.valueOf(1), pceCounter.get("HasPendingChanges"));
		}
	}

	@Test
	public void test_PropertyChange_ToBeUpdated()
	{
		HashMap<String, Integer> pceCounter = new HashMap<String, Integer>();
		PropertyChangeListener handler = getPropertyChangeHandler(pceCounter);

		Material obj = entityFactory.createEntity(Material.class);
		((INotifyPropertyChanged) obj).addPropertyChangeListener(handler);

		obj.setId(1);
		waitForUI();

		Assert.assertEquals(Integer.valueOf(1), pceCounter.get("Id"));
		Assert.assertEquals(Integer.valueOf(1), pceCounter.get("ToBeCreated"));
		Assert.assertEquals(Integer.valueOf(1), pceCounter.get("ToBeUpdated"));
		Assert.assertEquals(Integer.valueOf(2), pceCounter.get("HasPendingChanges"));
		Assert.assertEquals(4, pceCounter.size());

		pceCounter.clear();

		obj.setId(0);
		waitForUI();

		Assert.assertEquals(Integer.valueOf(1), pceCounter.get("Id"));
		Assert.assertEquals(Integer.valueOf(1), pceCounter.get("ToBeCreated"));
		Assert.assertEquals(Integer.valueOf(1), pceCounter.get("ToBeUpdated"));
		Assert.assertEquals(Integer.valueOf(2), pceCounter.get("HasPendingChanges"));
		Assert.assertEquals(4, pceCounter.size());
	}

	@Test
	public void test_PropertyChange_On_CollectionChange()
	{
		HashMap<String, Integer> pceCounter = new HashMap<String, Integer>();
		PropertyChangeListener handler = getPropertyChangeHandler(pceCounter);

		Material obj = entityFactory.createEntity(Material.class);

		obj.setId(1);
		((IDataObject) obj).setToBeUpdated(false);

		((INotifyPropertyChanged) obj).addPropertyChangeListener(handler);

		obj.getNames().add("Item1");
		obj.getNames().add("Item2");
		waitForUI();

		Assert.assertEquals(2, pceCounter.size());
		Assert.assertEquals(Integer.valueOf(1), pceCounter.get("ToBeUpdated"));
		Assert.assertEquals(Integer.valueOf(1), pceCounter.get("HasPendingChanges"));
	}

	@Test
	public void test_PropertyChange_On_CollectionChange2()
	{
		HashMap<String, Integer> pceCounter = new HashMap<String, Integer>();
		PropertyChangeListener handler = getPropertyChangeHandler(pceCounter);

		Material obj = entityFactory.createEntity(Material.class);

		obj.setId(1);
		((IDataObject) obj).setToBeUpdated(false);

		((INotifyPropertyChanged) obj).addPropertyChangeListener(handler);

		obj.getEmbMat().getEmbMat2().getNames2().add("Item1");
		obj.getEmbMat().getEmbMat2().getNames2().add("Item2");
		waitForUI();

		Assert.assertEquals(2, pceCounter.size());
		Assert.assertEquals(Integer.valueOf(1), pceCounter.get("ToBeUpdated"));
		Assert.assertEquals(Integer.valueOf(1), pceCounter.get("HasPendingChanges"));
	}

	@Test
	public void test_PropertyChange_Registration_Embedded()
	{
		Material obj = entityFactory.createEntity(Material.class);

		Assert.assertTrue(obj instanceof INotifyPropertyChanged);
		Assert.assertTrue(obj instanceof INotifyPropertyChangedSource);
		Assert.assertTrue(obj.getEmbMat() instanceof INotifyPropertyChanged);
		Assert.assertTrue(obj.getEmbMat() instanceof INotifyPropertyChangedSource);
		Assert.assertTrue(obj.getEmbMat().getEmbMat2() instanceof INotifyPropertyChanged);
		Assert.assertTrue(obj.getEmbMat().getEmbMat2() instanceof INotifyPropertyChangedSource);
		Assert.assertTrue(obj.getEmbMat3() instanceof INotifyPropertyChanged);
		Assert.assertTrue(obj.getEmbMat3() instanceof INotifyPropertyChangedSource);

		HashMap<String, Integer> embMat_embMat2_counter = new HashMap<String, Integer>();
		PropertyChangeListener embMat_embMat2_handler = getPropertyChangeHandler(embMat_embMat2_counter);

		HashMap<String, Integer> embMat_counter = new HashMap<String, Integer>();
		PropertyChangeListener embMat_handler = getPropertyChangeHandler(embMat_counter);

		HashMap<String, Integer> counter = new HashMap<String, Integer>();
		PropertyChangeListener handler = getPropertyChangeHandler(counter);

		((INotifyPropertyChanged) obj).addPropertyChangeListener(handler);
		((INotifyPropertyChanged) obj.getEmbMat()).addPropertyChangeListener(embMat_handler);
		((INotifyPropertyChanged) obj.getEmbMat().getEmbMat2()).addPropertyChangeListener(embMat_embMat2_handler);

		obj.setId(1);
		obj.getEmbMat().getEmbMat2().setName2("name2");
		waitForUI();

		Assert.assertEquals(4, counter.size());
		Assert.assertEquals(Integer.valueOf(1), counter.get("Id"));
		Assert.assertEquals(Integer.valueOf(1), counter.get("ToBeCreated"));
		Assert.assertEquals(Integer.valueOf(1), counter.get("ToBeUpdated"));
		Assert.assertEquals(Integer.valueOf(2), counter.get("HasPendingChanges"));
		Assert.assertEquals(0, embMat_counter.size());
		Assert.assertEquals(1, embMat_embMat2_counter.size());
		Assert.assertEquals(Integer.valueOf(1), embMat_embMat2_counter.get("Name2"));
	}

	@Test
	public void test_PropertyChange_Annotations()
	{
		MaterialType obj = entityFactory.createEntity(MaterialType.class);

		Assert.assertTrue(obj instanceof INotifyPropertyChanged);
		Assert.assertTrue(obj instanceof INotifyPropertyChangedSource);

		HashMap<String, Integer> propertyNameToHitCountMap = new HashMap<String, Integer>();
		PropertyChangeListener handler = getPropertyChangeHandler(propertyNameToHitCountMap);

		((INotifyPropertyChanged) obj).addPropertyChangeListener(handler);

		obj.setName("name2");
		waitForUI();

		Assert.assertEquals(3, propertyNameToHitCountMap.size());
		Assert.assertEquals(Integer.valueOf(1), propertyNameToHitCountMap.get("Name"));
		Assert.assertEquals(Integer.valueOf(1), propertyNameToHitCountMap.get("Temp1"));
		Assert.assertEquals(Integer.valueOf(1), propertyNameToHitCountMap.get("Temp2"));

		((INotifyPropertyChanged) obj).addPropertyChangeListener(handler);
	}

	@Test
	public void test_ValueHolderContainer()
	{
		MaterialType obj = entityFactory.createEntity(MaterialType.class);

		obj.setId(2);
		obj.setName("name2");
		obj.setVersion(1);
		MaterialType obj2 = entityFactory.createEntity(MaterialType.class);
		obj2.setId(3);
		obj2.setName("name3");
		obj2.setVersion(1);

		IEntityMetaData metaData = entityMetaDataProvider.getMetaData(Material.class);
		IRelationInfoItem member = (IRelationInfoItem) metaData.getMemberByName("Types");

		Material parentEntity = entityFactory.createEntity(Material.class);
		Assert.assertTrue(parentEntity instanceof IValueHolderContainer);
		Assert.assertEquals(ValueHolderState.LAZY, ((IValueHolderContainer) parentEntity).get__State(member));
		Assert.assertEquals(0, ((IValueHolderContainer) parentEntity).get__ObjRefs(member).length);

		parentEntity.setId(1);
		parentEntity.setName("name1");
		parentEntity.setVersion(1);
		parentEntity.getTypes().add(obj);
		parentEntity.getTypes().add(obj2);

		IObjRef typeObjRef = oriHelper.entityToObjRef(obj);

		IDisposableCache cache = cacheFactory.create(CacheFactoryDirective.NoDCE);
		((IValueHolderContainer) parentEntity).set__TargetCache((ICacheIntern) cache);
		proxyHelper.setObjRefs(parentEntity, member, new IObjRef[] { typeObjRef });

		Assert.assertEquals(ValueHolderState.INIT, ((IValueHolderContainer) parentEntity).get__State(member));
		Assert.assertEquals(1, ((IValueHolderContainer) parentEntity).get__ObjRefs(member).length);

		Object value = valueHolderContainerTemplate.getValue(parentEntity, member);
	}

	@Test
	public void test_PropertyChange_ParentChild_ToOne()
	{
		MaterialType obj2 = entityFactory.createEntity(MaterialType.class);
		Material mat = entityFactory.createEntity(Material.class);
		cacheModification.setActive(true);
		try
		{
			obj2.setId(2);
			obj2.setName("name2");
			obj2.setVersion(1);

			mat.setId(1);
			mat.setName("name1");
			mat.setVersion(1);
			mat.setChildMatType(obj2);
		}
		finally
		{
			cacheModification.setActive(false);
		}

		HashMap<String, Integer> matCounter = new HashMap<String, Integer>();
		PropertyChangeListener matHandler = getPropertyChangeHandler(matCounter);

		HashMap<String, Integer> matTypeCounter = new HashMap<String, Integer>();
		PropertyChangeListener matTypeHandler = getPropertyChangeHandler(matTypeCounter);

		((INotifyPropertyChanged) mat).addPropertyChangeListener(matHandler);
		((INotifyPropertyChanged) mat.getChildMatType()).addPropertyChangeListener(matTypeHandler);

		mat.getChildMatType().setName(mat.getChildMatType().getName() + "_change");
		waitForUI();

		Assert.assertEquals(2, matCounter.size());
		Assert.assertTrue(matCounter.containsKey("ToBeUpdated"));
		Assert.assertTrue(matCounter.containsKey("HasPendingChanges"));
		Assert.assertEquals(1, matCounter.get("ToBeUpdated").intValue());
		Assert.assertEquals(1, matCounter.get("HasPendingChanges").intValue());

		Assert.assertEquals(5, matTypeCounter.size());
		Assert.assertTrue(matTypeCounter.containsKey("Name"));
		Assert.assertTrue(matTypeCounter.containsKey("Temp1"));
		Assert.assertTrue(matTypeCounter.containsKey("Temp2"));
		Assert.assertTrue(matTypeCounter.containsKey("ToBeUpdated"));
		Assert.assertTrue(matTypeCounter.containsKey("HasPendingChanges"));
		Assert.assertEquals(1, matTypeCounter.get("Name").intValue());
		Assert.assertEquals(1, matTypeCounter.get("Temp1").intValue());
		Assert.assertEquals(1, matTypeCounter.get("Temp2").intValue());
		Assert.assertEquals(1, matTypeCounter.get("ToBeUpdated").intValue());
		Assert.assertEquals(1, matTypeCounter.get("HasPendingChanges").intValue());
	}

	@Test
	public void test_PropertyChange_ParentChild_ToMany()
	{
		MaterialType obj3 = entityFactory.createEntity(MaterialType.class);
		MaterialType obj4 = entityFactory.createEntity(MaterialType.class);
		Material mat = entityFactory.createEntity(Material.class);
		cacheModification.setActive(true);
		try
		{
			obj3.setId(3);
			obj3.setName("name3");
			obj3.setVersion(1);

			obj4.setId(4);
			obj4.setName("name4");
			obj4.setVersion(1);

			mat.setId(1);
			mat.setName("name1");
			mat.setVersion(1);
			mat.getChildMatTypes().add(obj3);
			mat.getChildMatTypes().add(obj4);
		}
		finally
		{
			cacheModification.setActive(false);
		}

		HashMap<String, Integer> matCounter = new HashMap<String, Integer>();
		PropertyChangeListener matHandler = getPropertyChangeHandler(matCounter);

		((INotifyPropertyChanged) mat).addPropertyChangeListener(matHandler);

		for (MaterialType childMatType : mat.getChildMatTypes())
		{
			childMatType.setName(childMatType.getName() + "_change");
		}
		waitForUI();

		Assert.assertEquals(2, matCounter.size());
		Assert.assertTrue(matCounter.containsKey("ToBeUpdated"));
		Assert.assertTrue(matCounter.containsKey("HasPendingChanges"));
		Assert.assertEquals(1, matCounter.get("ToBeUpdated").intValue());
		Assert.assertEquals(1, matCounter.get("HasPendingChanges").intValue());
	}

	@Test
	public void test_PropertyChange_OutOfGuiThread() throws InterruptedException
	{
		final HashMap<String, IMap<Thread, Integer>> counter = new HashMap<String, IMap<Thread, Integer>>();
		final PropertyChangeListener handler = getPropertyChangeHandlerForUI(counter);

		final CountDownLatch latch = new CountDownLatch(1);
		guiThreadHelper.invokeOutOfGui(new IBackgroundWorkerDelegate()
		{

			@Override
			public void invoke() throws Throwable
			{
				try
				{
					Material obj = entityFactory.createEntity(Material.class);
					((INotifyPropertyChanged) obj).addPropertyChangeListener(handler);

					cacheModification.setActive(true);
					try
					{
						obj.setId(1);
						Assert.assertEquals(0, counter.size());
					}
					finally
					{
						cacheModification.setActive(false);
					}
					waitForUI();
					Assert.assertEquals(3, counter.size());
					obj.setName("hallo");
					waitForUI();
					Assert.assertEquals(5, counter.size());
				}
				finally
				{
					latch.countDown();
				}
			}
		});
		latch.await();
		Assert.assertEquals(5, counter.size());
		Assert.assertTrue(counter.containsKey("ToBeCreated"));
		Assert.assertTrue(counter.containsKey("ToBeUpdated"));
		Assert.assertTrue(counter.containsKey("Id"));
		Assert.assertTrue(counter.containsKey("Name"));
		Assert.assertTrue(counter.containsKey("HasPendingChanges"));

		Thread guiThread = Thread.currentThread();

		for (Entry<String, IMap<Thread, Integer>> entry : counter)
		{
			for (Entry<Thread, Integer> entry2 : entry.getValue())
			{
				Assert.assertSame(guiThread, entry2.getKey());
			}
		}
	}
	// [TestMethod]
	// public void test_ValueHolderContainer2()
	// {
	// InitManually();
	// IServiceContext oldContext = ValueHolderFlattenHierarchyProxy.Context;
	// ValueHolderFlattenHierarchyProxy.Context = this.BeanContext;
	// try
	// {
	// Material parentEntity = EntityFactory.CreateEntity<Material>();
	// //parentEntity.getTypes().add(null);

	// int a = 5;
	// }
	// finally
	// {
	// ValueHolderFlattenHierarchyProxy.Context = oldContext;
	// }
	// }
}