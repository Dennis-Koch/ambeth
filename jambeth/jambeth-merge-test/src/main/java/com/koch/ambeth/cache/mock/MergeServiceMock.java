package com.koch.ambeth.cache.mock;

/*-
 * #%L
 * jambeth-merge-test
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

import java.util.List;

import com.koch.ambeth.merge.model.ICUDResult;
import com.koch.ambeth.merge.model.IOriCollection;
import com.koch.ambeth.merge.service.IMergeService;
import com.koch.ambeth.service.merge.IValueObjectConfig;
import com.koch.ambeth.service.merge.model.IEntityMetaData;
import com.koch.ambeth.util.model.IMethodDescription;

/**
 * Support for unit tests that do not include jAmbeth.Cache
 */
public class MergeServiceMock implements IMergeService {
	@Override
	public IOriCollection merge(ICUDResult cudResult, IMethodDescription methodDescription) {
		return null;
	}

	@Override
	public List<IEntityMetaData> getMetaData(List<Class<?>> entityTypes) {
		return null;
	}

	@Override
	public IValueObjectConfig getValueObjectConfig(Class<?> valueType) {
		return null;
	}

	@Override
	public String createMetaDataDOT() {
		return null;
	}
}
