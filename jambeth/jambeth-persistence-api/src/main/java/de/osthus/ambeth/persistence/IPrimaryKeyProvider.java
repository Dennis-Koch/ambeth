package de.osthus.ambeth.persistence;

import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.merge.model.IObjRef;

public interface IPrimaryKeyProvider
{
	void acquireIds(ITableMetaData table, IList<IObjRef> idlessObjRefs);
}
