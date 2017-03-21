package com.koch.ambeth.persistence.database;

/*-
 * #%L
 * jambeth-persistence
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

import com.koch.ambeth.persistence.api.IDatabase;

public abstract class DefaultDatabaseLifecycleCallback implements IDatabaseLifecycleCallback
{

	@Override
	public void databaseNotFound(Object databaseHandle, String dbName)
	{
		// Intended blank
	}

	@Override
	public void databaseEmpty(Object databaseHandle)
	{
		// Intended blank
	}

	@Override
	public void databaseConnected(IDatabase database)
	{
		// Intended blank
	}

	@Override
	public void databaseClosed(IDatabase database)
	{
		// Intended blank
	}

}
