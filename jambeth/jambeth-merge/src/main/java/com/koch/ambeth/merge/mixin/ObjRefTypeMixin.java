package com.koch.ambeth.merge.mixin;

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.merge.IObjRefHelper;
import com.koch.ambeth.merge.proxy.IEntityMetaDataHolder;
import com.koch.ambeth.service.merge.model.IEntityMetaData;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.service.merge.model.IObjRefType;
import com.koch.ambeth.util.collections.IList;

public class ObjRefTypeMixin {
	@Autowired
	protected IObjRefHelper objRefHelper;

	public IObjRef getObjRef(IObjRefType entity) {
		return objRefHelper.entityToObjRef(entity);
	}

	public IObjRef getObjRef(IObjRefType entity, String identifierMemberName) {
		IEntityMetaData metaData = ((IEntityMetaDataHolder) entity).get__EntityMetaData();
		int idIndex = metaData.getIdIndexByMemberName(identifierMemberName);
		return objRefHelper.entityToObjRef(entity, idIndex);
	}

	public IList<IObjRef> getAllObjRefs(IObjRefType entity) {
		return objRefHelper.entityToAllObjRefs(entity);
	}
}
