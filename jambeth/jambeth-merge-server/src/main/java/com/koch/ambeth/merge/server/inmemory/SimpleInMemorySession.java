package com.koch.ambeth.merge.server.inmemory;

import com.koch.ambeth.cache.RootCache;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.util.IDisposable;
import com.koch.ambeth.util.collections.HashSet;

public class SimpleInMemorySession implements IDisposable
{
	protected final RootCache data;

	protected final HashSet<IObjRef> createdObjRefs = new HashSet<IObjRef>();

	protected final HashSet<IObjRef> updatedObjRefs = new HashSet<IObjRef>();

	protected final HashSet<IObjRef> deletedObjRefs = new HashSet<IObjRef>();

	protected final HashSet<IObjRef> changesOfSession = new HashSet<IObjRef>();

	protected final SimpleInMemoryDatabase inMemoryDatabase;

	public SimpleInMemorySession(SimpleInMemoryDatabase inMemoryDatabase, RootCache data)
	{
		this.inMemoryDatabase = inMemoryDatabase;
		this.data = data;
	}

	@Override
	public void dispose()
	{
		inMemoryDatabase.releaseChangesOfSession(changesOfSession);
		data.dispose();
	}
}