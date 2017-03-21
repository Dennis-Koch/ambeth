package com.koch.ambeth.persistence.jdbc.database;

/*-
 * #%L
 * jambeth-persistence-jdbc
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
import com.koch.ambeth.util.collections.LinkedHashMap;

public class TransactionBeginEvent
{
	protected final LinkedHashMap<Object, IDatabase> persistenceUnitToDatabaseMap;

	public TransactionBeginEvent(LinkedHashMap<Object, IDatabase> persistenceUnitToDatabaseMap)
	{
		this.persistenceUnitToDatabaseMap = persistenceUnitToDatabaseMap;
	}

	public LinkedHashMap<Object, IDatabase> getPersistenceUnitToDatabaseMap()
	{
		return persistenceUnitToDatabaseMap;
	}
}
