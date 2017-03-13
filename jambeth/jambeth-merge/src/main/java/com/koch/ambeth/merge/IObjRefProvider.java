package com.koch.ambeth.merge;

import com.koch.ambeth.service.merge.model.IEntityMetaData;
import com.koch.ambeth.service.merge.model.IObjRef;

public interface IObjRefProvider
{
	IObjRef getORI(Object obj, IEntityMetaData metaData);
}
