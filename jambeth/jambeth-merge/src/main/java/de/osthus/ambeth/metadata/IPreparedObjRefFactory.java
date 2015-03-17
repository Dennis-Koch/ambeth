package de.osthus.ambeth.metadata;

import de.osthus.ambeth.merge.model.IObjRef;

public interface IPreparedObjRefFactory
{
	IObjRef createObjRef(Object id, Object version);

	IObjRef createObjRef();
}
