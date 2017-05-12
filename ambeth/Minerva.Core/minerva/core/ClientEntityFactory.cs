//using System;
//using De.Osthus.Ambeth.Cache;
//using De.Osthus.Ambeth.Ioc;
//using De.Osthus.Ambeth.Proxy;
//using De.Osthus.Ambeth.Util;
//#if SILVERLIGHT
//using Castle.Core.Interceptor;
//#else
//using Castle.DynamicProxy;
//#endif
//using System.Reflection;
//using System.ComponentModel;
//using System.Text.RegularExpressions;
//using De.Osthus.Ambeth.Typeinfo;
//using De.Osthus.Ambeth.Merge;
//using De.Osthus.Ambeth.Merge.Model;
//using System.Collections.Generic;
//using De.Osthus.Ambeth.Collections;
//using De.Osthus.Minerva.Core.Config;
//using De.Osthus.Ambeth.Config;
//using De.Osthus.Ambeth.Annotation;
//using De.Osthus.Ambeth.Model;
//using De.Osthus.Ambeth.Threading;
//using System.Collections;
//using De.Osthus.Ambeth.Merge.Proxy;

//namespace De.Osthus.Minerva.Core
//{
//    public class ClientEntityFactory : AbstractEntityFactory
//    {
//        public class GetterItem
//        {
//            public readonly MethodInfo getter;

//            public readonly PropertyInfo property;

//            public readonly String[] propertyNames;

//            public GetterItem(MethodInfo getter, PropertyInfo property, String[] propertyNames)
//            {
//                this.getter = getter;
//                this.property = property;
//                this.propertyNames = propertyNames;
//            }
//        }

//        private static readonly Type[] interfaces = new Type[] { typeof(IDataObject), typeof(IParentCacheValueHardRef), typeof(INotifyPropertyChanged), typeof(INotifyPropertyChangedSource) };

//        private static readonly Type[] embeddedTypeInterfaces = new Type[] { typeof(INotifyPropertyChanged) };

//        protected readonly ISet<Object> hardRefSet = new IdentityHashSet<Object>();

//        protected readonly IDictionary<Type, IDictionary<String, GetterItem>[]> typeToGetterMappingDict = new Dictionary<Type, IDictionary<String, GetterItem>[]>();

//        public ICacheModification CacheModification { protected get; set; }
        
//        public IProxyFactory ProxyFactory { protected get; set; }

//        public IRevertChangesHelper RevertChangesHelper { protected get; set; }

//        public IThreadPool ThreadPool { protected get; set; }

//        [Property(MinervaCoreConfigurationConstants.EntityProxyActive, DefaultValue = "false")]
//        public bool IsEntityProxyActive { protected get; set; }

//        protected readonly Lock readLock, writeLock;

//        public ClientEntityFactory()
//        {
//            ReadWriteLock rwLock = new ReadWriteLock();
//            readLock = rwLock.ReadLock;
//            writeLock = rwLock.WriteLock;
//        }

//        public virtual void AfterPropertiesSet()
//        {
//            ParamChecker.AssertNotNull(CacheModification, "CacheModification");
//            ParamChecker.AssertNotNull(EntityMetaDataProvider, "EntityMetaDataProvider");
//            ParamChecker.AssertNotNull(ProxyFactory, "ProxyFactory");
//            ParamChecker.AssertNotNull(RevertChangesHelper, "RevertChangesHelper");
//            ParamChecker.AssertNotNull(ThreadPool, "ThreadPool");
//        }
                
//        public override Object CreateEntity(IEntityMetaData metaData)
//        {
//            if (IsEntityProxyActive)
//            {
//                ICacheModification cacheModification = CacheModification;

//                Type entityType = metaData.EntityType;
//                IDictionary<String, GetterItem>[] getterMappingDict = GetMapping(entityType);
//                ClientEntityInterceptor interceptor = new ClientEntityInterceptor(RevertChangesHelper, ThreadPool, writeLock, hardRefSet,
//                    getterMappingDict[0], getterMappingDict[1], metaData.IdMember, cacheModification);
//                Object proxy = ProxyFactory.CreateProxy(entityType, interfaces, interceptor);
//                interceptor.SetProxy(proxy);

//                bool oldCacheModActive = cacheModification.Active;
//                cacheModification.Active = true;
//                try
//                {
//                    foreach (ITypeInfoItem primitiveMember in metaData.PrimitiveMembers)
//                    {
//                        // Check for embedded members
//                        if (!(primitiveMember is EmbeddedTypeInfoItem))
//                        {
//                            Type realType = primitiveMember.RealType;
//                            if (typeof(IEnumerable).IsAssignableFrom(realType) && !typeof(String).Equals(realType)
//                                && !realType.IsArray)
//                            {
//                                Object primitive = primitiveMember.GetValue(proxy);
//                                if (primitive == null)
//                                {
//                                    primitive = ListUtil.CreateObservableCollectionOfType(realType);
//                                    primitiveMember.SetValue(proxy, primitive);
//                                }
//                            }
//                            continue;
//                        }
//                        ITypeInfoItem[] memberPath = ((EmbeddedTypeInfoItem)primitiveMember).MemberPath;
//                        Object parentObject = proxy;
//                        foreach (ITypeInfoItem pathItem in memberPath)
//                        {
//                            Type currentEmbeddedType = pathItem.RealType;
//                            IDictionary<String, GetterItem>[] embeddedTypeGetterMappingDict = GetMapping(currentEmbeddedType);

//                            EmbeddedTypeInterceptor embeddedTypeInterceptor = new EmbeddedTypeInterceptor(parentObject,
//                                embeddedTypeGetterMappingDict[0], embeddedTypeGetterMappingDict[1], cacheModification);
//                            Object embeddedProxy = ProxyFactory.CreateProxy(currentEmbeddedType, embeddedTypeInterfaces, embeddedTypeInterceptor);
//                            embeddedTypeInterceptor.SetProxy(embeddedProxy);
//                            pathItem.SetValue(parentObject, embeddedProxy);
//                            parentObject = embeddedProxy;
//                        }
//                    }
//                }
//                finally
//                {
//                    cacheModification.Active = oldCacheModActive;
//                }
//                return proxy;
//            }
//            return Activator.CreateInstance(metaData.EntityType);
//        }

//        protected IDictionary<String, GetterItem>[] GetMapping(Type entityType)
//        {
//            readLock.Lock();
//            try
//            {
//                IDictionary<String, GetterItem>[] getterMappingDicts = DictionaryExtension.ValueOrDefault(typeToGetterMappingDict, entityType);
//                if (getterMappingDicts != null)
//                {
//                    return getterMappingDicts;
//                }
//            }
//            finally
//            {
//                readLock.Unlock();
//            }
//            ISet<String> propertyNameSet = new HashSet<String>();
//            IDictionary<String, GetterItem> setterToGetterDict = new Dictionary<String, GetterItem>();
//            IDictionary<String, GetterItem> getterToSetterDict = new Dictionary<String, GetterItem>();
//            PropertyInfo[] properties = entityType.GetProperties(BindingFlags.Instance | BindingFlags.Public | BindingFlags.NonPublic);
//            foreach (PropertyInfo propertyInfo in properties)
//            {
//                MethodInfo setter = propertyInfo.GetSetMethod();
//                if (setter != null)
//                {
//                    MethodInfo getter = propertyInfo.GetGetMethod();
//                    if (getter != null)
//                    {
//                        Object[] annotations = propertyInfo.GetCustomAttributes(true);
//                        try
//                        {
//                            propertyNameSet.Add(propertyInfo.Name);
//                            for (int a = 0, size = annotations.Length; a < size; a++)
//                            {
//                                Object annotation = annotations[a];
//                                if (!(annotation is PropertyChangedAttribute))
//                                {
//                                    continue;
//                                }
//                                propertyNameSet.Add(((PropertyChangedAttribute)annotation).PropertyName);
//                            }
//                            GetterItem getterItem = new GetterItem(getter, propertyInfo, ListUtil.ToArray(propertyNameSet));
//                            GetterItem setterItem = new GetterItem(setter, propertyInfo, ListUtil.ToArray(propertyNameSet));
//                            setterToGetterDict.Add(setter.Name, getterItem);
//                            getterToSetterDict.Add(getter.Name, setterItem);
//                        }
//                        finally
//                        {
//                            propertyNameSet.Clear();
//                        }
//                    }
//                }
//            }
//            writeLock.Lock();
//            try
//            {
//                IDictionary<String, GetterItem>[] getterMappingDicts = DictionaryExtension.ValueOrDefault(typeToGetterMappingDict, entityType);
//                if (getterMappingDicts != null)
//                {
//                    return getterMappingDicts;
//                }
//                getterMappingDicts = new IDictionary<String, GetterItem>[] { setterToGetterDict, getterToSetterDict };
//                typeToGetterMappingDict.Add(entityType, getterMappingDicts);
//                return getterMappingDicts;
//            }
//            finally
//            {
//                writeLock.Unlock();
//            }
//        }
//    }
//}