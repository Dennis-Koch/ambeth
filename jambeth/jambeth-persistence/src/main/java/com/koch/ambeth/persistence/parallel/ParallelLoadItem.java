package com.koch.ambeth.persistence.parallel;

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

import java.util.Collection;

import com.koch.ambeth.persistence.LoadMode;
import com.koch.ambeth.util.collections.LinkedHashMap;

public class ParallelLoadItem extends AbstractParallelItem
{
	public final Class<?> entityType;

	public final byte idIndex;

	public final Collection<Object> ids;

	public final LoadMode loadMode;

	public ParallelLoadItem(Class<?> entityType, byte idIndex, Collection<Object> ids, LoadMode loadMode,
			LinkedHashMap<Class<?>, Collection<Object>[]> sharedCascadeTypeToPendingInit)
	{
		super(sharedCascadeTypeToPendingInit);
		this.entityType = entityType;
		this.idIndex = idIndex;
		this.ids = ids;
		this.loadMode = loadMode;
	}
}
