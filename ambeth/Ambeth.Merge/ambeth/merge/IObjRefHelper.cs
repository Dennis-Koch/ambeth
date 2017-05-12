using System;
using System.Collections.Generic;
using De.Osthus.Ambeth.Merge.Model;

namespace De.Osthus.Ambeth.Merge
{
    public interface IObjRefHelper
    {
        IList<IObjRef> ExtractObjRefList(Object objValue, MergeHandle mergeHandle);

        IList<IObjRef> ExtractObjRefList(Object objValue, MergeHandle mergeHandle, IList<IObjRef> targetOriList);

        IList<IObjRef> ExtractObjRefList(Object objValue, MergeHandle mergeHandle, IList<IObjRef> targetOriList, EntityCallback entityCallback);

        IList<IObjRef> ExtractObjRefList(Object objValue, IObjRefProvider oriProvider, IList<IObjRef> targetOriList, EntityCallback entityCallback);

        IObjRef GetCreateObjRef(Object obj, IObjRefProvider oriProvider);

        IObjRef GetCreateObjRef(Object obj, MergeHandle mergeHandle);

        IObjRef EntityToObjRef(Object entity);

        IObjRef EntityToObjRef(Object entity, bool forceOri);

        IObjRef EntityToObjRef(Object entity, int idIndex);

        IObjRef EntityToObjRef(Object entity, IEntityMetaData metaData);

        IObjRef EntityToObjRef(Object entity, int idIndex, IEntityMetaData metaData);

        IObjRef EntityToObjRef(Object entity, int idIndex, IEntityMetaData metaData, bool forceOri);

        IList<IObjRef> EntityToAllObjRefs(Object id, Object version, Object[] primitives, IEntityMetaData metaData);

        IList<IObjRef> EntityToAllObjRefs(Object entity);

        IList<IObjRef> EntityToAllObjRefs(Object entity, IEntityMetaData metaData);
    }
}
