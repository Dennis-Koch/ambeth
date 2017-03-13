package com.koch.ambeth.persistence.api;

import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.util.collections.IList;

public interface IPrimaryKeyProvider
{
	void acquireIds(ITableMetaData table, IList<IObjRef> idlessObjRefs);
}
