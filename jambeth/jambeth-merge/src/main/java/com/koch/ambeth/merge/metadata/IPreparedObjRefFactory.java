package com.koch.ambeth.merge.metadata;

import com.koch.ambeth.service.merge.model.IObjRef;

public interface IPreparedObjRefFactory
{
	IObjRef createObjRef(Object id, Object version);

	IObjRef createObjRef();
}
