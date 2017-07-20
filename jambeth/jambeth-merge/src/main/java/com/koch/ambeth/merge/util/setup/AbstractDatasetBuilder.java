package com.koch.ambeth.merge.util.setup;

/*-
 * #%L
 * jambeth-merge
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

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.merge.IEntityFactory;
import com.koch.ambeth.merge.IMergeProcess;
import com.koch.ambeth.service.merge.IEntityMetaDataProvider;
import com.koch.ambeth.util.collections.IdentityHashSet;

public abstract class AbstractDatasetBuilder implements IDatasetBuilder {
	@LogInstance
	private ILogger log;

	@Autowired
	protected IEntityFactory entityFactory;

	@Autowired
	protected IEntityMetaDataProvider entityMetaDataProvider;

	@Autowired
	protected IMergeProcess mergeProcess;

	protected final ThreadLocal<Collection<Object>> initialTestDatasetTL = new ThreadLocal<>();

	@Override
	public Collection<Object> buildDataset() {
		beforeBuildDataset();
		try {
			buildDatasetInternal();
			return initialTestDatasetTL.get();
		}
		finally {
			afterBuildDataset();
		}
	}

	protected abstract void buildDatasetInternal();

	protected void beforeBuildDataset() {
		IdentityHashSet<Object> initialTestDataset = new IdentityHashSet<>();
		initialTestDatasetTL.set(initialTestDataset);
	}

	protected void afterBuildDataset() {
		initialTestDatasetTL.remove();
	}

	protected <V> V createEntity(Class<V> entityType) {
		V entity = entityFactory.createEntity(entityType);
		Collection<Object> initialTestDataset = initialTestDatasetTL.get();
		// if the set is null it is not considered an error. it is assumed that someone called a
		// convenience method from a concrete class to create an entity. this is e.g. the case in JUnit
		// tests
		if (initialTestDataset != null) {
			initialTestDataset.add(entity);
		}
		return entity;
	}
}
