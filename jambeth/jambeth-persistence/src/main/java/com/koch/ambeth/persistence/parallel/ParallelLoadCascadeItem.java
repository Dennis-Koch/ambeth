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

import com.koch.ambeth.persistence.api.IDirectedLink;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.LinkedHashMap;

public class ParallelLoadCascadeItem extends AbstractParallelItem
{
	public final Class<?> entityType;

	public final IDirectedLink link;

	public final ArrayList<Object> splittedIds;

	public final int relationIndex;

	public ParallelLoadCascadeItem(Class<?> entityType, IDirectedLink link, ArrayList<Object> splittedIds, int relationIndex,
			LinkedHashMap<Class<?>, Collection<Object>[]> sharedCascadeTypeToPendingInit)
	{
		super(sharedCascadeTypeToPendingInit);
		this.entityType = entityType;
		this.link = link;
		this.splittedIds = splittedIds;
		this.relationIndex = relationIndex;
	}
}
