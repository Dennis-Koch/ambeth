package de.osthus.ambeth.inmemory;

import de.osthus.ambeth.cache.RootCache;
import de.osthus.ambeth.collections.HashSet;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.util.IDisposable;

public class SimpleInMemorySession implements IDisposable
{
	protected final RootCache data;

	protected final HashSet<IObjRef> createdObjRefs = new HashSet<IObjRef>();

	protected final HashSet<IObjRef> updatedObjRefs = new HashSet<IObjRef>();

	protected final HashSet<IObjRef> deletedObjRefs = new HashSet<IObjRef>();

	public SimpleInMemorySession(RootCache data)
	{
		this.data = data;
	}

	@Override
	public void dispose()
	{
		data.dispose();
	}
}