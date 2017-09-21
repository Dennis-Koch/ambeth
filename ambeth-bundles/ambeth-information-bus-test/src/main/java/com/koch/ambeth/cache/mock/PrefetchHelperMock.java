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

import com.koch.ambeth.merge.util.IPrefetchConfig;
import com.koch.ambeth.merge.util.IPrefetchHelper;
import com.koch.ambeth.merge.util.IPrefetchState;
import com.koch.ambeth.util.collections.IList;

/**
 * Support for unit tests that do not include jAmbeth.Cache
 */
public class PrefetchHelperMock implements IPrefetchHelper {
	@Override
	public IPrefetchConfig createPrefetch() {
		return null;
	}

	@Override
	public IPrefetchState prefetch(Object objects) {
		return null;
	}

	@Override
	public <T, S> IList<T> extractTargetEntities(List<S> sourceEntities,
			String sourceToTargetEntityPropertyPath, Class<S> sourceEntityType) {
		return null;
	}
}
