package com.koch.ambeth.merge.server.inmemory;

/*-
 * #%L
 * jambeth-merge-server
 * %%
 * Copyright (C) 2017 Koch Softwaredevelopment
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 * #L%
 */

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
