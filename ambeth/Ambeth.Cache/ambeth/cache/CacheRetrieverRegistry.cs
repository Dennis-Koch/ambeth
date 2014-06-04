using System;
using System.Collections.Generic;
using System.Threading;
using De.Osthus.Ambeth.Cache.Model;
using De.Osthus.Ambeth.Collections;
using De.Osthus.Ambeth.Ioc.Annotation;
using De.Osthus.Ambeth.Ioc.Extendable;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Merge;
using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Model;
using De.Osthus.Ambeth.Service;
using De.Osthus.Ambeth.Threading;
using De.Osthus.Ambeth.Typeinfo;
using De.Osthus.Ambeth.Util;

namespace De.Osthus.Ambeth.Cache
{
    public class CacheRetrieverRegistry : ICacheService, ICacheRetrieverExtendable, ICacheServiceByNameExtendable
    {
        [LogInstance]
        public ILogger Log { private get; set; }

        protected readonly Lock writeLock = new ReadWriteLock().WriteLock;

        protected readonly ClassExtendableContainer<ICacheRetriever> typeToCacheRetrieverMap = new ClassExtendableContainer<ICacheRetriever>("cacheRetriever", "entityType");

        protected readonly MapExtendableContainer<String, ICacheService> nameToCacheServiceMap = new MapExtendableContainer<String, ICacheService>("cacheService", "serviceName");

        public ICacheService CacheServiceForOris { protected get; set; }

        [Autowired(Optional = true)]
        public ICacheRetriever DefaultCacheRetriever { protected get; set; }

        [Autowired]
        public IEntityMetaDataProvider EntityMetaDataProvider { protected get; set; }

        public IThreadPool ThreadPool { protected get; set; }

        public void RegisterCacheRetriever(ICacheRetriever cacheRetriever, Type handledType)
        {
            ParamChecker.AssertParamNotNull(cacheRetriever, "CacheRetriever");
            ParamChecker.AssertParamNotNull(handledType, "handledType");

            writeLock.Lock();
            try
            {
                ICacheRetriever registered = typeToCacheRetrieverMap.GetExtension(handledType);
                if (registered == null)
                {
                    this.typeToCacheRetrieverMap.Register(cacheRetriever, handledType);
                }
                else if (registered.Equals(cacheRetriever))
                {
                    if (Log.InfoEnabled)
                    {
                        Log.Info("Duplicat registration of same service object for " + handledType);
                    }
                }
                else
                {
                    if (Log.InfoEnabled)
                    {
                        Log.Info("There is already a CacheService mapped to " + handledType);
                    }
                }
            }
            finally
            {
                writeLock.Unlock();
            }
        }

        public void UnregisterCacheRetriever(ICacheRetriever cacheRetriever, Type handledType)
        {
            ParamChecker.AssertParamNotNull(cacheRetriever, "CacheRetriever");
            ParamChecker.AssertParamNotNull(handledType, "handledType");

            writeLock.Lock();
            try
            {
                this.typeToCacheRetrieverMap.Unregister(cacheRetriever, handledType);
            }
            finally
            {
                writeLock.Unlock();
            }
        }

        public void RegisterCacheService(ICacheService cacheService, String serviceName)
        {
            ParamChecker.AssertParamNotNull(cacheService, "cacheService");
            ParamChecker.AssertParamNotNull(serviceName, "serviceName");

            this.nameToCacheServiceMap.Register(cacheService, serviceName);
        }

        public void UnregisterCacheService(ICacheService cacheService, String serviceName)
        {
            ParamChecker.AssertParamNotNull(cacheService, "cacheService");
            ParamChecker.AssertParamNotNull(serviceName, "serviceName");

            this.nameToCacheServiceMap.Unregister(cacheService, serviceName);
        }

        public IList<ILoadContainer> GetEntities(IList<IObjRef> orisToLoad)
        {
            ParamChecker.AssertParamNotNull(orisToLoad, "orisToLoad");

            IList<ILoadContainer> entities = new List<ILoadContainer>(orisToLoad.Count);
            IDictionary<Type, IList<IObjRef>> sortedIObjRefs = BucketSortObjRefs(orisToLoad);
            IDictionary<ICacheRetriever, IList<IObjRef>> assignedIObjRefs = AssignObjRefsToCacheRetriever(sortedIObjRefs);

            GetData(assignedIObjRefs, entities, delegate(ICacheRetriever cacheRetriever, IList<IObjRef> objRefsForService)
                {
                    return cacheRetriever.GetEntities(objRefsForService);
                });
            return entities;
        }

        public IList<IObjRelationResult> GetRelations(IList<IObjRelation> objRelations)
        {
            ParamChecker.AssertParamNotNull(objRelations, "objRelations");

            IList<IObjRelationResult> entities = new List<IObjRelationResult>(objRelations.Count);
            IDictionary<Type, IList<IObjRelation>> sortedObjRelations = BucketSortObjRels(objRelations);
            IDictionary<ICacheRetriever, IList<IObjRelation>> assignedObjRelations = AssignObjRelsToCacheRetriever(sortedObjRelations);

            GetData(assignedObjRelations, entities, delegate(ICacheRetriever cacheRetriever, IList<IObjRelation> objRelationsForService)
            {
                return cacheRetriever.GetRelations(objRelationsForService);
            });
            return entities;
        }

        public IServiceResult GetORIsForServiceRequest(IServiceDescription serviceDescription)
        {
            ICacheService cacheService = nameToCacheServiceMap.GetExtension(serviceDescription.ServiceName);
            if (cacheService == null)
            {
                if (CacheServiceForOris != null)
                {
                    return CacheServiceForOris.GetORIsForServiceRequest(serviceDescription);
                }
                throw new ArgumentException("No cache service registered for with name '" + serviceDescription.ServiceName + "'");
            }
            return cacheService.GetORIsForServiceRequest(serviceDescription);
        }

        protected ICacheRetriever GetRetrieverForType(Type type)
        {
            if (type == null)
            {
                return null;
            }
            ICacheRetriever cacheRetriever = typeToCacheRetrieverMap.GetExtension(type);
            if (cacheRetriever == null)
            {
                if (DefaultCacheRetriever != null && DefaultCacheRetriever != this)
                {
                    cacheRetriever = DefaultCacheRetriever;
                }
                else
                {
                    throw new Exception("No cache retriever found to handle entity type '" + type.FullName + "'");
                }
            }

            return cacheRetriever;
        }

        protected IDictionary<Type, IList<IObjRef>> BucketSortObjRefs(IList<IObjRef> orisToLoad)
        {
            IDictionary<Type, IList<IObjRef>> sortedIObjRefs = new Dictionary<Type, IList<IObjRef>>();

            for (int i = orisToLoad.Count; i-- > 0; )
            {
                IObjRef oriToLoad = orisToLoad[i];
                Type type = oriToLoad.RealType;
                IList<IObjRef> objRefs = DictionaryExtension.ValueOrDefault(sortedIObjRefs, type);
                if (objRefs == null)
                {
                    objRefs = new List<IObjRef>();
                    sortedIObjRefs.Add(type, objRefs);
                }
                objRefs.Add(oriToLoad);
            }
            return sortedIObjRefs;
        }

        protected IDictionary<Type, IList<IObjRelation>> BucketSortObjRels(IList<IObjRelation> orisToLoad)
        {
            IDictionary<Type, IList<IObjRelation>> sortedIObjRefs = new Dictionary<Type, IList<IObjRelation>>();

            for (int i = orisToLoad.Count; i-- > 0; )
            {
                IObjRelation orelToLoad = orisToLoad[i];
                Type typeOfContainerBO = orelToLoad.RealType;
                IEntityMetaData metaData = EntityMetaDataProvider.GetMetaData(typeOfContainerBO);
                ITypeInfoItem relationMember = metaData.GetMemberByName(orelToLoad.MemberName);

                Type type = relationMember.ElementType;
                IList<IObjRelation> objRefs = DictionaryExtension.ValueOrDefault(sortedIObjRefs, type);
                if (objRefs == null)
                {
                    objRefs = new List<IObjRelation>();
                    sortedIObjRefs.Add(type, objRefs);
                }
                objRefs.Add(orelToLoad);
            }
            return sortedIObjRefs;
        }

        protected IDictionary<ICacheRetriever, IList<IObjRef>> AssignObjRefsToCacheRetriever(IDictionary<Type, IList<IObjRef>> sortedIObjRefs)
        {
            IDictionary<ICacheRetriever, IList<IObjRef>> serviceToAssignedObjRefsDict = new IdentityDictionary<ICacheRetriever, IList<IObjRef>>();

            DictionaryExtension.Loop(sortedIObjRefs, delegate(Type type, IList<IObjRef> objRefs)
            {
                ICacheRetriever cacheRetriever = GetRetrieverForType(type);
                IList<IObjRef> assignedObjRefs = DictionaryExtension.ValueOrDefault(serviceToAssignedObjRefsDict, cacheRetriever);
                if (assignedObjRefs == null)
                {
                    assignedObjRefs = new List<IObjRef>();
                    serviceToAssignedObjRefsDict.Add(cacheRetriever, assignedObjRefs);
                }
                foreach (IObjRef objRef in objRefs)
                {
                    assignedObjRefs.Add(objRef);
                }
            });
            return serviceToAssignedObjRefsDict;
        }

        protected IDictionary<ICacheRetriever, IList<IObjRelation>> AssignObjRelsToCacheRetriever(IDictionary<Type, IList<IObjRelation>> sortedIObjRefs)
        {
            IDictionary<ICacheRetriever, IList<IObjRelation>> serviceToAssignedObjRefsDict = new IdentityDictionary<ICacheRetriever, IList<IObjRelation>>();

            DictionaryExtension.Loop(sortedIObjRefs, delegate(Type type, IList<IObjRelation> objRefs)
            {
                ICacheRetriever cacheRetriever = GetRetrieverForType(type);
                IList<IObjRelation> assignedObjRefs = DictionaryExtension.ValueOrDefault(serviceToAssignedObjRefsDict, cacheRetriever);
                if (assignedObjRefs == null)
                {
                    assignedObjRefs = new List<IObjRelation>();
                    serviceToAssignedObjRefsDict.Add(cacheRetriever, assignedObjRefs);
                }
                foreach (IObjRelation objRef in objRefs)
                {
                    assignedObjRefs.Add(objRef);
                }
            });
            return serviceToAssignedObjRefsDict;
        }

        protected void GetData<V, R>(IDictionary<ICacheRetriever, IList<V>> assignedArguments, IList<R> result, GetDataDelegate<V, R> getDataDelegate)
        {
            if (ThreadPool == null || assignedArguments.Count == 1)
            {
                // Serialize GetEntities() requests
                DictionaryExtension.Loop(assignedArguments, delegate(ICacheRetriever cacheRetriever, IList<V> arguments)
                {
                    IList<R> partResult = getDataDelegate.Invoke(cacheRetriever, arguments);
                    foreach (R partItem in partResult)
                    {
                        result.Add(partItem);
                    }
                });
                return;
            }
            int remainingResponses = assignedArguments.Count;
            Exception routedException = null;
            // Execute CacheRetrievers in parallel
            DictionaryExtension.Loop(assignedArguments, delegate(ICacheRetriever cacheRetriever, IList<V> arguments)
            {
                ThreadPool.Queue(delegate()
                {
                    try
                    {
                        IList<R> partResult = getDataDelegate.Invoke(cacheRetriever, arguments);

                        lock (result)
                        {
                            foreach (R partItem in partResult)
                            {
                                result.Add(partItem);
                            }
                        }
                    }
                    catch (Exception e)
                    {
                        routedException = e;
                    }
                    finally
                    {
                        lock (result)
                        {
                            remainingResponses--;
                            Monitor.Pulse(result);
                        }
                    }
                });
            });
            lock (result)
            {
                while (remainingResponses > 0 && routedException == null)
                {
                    Monitor.Wait(result);
                }
            }
            if (routedException != null)
            {
                throw new Exception("Error occured while retrieving entities", routedException);
            }
        }
    }

    public delegate IList<R> GetDataDelegate<V, R>(ICacheRetriever cacheRetriever, IList<V> arguments);
}