using De.Osthus.Ambeth.CompositeId;
using De.Osthus.Ambeth.Merge;
using De.Osthus.Ambeth.Util;
using System;

namespace De.Osthus.Ambeth.Xml.Test
{
    class EmptyDummy : ICompositeIdFactory, IMergeController, IPrefetchHelper
    {
        public Typeinfo.ITypeInfoItem CreateCompositeIdMember(Merge.Model.IEntityMetaData metaData, Typeinfo.ITypeInfoItem[] idMembers)
        {
            throw new System.NotImplementedException();
        }

        public Typeinfo.ITypeInfoItem CreateCompositeIdMember(Type entityType, Typeinfo.ITypeInfoItem[] idMembers)
        {
            throw new System.NotImplementedException();
        }

        public object CreateCompositeId(Merge.Model.IEntityMetaData metaData, Typeinfo.ITypeInfoItem compositeIdMember, params object[] ids)
        {
            throw new System.NotImplementedException();
        }

        public object CreateIdFromPrimitives(Merge.Model.IEntityMetaData metaData, int idIndex, object[] primitives)
        {
            throw new System.NotImplementedException();
        }

        public object CreateIdFromPrimitives(Merge.Model.IEntityMetaData metaData, int idIndex, Cache.AbstractCacheValue cacheValue)
        {
            throw new System.NotImplementedException();
        }

        public void ApplyChangesToOriginals(System.Collections.Generic.IList<object> originalRefs, System.Collections.Generic.IList<Merge.Model.IObjRef> oriList, System.DateTime? changedOn, string changedBy)
        {
            throw new System.NotImplementedException();
        }

        public Merge.Model.ICUDResult MergeDeep(object obj, MergeHandle handle)
        {
            throw new System.NotImplementedException();
        }

        public System.Collections.Generic.IList<object> ScanForInitializedObjects(object obj, bool isDeepMerge, Collections.IMap<System.Type, System.Collections.Generic.IList<object>> typeToObjectsToMerge, System.Collections.Generic.IList<Merge.Model.IObjRef> objRefs, System.Collections.Generic.IList<ValueHolderRef> valueHolderRefs)
        {
            throw new System.NotImplementedException();
        }

        public IPrefetchConfig CreatePrefetch()
        {
            throw new System.NotImplementedException();
        }

        public IPrefetchState Prefetch(object objects)
        {
            throw new System.NotImplementedException();
        }

        public IPrefetchState Prefetch(object objects, System.Collections.Generic.IDictionary<System.Type, System.Collections.Generic.IList<string>> typeToPathToInitialize)
        {
            throw new System.NotImplementedException();
        }

        public System.Collections.Generic.ICollection<T> ExtractTargetEntities<T, S>(System.Collections.Generic.IEnumerable<S> sourceEntities, string sourceToTargetEntityPropertyPath)
        {
            throw new System.NotImplementedException();
        }
    }
}
