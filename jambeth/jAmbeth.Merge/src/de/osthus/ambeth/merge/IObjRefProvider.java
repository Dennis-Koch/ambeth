package de.osthus.ambeth.merge;

import de.osthus.ambeth.merge.model.IEntityMetaData;
import de.osthus.ambeth.merge.model.IObjRef;

public interface IObjRefProvider
{
	IObjRef getORI(Object obj, IEntityMetaData metaData);
}
