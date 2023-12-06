package com.koch.ambeth.merge.mixin;

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.merge.IObjRefHelper;
import com.koch.ambeth.merge.proxy.IEntityMetaDataHolder;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.service.merge.model.IObjRefType;

import java.util.List;

public class ObjRefTypeMixin {
    @Autowired
    protected IObjRefHelper objRefHelper;

    public IObjRef getObjRef(IObjRefType entity) {
        return objRefHelper.entityToObjRef(entity);
    }

    public IObjRef getObjRef(IObjRefType entity, String identifierMemberName) {
        var metaData = ((IEntityMetaDataHolder) entity).get__EntityMetaData();
        var idIndex = metaData.getIdIndexByMemberName(identifierMemberName);
        return objRefHelper.entityToObjRef(entity, idIndex);
    }

    public List<IObjRef> getAllObjRefs(IObjRefType entity) {
        return objRefHelper.entityToAllObjRefs(entity);
    }
}
