using De.Osthus.Ambeth.Accessor;
using De.Osthus.Ambeth.Bytecode;
using De.Osthus.Ambeth.Collections;
using De.Osthus.Ambeth.Ioc.Annotation;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Merge;
using System;

namespace De.Osthus.Ambeth.Objrefstore
{
    public class ObjRefStoreEntryProvider : IObjRefStoreEntryProvider
    {
        [LogInstance]
        public ILogger Log { private get; set; }

        [Autowired]
        public IAccessorTypeProvider AccessorTypeProvider { protected get; set; }

        [Autowired]
        public IBytecodeEnhancer BytecodeEnhancer { protected get; set; }

        [Autowired]
        public IEntityMetaDataProvider EtityMetaDataProvider { protected get; set; }

        protected readonly Tuple2KeyHashMap<Type, int, IObjRefStoreFactory> constructorDelegateMap = new Tuple2KeyHashMap<Type, int, IObjRefStoreFactory>();

        protected readonly Object writeLock = new Object();

        public override ObjRefStore CreateObjRefStore(Type entityType, sbyte idIndex, Object id)
        {
            IObjRefStoreFactory objRefConstructorDelegate = constructorDelegateMap.Get(entityType, idIndex);
            if (objRefConstructorDelegate == null)
            {
                objRefConstructorDelegate = BuildDelegate(entityType, idIndex);
            }
            ObjRefStore objRefStore = objRefConstructorDelegate.CreateObjRef();
            objRefStore.Id = id;
            return objRefStore;
        }

        public override ObjRefStore CreateObjRefStore(Type entityType, sbyte idIndex, Object id, ObjRefStore nextEntry)
        {
            IObjRefStoreFactory objRefConstructorDelegate = constructorDelegateMap.Get(entityType, idIndex);
            if (objRefConstructorDelegate == null)
            {
                objRefConstructorDelegate = BuildDelegate(entityType, idIndex);
            }
            ObjRefStore objRefStore = objRefConstructorDelegate.CreateObjRef();
            objRefStore.Id = id;
            objRefStore.NextEntry = nextEntry;
            return objRefStore;
        }

        protected IObjRefStoreFactory BuildDelegate(Type entityType, int idIndex)
        {
            lock (writeLock)
            {
                IObjRefStoreFactory objRefConstructorDelegate = constructorDelegateMap.Get(entityType, idIndex);
                if (objRefConstructorDelegate != null)
                {
                    return objRefConstructorDelegate;
                }
                Type enhancedType = BytecodeEnhancer.GetEnhancedType(typeof(ObjRefStore), new ObjRefStoreEnhancementHint(entityType, idIndex));
                objRefConstructorDelegate = AccessorTypeProvider.GetConstructorType<IObjRefStoreFactory>(enhancedType);
                constructorDelegateMap.Put(entityType, idIndex, objRefConstructorDelegate);
                return objRefConstructorDelegate;
            }
        }
    }
}