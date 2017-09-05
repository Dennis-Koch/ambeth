package com.koch.ambeth.cache.mixin;

/*-
 * #%L
 * jambeth-cache
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

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.merge.IRevertChangesHelper;
import com.koch.ambeth.merge.cache.ICacheModification;
import com.koch.ambeth.util.model.IDataObject;

public class DataObjectMixin {
	@Autowired
	protected ICacheModification cacheModification;

	@Autowired
	protected IRevertChangesHelper revertChangesHelper;

	public final void toBeUpdatedChanged(IDataObject obj, boolean previousValue,
			boolean currentValue) {
		// Intended blank
	}
}
