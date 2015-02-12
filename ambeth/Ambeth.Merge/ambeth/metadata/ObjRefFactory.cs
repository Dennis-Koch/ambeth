using De.Osthus.Ambeth.Accessor;
using De.Osthus.Ambeth.Bytecode;
using De.Osthus.Ambeth.Cache;
using De.Osthus.Ambeth.Collections;
using De.Osthus.Ambeth.Ioc.Annotation;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Merge;
using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Merge.Transfer;
using System;

namespace De.Osthus.Ambeth.Metadata
{
    public class ObjRefFactory : IObjRefFactory
    {
        [LogInstance]
        public ILogger Log { private get; set; }

        [Autowired]
        public IAccessorTypeProvider AccessorTypeProvider { protected get; set; }

        [Autowired]
        public IBytecodeEnhancer BytecodeEnhancer { protected get; set; }

        [Autowired]
        public IEntityMetaDataProvider EntityMetaDataProvider { protected get; set; }

        protected readonly Tuple2KeyHashMap<Type, int, IPreparedObjRefFactory> constructorDelegateMap = new Tuple2KeyHashMap<Type, int, IPreparedObjRefFactory>();

        protected readonly Object writeLock = new Object();

        protected IPreparedObjRefFactory BuildDelegate(Type realType, int idIndex)
        {
            lock (writeLock)
            {
                IPreparedObjRefFactory objRefConstructorDelegate = constructorDelegateMap.Get(realType, idIndex);
                if (objRefConstructorDelegate != null)
                {
                    return objRefConstructorDelegate;
                }
                Type enhancedType = BytecodeEnhancer.GetEnhancedType(typeof(Object), new ObjRefEnhancementHint(realType, idIndex));
                objRefConstructorDelegate = AccessorTypeProvider.GetConstructorType<IPreparedObjRefFactory>(enhancedType);
                constructorDelegateMap.Put(realType, idIndex, objRefConstructorDelegate);
                return objRefConstructorDelegate;
            }
        }

        public override IObjRef Dup(IObjRef objRef)
        {
            IPreparedObjRefFactory objRefConstructorDelegate = constructorDelegateMap.Get(objRef.RealType, objRef.IdNameIndex);
            if (objRefConstructorDelegate == null)
            {
                objRefConstructorDelegate = BuildDelegate(objRef.RealType, objRef.IdNameIndex);
            }
            return objRefConstructorDelegate.CreateObjRef(objRef.Id, objRef.Version);
        }

        public override IObjRef CreateObjRef(AbstractCacheValue cacheValue)
        {
            IPreparedObjRefFactory objRefConstructorDelegate = constructorDelegateMap.Get(cacheValue.EntityType, ObjRef.PRIMARY_KEY_INDEX);
            if (objRefConstructorDelegate == null)
            {
                objRefConstructorDelegate = BuildDelegate(cacheValue.EntityType, ObjRef.PRIMARY_KEY_INDEX);
            }
            return objRefConstructorDelegate.CreateObjRef(cacheValue.Id, cacheValue.Version);
        }

        public override IObjRef CreateObjRef(AbstractCacheValue cacheValue, int idIndex)
        {
            IPreparedObjRefFactory objRefConstructorDelegate = constructorDelegateMap.Get(cacheValue.EntityType, idIndex);
            if (objRefConstructorDelegate == null)
            {
                objRefConstructorDelegate = BuildDelegate(cacheValue.EntityType, idIndex);
            }
            return objRefConstructorDelegate.CreateObjRef(cacheValue.Id, cacheValue.Version);
        }

        public override IObjRef CreateObjRef(Type entityType, int idIndex, Object id, Object version)
        {
            IPreparedObjRefFactory objRefConstructorDelegate = constructorDelegateMap.Get(entityType, idIndex);
            if (objRefConstructorDelegate == null)
            {
                objRefConstructorDelegate = BuildDelegate(entityType, idIndex);
            }
            return objRefConstructorDelegate.CreateObjRef(id, version);
        }

        public override IPreparedObjRefFactory PrepareObjRefFactory(Type entityType, int idIndex)
        {
            IPreparedObjRefFactory objRefConstructorDelegate = constructorDelegateMap.Get(entityType, idIndex);
            if (objRefConstructorDelegate == null)
            {
                objRefConstructorDelegate = BuildDelegate(entityType, idIndex);
            }
            return objRefConstructorDelegate;
        }
    }
}