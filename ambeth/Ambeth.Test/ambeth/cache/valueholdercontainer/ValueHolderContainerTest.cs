using System;
using System.Collections.ObjectModel;
using System.ComponentModel;
using System.Threading;
using De.Osthus.Ambeth.Cache.Config;
using De.Osthus.Ambeth.Collections;
using De.Osthus.Ambeth.Config;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Merge;
using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Merge.Transfer;
using De.Osthus.Ambeth.Model;
using De.Osthus.Ambeth.Proxy;
using De.Osthus.Ambeth.Template;
using De.Osthus.Ambeth.Testutil;
using De.Osthus.Ambeth.Threading;
using De.Osthus.Ambeth.Typeinfo;
using De.Osthus.Ambeth.Util;
using Microsoft.VisualStudio.TestTools.UnitTesting;

namespace De.Osthus.Ambeth.Cache.Valueholdercontainer
{
    [TestProperties(Name = CacheConfigurationConstants.CacheServiceRegistryActive, Value = "true")]
    [TestProperties(Name = CacheConfigurationConstants.AsyncPropertyChangeActive, Value = "true")]
    [TestProperties(Name = ServiceConfigurationConstants.MappingFile, Value = "ambeth/cache/valueholdercontainer/orm.xml")]
    [TestFrameworkModule(typeof(ValueHolderContainerTestModule))]
    [TestRebuildContext]
    [TestClass]
    public class ValueHolderContainerTest : AbstractIndependentClientTest
    {
        public static readonly String getIdName = "Get__Id";

        [LogInstance]
        public ILogger Log { private get; set; }

        public ICacheFactory CacheFactory { protected get; set; }

        public ICacheModification CacheModification { protected get; set; }

        public IEntityFactory EntityFactory { protected get; set; }

        public IEntityMetaDataProvider EntityMetaDataProvider { protected get; set; }

        public IGuiThreadHelper GuiThreadHelper { protected get; set; }

        public IObjRefHelper OriHelper { protected get; set; }

        public IProxyHelper ProxyHelper { protected get; set; }

        public IThreadPool ThreadPool { protected get; set; }

        public ValueHolderContainerTemplate ValueHolderContainerTemplate { protected get; set; }

        //static int count = 10000000;

        ////[TestInitialize]
        //public void InitManually()
        //{
        //    base.InitManually(GetType());
        //}

        //static Object[] objects;

        //static ValueHolderContainerTest()
        //{
        //     objects = new Object[count];
        //        for (int a = count; a-- > 0; )
        //        {
        //            objects[a] = new Object();
        //        }
        //}

        //[TestMethod]
        //public void test_Dictionary()
        //{
        //    Dictionary<Object, Object> dict = new Dictionary<Object, Object>(count, new IdentityEqualityComparer<Object>());

        //    //for (int a = count; a-- > 0; )
        //    //{
        //    //    dict.Add(a, objects[a]);
        //    //    dict.Remove(a);
        //    //    dict.Add(a, objects[a]);
        //    //    dict.Remove(a);
        //    //    dict.Add(a, objects[a]);
        //    //    dict.Remove(a);
        //    //    dict.Add(a, objects[a]);
        //    //    dict.Remove(a);
        //    //    dict.Add(a, objects[a]);
        //    //    dict.Remove(a);
        //    //}
        //    for (int a = count; a-- > 0; )
        //    {
        //        dict.Add(a, objects[a]);
        //    }
        //    for (int a = 10; a-- > 0; )
        //    {
        //        foreach (KeyValuePair<Object, Object> entry in dict)
        //        {
        //            Object key = entry.Key;
        //            Object value = entry.Value;
        //            if (!Object.ReferenceEquals(objects[(int)key], value))
        //            {
        //                throw new Exception();
        //            }
        //        }
        //    }
        //}

        //[TestMethod]
        //public void test_HashMap()
        //{
        //    IdentityLinkedMap<Object, Object> dict = IdentityLinkedMap<Object, Object>.Create(count);

        //    //for (int a = count; a-- > 0; )
        //    //{
        //    //    dict.Put(a, objects[a]);
        //    //    dict.Remove(a);
        //    //    dict.Put(a, objects[a]);
        //    //    dict.Remove(a);
        //    //    dict.Put(a, objects[a]);
        //    //    dict.Remove(a);
        //    //    dict.Put(a, objects[a]);
        //    //    dict.Remove(a);
        //    //    dict.Put(a, objects[a]);
        //    //    dict.Remove(a);
        //    //}
        //    for (int a = count; a-- > 0; )
        //    {
        //        dict.Put(a, objects[a]);
        //    }
        //    for (int a = 10; a-- > 0; )
        //    {
        //        foreach (Entry<Object, Object> entry in dict)
        //        {
        //            Object key = entry.Key;
        //            Object value = entry.Value;
        //            if (!Object.ReferenceEquals(objects[(int)key], value))
        //            {
        //                throw new Exception();
        //            }
        //        }
        //    }
        //}

        //[TestMethod]
        //public void test_PropertyInfo()
        //{
        //    Material mat = new Material();

        //    PropertyInfo pi = mat.GetType().GetProperty("Name");

        //    DateTime prePi = DateTime.Now;
        //    for (int a = count; a-- > 0; )
        //    {
        //        Object piValue = pi.GetValue(mat, null);
        //        piValue = pi.GetValue(mat, null);
        //        piValue = pi.GetValue(mat, null);
        //        piValue = pi.GetValue(mat, null);
        //        piValue = pi.GetValue(mat, null);
        //        piValue = pi.GetValue(mat, null);
        //        piValue = pi.GetValue(mat, null);
        //        piValue = pi.GetValue(mat, null);
        //        piValue = pi.GetValue(mat, null);
        //        piValue = pi.GetValue(mat, null);
        //        piValue = pi.GetValue(mat, null);
        //        piValue = pi.GetValue(mat, null);
        //        piValue = pi.GetValue(mat, null);
        //        piValue = pi.GetValue(mat, null);
        //        piValue = pi.GetValue(mat, null);
        //        piValue = pi.GetValue(mat, null);
        //        piValue = pi.GetValue(mat, null);
        //        piValue = pi.GetValue(mat, null);
        //        piValue = pi.GetValue(mat, null);
        //        piValue = pi.GetValue(mat, null);
        //    }
        //    DateTime postPi = DateTime.Now;
        //    long piTime = postPi.Ticks - prePi.Ticks;
        //    Console.WriteLine(piTime);
        //}

        //[TestMethod]
        //public void test_GetDelegate()
        //{
        //    Material mat = new Material();

        //    MemberGetDelegate del = TypeUtility.GetMemberGetDelegate(mat.GetType(), "Name");

        //    MemberSetDelegate setDel = TypeUtility.GetMemberSetDelegate(mat.GetType(), "Id");

        //    setDel(mat, 5);

        //    DateTime preDel = DateTime.Now;
        //    for (int a = count; a-- > 0; )
        //    {
        //        Object delValue = del(mat);
        //        delValue = del(mat);
        //        delValue = del(mat);
        //        delValue = del(mat);
        //        delValue = del(mat);
        //        delValue = del(mat);
        //        delValue = del(mat);
        //        delValue = del(mat);
        //        delValue = del(mat);
        //        delValue = del(mat);
        //        delValue = del(mat);
        //        delValue = del(mat);
        //        delValue = del(mat);
        //        delValue = del(mat);
        //        delValue = del(mat);
        //        delValue = del(mat);
        //        delValue = del(mat);
        //        delValue = del(mat);
        //        delValue = del(mat);
        //        delValue = del(mat);
        //    }
        //    DateTime postDel = DateTime.Now;

        //    long delTime = postDel.Ticks - preDel.Ticks;
        //    Console.WriteLine(delTime);
        //}

        protected void WaitForUI()
        {
            GuiThreadHelper.InvokeInGuiAndWait(delegate()
            {
                // Intended blank
            });
        }

        [TestMethod]
        public void test_DataObject()
        {
            Material obj = EntityFactory.CreateEntity<Material>();

            Assert.IsInstanceOfType(obj, typeof(IDataObject));

            obj.Id = obj.Id;
            obj.Name = obj.Name;
            obj.Version = obj.Version;

            Object id = ((IEntityEquals)obj).Get__Id();

            Assert.IsTrue(((IDataObject)obj).ToBeCreated);
            Assert.IsFalse(((IDataObject)obj).ToBeUpdated);
            Assert.IsFalse(((IDataObject)obj).ToBeDeleted);
            Assert.IsTrue(((IDataObject)obj).HasPendingChanges);

            obj.Id = 0;
            obj.Name = "name2";
            obj.Version = 1;

            Object idNull = obj.GetType().GetMethod(getIdName).Invoke(obj, new Object[0]);
            Assert.IsNull(idNull);

            Assert.IsTrue(((IDataObject)obj).ToBeCreated);
            Assert.IsFalse(((IDataObject)obj).ToBeUpdated);
            Assert.IsFalse(((IDataObject)obj).ToBeDeleted);
            Assert.IsTrue(((IDataObject)obj).HasPendingChanges);

            obj.Id = 1;

            Object idNotNull = obj.GetType().GetMethod(getIdName).Invoke(obj, new Object[0]);
            Assert.IsNotNull(idNotNull);

            Assert.IsFalse(((IDataObject)obj).ToBeCreated);
            Assert.IsTrue(((IDataObject)obj).ToBeUpdated);
            Assert.IsFalse(((IDataObject)obj).ToBeDeleted);
            Assert.IsTrue(((IDataObject)obj).HasPendingChanges);

            obj = EntityFactory.CreateEntity<Material>();
            bool oldCacheModActive = CacheModification.Active;
            CacheModification.Active = true;
            try
            {
                obj.Id = 1;
            }
            finally
            {
                CacheModification.Active = oldCacheModActive;
            }
            idNotNull = obj.GetType().GetMethod(getIdName).Invoke(obj, new Object[0]);
            Assert.IsNotNull(idNotNull);

            Assert.IsFalse(((IDataObject)obj).ToBeCreated);
            Assert.IsFalse(((IDataObject)obj).ToBeUpdated);
            Assert.IsFalse(((IDataObject)obj).ToBeDeleted);
            Assert.IsFalse(((IDataObject)obj).HasPendingChanges);
        }

        [TestMethod]
        public void test_DataObject_Embedded()
        {
            Material obj = EntityFactory.CreateEntity<Material>();

            Assert.IsInstanceOfType(obj, typeof(IDataObject));
            Assert.IsNotInstanceOfType(obj.EmbMat, typeof(IDataObject));
            Assert.IsNotInstanceOfType(obj.EmbMat.EmbMat2, typeof(IDataObject));
            Assert.IsNotInstanceOfType(obj.EmbMat3, typeof(IDataObject));

            obj.Id = 1;
            ((IDataObject)obj).ToBeUpdated = false;

            obj.EmbMat.Name = "Name";
            Assert.IsTrue(((IDataObject)obj).ToBeUpdated);
            ((IDataObject)obj).ToBeUpdated = false;

            obj.EmbMat.EmbMat2.Name2 = "Name2";
            Assert.IsTrue(((IDataObject)obj).ToBeUpdated);
            ((IDataObject)obj).ToBeUpdated = false;
        }

        [TestMethod]
        public void test_ValueHolderContainer_Embedded()
        {
            Material obj = EntityFactory.CreateEntity<Material>();

            // Test EmbMat.EmbMatType
            Assert.IsInstanceOfType(obj.EmbMat, typeof(IEmbeddedType));
            obj.EmbMat.Name = "Name2";
            Assert.AreEqual("Name2", obj.EmbMat.Name);

            Assert.AreEqual(0, ReflectUtil.GetDeclaredFieldInHierarchy(obj.GetType(), ValueHolderIEC.GetInitializedFieldName("EmbMat.EmbMatType")).Length);
            Assert.AreEqual(1, ReflectUtil.GetDeclaredFieldInHierarchy(obj.EmbMat.GetType(), ValueHolderIEC.GetInitializedFieldName("EmbMatType")).Length);

            IRelationInfoItem embMatTypeRI = (IRelationInfoItem)EntityMetaDataProvider.GetMetaData(obj.GetType()).GetMemberByName("EmbMat.EmbMatType");

            Assert.IsFalse(ProxyHelper.IsInitialized(obj, embMatTypeRI));

            IObjRef[] emptyRefs = ObjRef.EMPTY_ARRAY;
            ProxyHelper.SetObjRefs(obj, embMatTypeRI, emptyRefs);
            IObjRef[] objRefs = ProxyHelper.GetObjRefs(obj, embMatTypeRI);
            Assert.AreSame(emptyRefs, objRefs);

            Assert.IsNull(obj.EmbMat.EmbMatType);
            Assert.IsTrue(ProxyHelper.IsInitialized(obj, embMatTypeRI));

            // Test EmbMat.EmbMat2.EmbMatType2
            Assert.IsInstanceOfType(obj.EmbMat.EmbMat2, typeof(IEmbeddedType));
            obj.EmbMat.EmbMat2.Name2 = "Name3";
            Assert.AreEqual("Name3", obj.EmbMat.EmbMat2.Name2);

            Assert.IsNull(obj.GetType().GetField(ValueHolderIEC.GetInitializedFieldName("EmbMat.EmbMat2.EmbMatType2")));
            Assert.IsNull(obj.EmbMat.GetType().GetField(ValueHolderIEC.GetInitializedFieldName("EmbMat2.EmbMatType2")));
            Assert.IsNotNull(obj.EmbMat.EmbMat2.GetType().GetField(ValueHolderIEC.GetInitializedFieldName("EmbMatType2")));

            embMatTypeRI = (IRelationInfoItem)EntityMetaDataProvider.GetMetaData(obj.GetType()).GetMemberByName("EmbMat.EmbMat2.EmbMatType2");

            Assert.IsFalse(ProxyHelper.IsInitialized(obj, embMatTypeRI));

            ProxyHelper.SetObjRefs(obj, embMatTypeRI, emptyRefs);
            objRefs = ProxyHelper.GetObjRefs(obj, embMatTypeRI);
            Assert.AreSame(emptyRefs, objRefs);

            Assert.IsNull(obj.EmbMat.EmbMat2.EmbMatType2);
            Assert.IsTrue(ProxyHelper.IsInitialized(obj, embMatTypeRI));

            // Test EmbMat3.EmbMatType
            Assert.IsInstanceOfType(obj.EmbMat3, typeof(IEmbeddedType));
        }

        protected PropertyChangedEventHandler GetPropertyChangeHandler(IMap<String, int> propertyNameToHitCountMap)
        {
            PropertyChangedEventHandler handler = new PropertyChangedEventHandler(delegate(Object sender, PropertyChangedEventArgs e)
            {
                int hitCount = propertyNameToHitCountMap.Get(e.PropertyName);
                hitCount++;
                propertyNameToHitCountMap.Put(e.PropertyName, hitCount);
            });
            return handler;
        }

        protected PropertyChangedEventHandler GetPropertyChangeHandler(IMap<String, IMap<Thread, int>> propertyNameToHitCountMap)
        {
            PropertyChangedEventHandler handler = new PropertyChangedEventHandler(delegate(Object sender, PropertyChangedEventArgs e)
            {
                Thread currentThread = Thread.CurrentThread;
                lock (propertyNameToHitCountMap)
                {
                    IMap<Thread, int> threadMap = propertyNameToHitCountMap.Get(e.PropertyName);
                    if (threadMap == null)
                    {
                        threadMap = new HashMap<Thread, int>();
                        propertyNameToHitCountMap.Put(e.PropertyName, threadMap);
                    }
                    int hitCount = threadMap.Get(currentThread);
                    hitCount++;
                    threadMap.Put(currentThread, hitCount);
                }
            });
            return handler;
        }

        protected PropertyChangedEventHandler GetPropertyChangeHandlerForUI(IMap<String, IMap<Thread, int>> propertyNameToHitCountMap)
        {
            PropertyChangedEventHandler handler = new PropertyChangedEventHandler(delegate(Object sender, PropertyChangedEventArgs e)
            {
                Thread currentThread = Thread.CurrentThread;
                //Assert.AreSame(syncContext, SynchronizationContext.Current);
                lock (propertyNameToHitCountMap)
                {
                    IMap<Thread, int> threadMap = propertyNameToHitCountMap.Get(e.PropertyName);
                    if (threadMap == null)
                    {
                        threadMap = new HashMap<Thread, int>();
                        propertyNameToHitCountMap.Put(e.PropertyName, threadMap);
                    }
                    int hitCount = threadMap.Get(currentThread);
                    hitCount++;
                    threadMap.Put(currentThread, hitCount);
                }
            });
            return handler;
        }

        [TestMethod]
        public void test_DataObject_PropertyChange()
        {
            MaterialType obj = EntityFactory.CreateEntity<MaterialType>();

            Assert.IsInstanceOfType(obj, typeof(IDataObject));
            Assert.IsInstanceOfType(obj, typeof(INotifyPropertyChanged));
            Assert.IsInstanceOfType(obj, typeof(INotifyPropertyChangedSource));

            HashMap<String, int> propertyNameToHitCountMap = new HashMap<String, int>();
            PropertyChangedEventHandler handler = GetPropertyChangeHandler(propertyNameToHitCountMap);
            ((INotifyPropertyChanged)obj).PropertyChanged += handler;

            Assert.AreEqual(0, propertyNameToHitCountMap.Count);
            obj.Id = 1;

            WaitForUI();

            Assert.AreEqual(4, propertyNameToHitCountMap.Count);
            Assert.AreEqual(1, propertyNameToHitCountMap.Get("Id"));
            Assert.AreEqual(1, propertyNameToHitCountMap.Get("ToBeCreated"));
            Assert.AreEqual(1, propertyNameToHitCountMap.Get("ToBeUpdated"));
            Assert.AreEqual(2, propertyNameToHitCountMap.Get("HasPendingChanges"));
            ((IDataObject)obj).ToBeUpdated = false;
            propertyNameToHitCountMap.Clear();

            obj.Name = "name2";
            WaitForUI();

            Assert.AreEqual(5, propertyNameToHitCountMap.Count);
            Assert.AreEqual(1, propertyNameToHitCountMap.Get("Name"));
            Assert.AreEqual(1, propertyNameToHitCountMap.Get("Temp1"));
            Assert.AreEqual(1, propertyNameToHitCountMap.Get("Temp2"));
            Assert.AreEqual(2, propertyNameToHitCountMap.Get("ToBeUpdated"));
            Assert.AreEqual(2, propertyNameToHitCountMap.Get("HasPendingChanges"));
            propertyNameToHitCountMap.Clear();
        }

        [TestMethod]
        public void test_EntityEquals()
        {
            MaterialType objNull1 = EntityFactory.CreateEntity<MaterialType>();
            Assert.IsInstanceOfType(objNull1, typeof(IEntityEquals));

            objNull1.Id = 0;
            objNull1.Name = "name2";
            objNull1.Version = 1;

            Assert.AreEqual(objNull1, objNull1);

            MaterialType objNull2 = EntityFactory.CreateEntity<MaterialType>();
            objNull2.Id = 0;
            objNull2.Name = "name3";
            objNull2.Version = 1;

            Assert.AreNotEqual(objNull1, objNull2);

            MaterialType obj1 = EntityFactory.CreateEntity<MaterialType>();
            obj1.Id = 1;
            obj1.Name = "name2";
            obj1.Version = 1;

            Assert.AreEqual(obj1, obj1);

            MaterialType obj1_1 = EntityFactory.CreateEntity<MaterialType>();
            obj1_1.Id = 1;
            obj1_1.Name = "name3";
            obj1_1.Version = 2;

            Assert.AreEqual(obj1, obj1_1);
            Assert.AreEqual(obj1.GetHashCode(), obj1_1.GetHashCode());

            MaterialType obj2 = EntityFactory.CreateEntity<MaterialType>();
            obj2.Id = 2;
            obj2.Name = "name2";
            obj2.Version = 1;

            Object value1 = obj1.GetType().GetMethod(getIdName).Invoke(obj1, new Object[0]);
            Object value2 = obj2.GetType().GetMethod(getIdName).Invoke(obj2, new Object[0]);
            Assert.AreNotEqual(obj1, obj2);

            Material mat2 = EntityFactory.CreateEntity<Material>();
            mat2.Id = 2;
            mat2.Name = "name2";
            mat2.Version = 1;

            Assert.AreNotEqual(mat2, obj2);
        }

        [TestMethod]
        public void test_PropertyChange_Registration()
        {
            HashMap<String, int> pceCounter = new HashMap<String, int>();
            PropertyChangedEventHandler handler = GetPropertyChangeHandler(pceCounter);

            {
                MaterialType obj = EntityFactory.CreateEntity<MaterialType>();

                Assert.IsInstanceOfType(obj, typeof(INotifyPropertyChanged));
                Assert.IsInstanceOfType(obj, typeof(INotifyPropertyChangedSource));

                ((INotifyPropertyChanged)obj).PropertyChanged += handler;

                obj.Name = "name2";
                obj.Version = 1;
                WaitForUI();

                Assert.AreEqual(4, pceCounter.Count);
                Assert.AreEqual(1, pceCounter.Get("Name"));
                Assert.AreEqual(1, pceCounter.Get("Temp1"));
                Assert.AreEqual(1, pceCounter.Get("Temp2"));
                Assert.AreEqual(1, pceCounter.Get("Version"));
            }
            {
                Material obj = EntityFactory.CreateEntity<Material>();

                Assert.IsInstanceOfType(obj, typeof(INotifyPropertyChanged));
                Assert.IsInstanceOfType(obj, typeof(INotifyPropertyChangedSource));
                Assert.IsInstanceOfType(obj, typeof(IDataObject));

                obj.Id = 1;
                ((IDataObject)obj).ToBeUpdated = false;

                ((INotifyPropertyChanged)obj).PropertyChanged += handler;

                pceCounter.Clear();
                Assert.IsInstanceOfType(obj.Names, typeof(ObservableCollection<String>));
                obj.Names.Add("Item1");
                obj.Names.Add("Item2");
                WaitForUI();

                Assert.AreEqual(2, pceCounter.Count);
                Assert.AreEqual(1, pceCounter.Get("ToBeUpdated"));
                Assert.AreEqual(1, pceCounter.Get("HasPendingChanges"));
            }
        }

        [TestMethod]
        public void test_PropertyChange_ToBeUpdated()
        {
            HashMap<String, int> pceCounter = new HashMap<String, int>();
            PropertyChangedEventHandler handler = GetPropertyChangeHandler(pceCounter);

            Material obj = EntityFactory.CreateEntity<Material>();
            ((INotifyPropertyChanged)obj).PropertyChanged += handler;

            obj.Id = 1;
            WaitForUI();

            Assert.AreEqual(1, pceCounter.Get("Id"));
            Assert.AreEqual(1, pceCounter.Get("ToBeCreated"));
            Assert.AreEqual(1, pceCounter.Get("ToBeUpdated"));
            Assert.AreEqual(2, pceCounter.Get("HasPendingChanges"));
            Assert.AreEqual(4, pceCounter.Count);

            pceCounter.Clear();

            obj.Id = null;
            WaitForUI();

            Assert.AreEqual(1, pceCounter.Get("Id"));
            Assert.AreEqual(1, pceCounter.Get("ToBeCreated"));
            Assert.AreEqual(1, pceCounter.Get("ToBeUpdated"));
            Assert.AreEqual(2, pceCounter.Get("HasPendingChanges"));
            Assert.AreEqual(4, pceCounter.Count);
        }

        [TestMethod]
        public void test_PropertyChange_On_CollectionChange()
        {
            HashMap<String, int> pceCounter = new HashMap<String, int>();
            PropertyChangedEventHandler handler = GetPropertyChangeHandler(pceCounter);

            Material obj = EntityFactory.CreateEntity<Material>();

            obj.Id = 1;

            ((IDataObject)obj).ToBeUpdated = false;

            ((INotifyPropertyChanged)obj).PropertyChanged += handler;

            obj.Names.Add("Item1");
            obj.Names.Add("Item2");
            WaitForUI();

            Assert.AreEqual(2, pceCounter.Count);
            Assert.AreEqual(1, pceCounter.Get("ToBeUpdated"));
            Assert.AreEqual(1, pceCounter.Get("HasPendingChanges"));
        }

        [TestMethod]
        public void test_PropertyChange_On_CollectionChange2()
        {
            HashMap<String, int> pceCounter = new HashMap<String, int>();
            PropertyChangedEventHandler handler = GetPropertyChangeHandler(pceCounter);

            Material obj = EntityFactory.CreateEntity<Material>();

            obj.Id = 1;
            ((IDataObject)obj).ToBeUpdated = false;

            ((INotifyPropertyChanged)obj).PropertyChanged += handler;

            obj.EmbMat.EmbMat2.Names2.Add("Item1");
            obj.EmbMat.EmbMat2.Names2.Add("Item2");
            WaitForUI();

            Assert.AreEqual(2, pceCounter.Count);
            Assert.AreEqual(1, pceCounter.Get("ToBeUpdated"));
            Assert.AreEqual(1, pceCounter.Get("HasPendingChanges"));
        }

        [TestMethod]
        public void test_PropertyChange_Registration_Embedded()
        {
            Material obj = EntityFactory.CreateEntity<Material>();

            Assert.IsInstanceOfType(obj, typeof(INotifyPropertyChanged));
            Assert.IsInstanceOfType(obj, typeof(INotifyPropertyChangedSource));
            Assert.IsInstanceOfType(obj.EmbMat, typeof(INotifyPropertyChanged));
            Assert.IsInstanceOfType(obj.EmbMat, typeof(INotifyPropertyChangedSource));
            Assert.IsInstanceOfType(obj.EmbMat.EmbMat2, typeof(INotifyPropertyChanged));
            Assert.IsInstanceOfType(obj.EmbMat.EmbMat2, typeof(INotifyPropertyChangedSource));
            Assert.IsInstanceOfType(obj.EmbMat3, typeof(INotifyPropertyChanged));
            Assert.IsInstanceOfType(obj.EmbMat3, typeof(INotifyPropertyChangedSource));

            HashMap<String, int> embMat_embMat2_counter = new HashMap<String, int>();
            PropertyChangedEventHandler embMat_embMat2_handler = GetPropertyChangeHandler(embMat_embMat2_counter);

            HashMap<String, int> embMat_counter = new HashMap<String, int>();
            PropertyChangedEventHandler embMat_handler = GetPropertyChangeHandler(embMat_counter);

            HashMap<String, int> counter = new HashMap<String, int>();
            PropertyChangedEventHandler handler = GetPropertyChangeHandler(counter);

            ((INotifyPropertyChanged)obj).PropertyChanged += handler;
            ((INotifyPropertyChanged)obj.EmbMat).PropertyChanged += embMat_handler;
            ((INotifyPropertyChanged)obj.EmbMat.EmbMat2).PropertyChanged += embMat_embMat2_handler;

            obj.Id = 1;
            obj.EmbMat.EmbMat2.Name2 = "name2";
            WaitForUI();

            Assert.AreEqual(4, counter.Count);
            Assert.AreEqual(1, counter.Get("Id"));
            Assert.AreEqual(1, counter.Get("ToBeCreated"));
            Assert.AreEqual(1, counter.Get("ToBeUpdated"));
            Assert.AreEqual(2, counter.Get("HasPendingChanges"));
            Assert.AreEqual(0, embMat_counter.Count);
            Assert.AreEqual(1, embMat_embMat2_counter.Count);
            Assert.AreEqual(1, embMat_embMat2_counter.Get("Name2"));
        }

        [TestMethod]
        public void test_PropertyChange_Annotations()
        {
            MaterialType obj = EntityFactory.CreateEntity<MaterialType>();

            Assert.IsInstanceOfType(obj, typeof(INotifyPropertyChanged));
            Assert.IsInstanceOfType(obj, typeof(INotifyPropertyChangedSource));

            HashMap<String, int> propertyNameToHitCountMap = new HashMap<String, int>();
            PropertyChangedEventHandler handler = GetPropertyChangeHandler(propertyNameToHitCountMap);

            ((INotifyPropertyChanged)obj).PropertyChanged += handler;

            obj.Name = "name2";
            WaitForUI();

            Assert.AreEqual(3, propertyNameToHitCountMap.Count);
            Assert.AreEqual(1, propertyNameToHitCountMap.Get("Name"));
            Assert.AreEqual(1, propertyNameToHitCountMap.Get("Temp1"));
            Assert.AreEqual(1, propertyNameToHitCountMap.Get("Temp2"));

            ((INotifyPropertyChanged)obj).PropertyChanged -= handler;
        }

        [TestMethod]
        public void test_ValueHolderContainer()
        {
            MaterialType obj = EntityFactory.CreateEntity<MaterialType>();

            obj.Id = 2;
            obj.Name = "name2";
            obj.Version = 1;
            MaterialType obj2 = EntityFactory.CreateEntity<MaterialType>();
            obj2.Id = 3;
            obj2.Name = "name3";
            obj2.Version = 1;

            IEntityMetaData metaData = EntityMetaDataProvider.GetMetaData(typeof(Material));
            IRelationInfoItem member = (IRelationInfoItem)metaData.GetMemberByName("Types");

            Material parentEntity = EntityFactory.CreateEntity<Material>();
            Assert.IsInstanceOfType(parentEntity, typeof(IValueHolderContainer));
            Assert.AreEqual(ValueHolderState.LAZY, ((IValueHolderContainer)parentEntity).GetState(member));
            Assert.AreEqual(0, ((IValueHolderContainer)parentEntity).GetObjRefs(member).Length);

            parentEntity.Id = 1;
            parentEntity.Name = "name1";
            parentEntity.Version = 1;
            parentEntity.Types.Add(obj);
            parentEntity.Types.Add(obj2);

            IObjRef typeObjRef = OriHelper.EntityToObjRef(obj);

            IDisposableCache cache = CacheFactory.Create(CacheFactoryDirective.NoDCE);
            ((IValueHolderContainer)parentEntity).__TargetCache = (ICacheIntern)cache;
            ProxyHelper.SetObjRefs(parentEntity, member, new IObjRef[] { typeObjRef });

            Assert.AreEqual(ValueHolderState.INIT, ((IValueHolderContainer)parentEntity).GetState(member));
            Assert.AreEqual(1, ((IValueHolderContainer)parentEntity).GetObjRefs(member).Length);

            Object value = ValueHolderContainerTemplate.GetValue(parentEntity, member);
        }

        [TestMethod]
        public void test_PropertyChange_ParentChild_ToOne()
        {
            MaterialType obj2 = EntityFactory.CreateEntity<MaterialType>();
            Material mat = EntityFactory.CreateEntity<Material>();
            CacheModification.Active = true;
            try
            {
                obj2.Id = 2;
                obj2.Name = "name2";
                obj2.Version = 1;

                mat.Id = 1;
                mat.Name = "name1";
                mat.Version = 1;
                mat.ChildMatType = obj2;
            }
            finally
            {
                CacheModification.Active = false;
            }

            HashMap<String, int> matCounter = new HashMap<String, int>();
            PropertyChangedEventHandler matHandler = GetPropertyChangeHandler(matCounter);

            HashMap<String, int> matTypeCounter = new HashMap<String, int>();
            PropertyChangedEventHandler matTypeHandler = GetPropertyChangeHandler(matTypeCounter);

            ((INotifyPropertyChanged)mat).PropertyChanged += matHandler;
            ((INotifyPropertyChanged)mat.ChildMatType).PropertyChanged += matTypeHandler;

            mat.ChildMatType.Name += "_change";
            WaitForUI();

            Assert.AreEqual(2, matCounter.Count);
            Assert.IsTrue(matCounter.ContainsKey("ToBeUpdated"));
            Assert.IsTrue(matCounter.ContainsKey("HasPendingChanges"));
            Assert.AreEqual(1, matCounter.Get("ToBeUpdated"));
            Assert.AreEqual(1, matCounter.Get("HasPendingChanges"));

            Assert.AreEqual(5, matTypeCounter.Count);
            Assert.IsTrue(matTypeCounter.ContainsKey("Name"));
            Assert.IsTrue(matTypeCounter.ContainsKey("Temp1"));
            Assert.IsTrue(matTypeCounter.ContainsKey("Temp2"));
            Assert.IsTrue(matTypeCounter.ContainsKey("ToBeUpdated"));
            Assert.IsTrue(matTypeCounter.ContainsKey("HasPendingChanges"));
            Assert.AreEqual(1, matTypeCounter.Get("Name"));
            Assert.AreEqual(1, matTypeCounter.Get("Temp1"));
            Assert.AreEqual(1, matTypeCounter.Get("Temp2"));
            Assert.AreEqual(1, matTypeCounter.Get("ToBeUpdated"));
            Assert.AreEqual(1, matTypeCounter.Get("HasPendingChanges"));
        }

        [TestMethod]
        public void test_PropertyChange_ParentChild_ToMany()
        {
            MaterialType obj3 = EntityFactory.CreateEntity<MaterialType>();
            MaterialType obj4 = EntityFactory.CreateEntity<MaterialType>();
            Material mat = EntityFactory.CreateEntity<Material>();
            CacheModification.Active = true;
            try
            {
                obj3.Id = 3;
                obj3.Name = "name3";
                obj3.Version = 1;

                obj4.Id = 4;
                obj4.Name = "name4";
                obj4.Version = 1;

                mat.Id = 1;
                mat.Name = "name1";
                mat.Version = 1;
                mat.ChildMatTypes.Add(obj3);
                mat.ChildMatTypes.Add(obj4);
            }
            finally
            {
                CacheModification.Active = false;
            }

            HashMap<String, int> matCounter = new HashMap<String, int>();
            PropertyChangedEventHandler matHandler = GetPropertyChangeHandler(matCounter);

            ((INotifyPropertyChanged)mat).PropertyChanged += matHandler;

            foreach (MaterialType childMatType in mat.ChildMatTypes)
            {
                childMatType.Name += "_change";
            }
            WaitForUI();

            Assert.AreEqual(2, matCounter.Count);
            Assert.IsTrue(matCounter.ContainsKey("ToBeUpdated"));
            Assert.IsTrue(matCounter.ContainsKey("HasPendingChanges"));
            Assert.AreEqual(1, matCounter.Get("ToBeUpdated"));
            Assert.AreEqual(1, matCounter.Get("HasPendingChanges"));
        }

        [TestMethod]
        public void test_PropertyChange_OutOfGuiThread()
        {
            HashMap<String, IMap<Thread, int>> counter = new HashMap<String, IMap<Thread, int>>();
            PropertyChangedEventHandler handler = GetPropertyChangeHandlerForUI(counter);

            Thread workerThread = null;
            CountDownLatch latch = new CountDownLatch(1);

            ThreadPool.Queue(delegate()
            {
                workerThread = Thread.CurrentThread;
                Log.Info("Test()");
                try
                {
                    Material obj = EntityFactory.CreateEntity<Material>();
                    ((INotifyPropertyChanged)obj).PropertyChanged += handler;

                    Log.Info("ICacheModification.set_Active(true)");
                    CacheModification.Active = true;
                    try
                    {
                        Log.Info("set_Id");
                        obj.Id = 1;
                        Log.Info("set_Id finished");
                        Assert.AreEqual(0, counter.Count);
                    }
                    finally
                    {
                        Log.Info("ICacheModification.set_Active(false)");
                        CacheModification.Active = false;
                        Log.Info("ICacheModification.set_Active(false) finished");
                    }
                    WaitForUI();
                    Assert.AreEqual(3, counter.Count);
                    Log.Info(" set_Name");
                    obj.Name = "hallo";
                    WaitForUI();
                    Log.Info("set_Name finished");
                    Assert.AreEqual(5, counter.Count);
                }
                finally
                {
                    latch.CountDown();
                }
            });
            Log.Info("Await()");
            latch.Await();
            // Wait till the current ui queue has been processed completely
            GuiThreadHelper.InvokeInGuiAndWait(delegate()
            {
                // just an empty blocking delegate
            });
            Assert.AreEqual(5, counter.Count);

            Thread guiThread = ValueHolderContainerTestModule.dispatcherThread;

            IMap<Thread, int> toBeCreatedMap = counter.Get("ToBeCreated");
            Assert.IsNotNull(toBeCreatedMap);
            Assert.AreEqual(1, toBeCreatedMap.Count);
            Assert.IsTrue(toBeCreatedMap.ContainsKey(guiThread));
            Assert.AreEqual(1, toBeCreatedMap.Get(guiThread));

            IMap<Thread, int> idMap = counter.Get("Id");
            Assert.IsNotNull(idMap);
            Assert.AreEqual(1, idMap.Count);
            Assert.IsTrue(idMap.ContainsKey(guiThread));
            Assert.AreEqual(1, idMap.Get(guiThread));

            // uiThread is intended for Name in the case where asynchronous PCEs are allowed
            // but dispatched transparently in the UI
            IMap<Thread, int> nameMap = counter.Get("Name");
            Assert.IsNotNull(nameMap);
            Assert.AreEqual(1, nameMap.Count);
            Assert.IsTrue(idMap.ContainsKey(guiThread));
            Assert.AreEqual(1, nameMap.Get(guiThread));

            IMap<Thread, int> toBeUpdatedMap = counter.Get("ToBeUpdated");
            Assert.IsNotNull(toBeUpdatedMap);
            Assert.AreEqual(1, toBeUpdatedMap.Count);
            Assert.IsTrue(toBeUpdatedMap.ContainsKey(guiThread));
            Assert.AreEqual(1, toBeUpdatedMap.Get(guiThread));

            IMap<Thread, int> hasPendingChangesMap = counter.Get("HasPendingChanges");
            Assert.IsNotNull(hasPendingChangesMap);
            Assert.AreEqual(1, hasPendingChangesMap.Count);
            Assert.IsTrue(hasPendingChangesMap.ContainsKey(guiThread));
            Assert.AreEqual(2, hasPendingChangesMap.Get(guiThread));
        }

        //[TestMethod]
        //public void test_ValueHolderContainer2()
        //{
        //    InitManually();
        //    IServiceContext oldContext = ValueHolderFlattenHierarchyProxy.Context;
        //    ValueHolderFlattenHierarchyProxy.Context = this.BeanContext;
        //    try
        //    {
        //        Material parentEntity = EntityFactory.CreateEntity<Material>();
        //        //parentEntity.Types.Add(null);

        //        int a = 5;
        //    }
        //    finally
        //    {
        //        ValueHolderFlattenHierarchyProxy.Context = oldContext;
        //    }
        //}
    }
}