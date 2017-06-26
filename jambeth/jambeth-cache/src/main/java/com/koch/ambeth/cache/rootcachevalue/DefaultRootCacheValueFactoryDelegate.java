package com.koch.ambeth.cache.rootcachevalue;

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

import com.koch.ambeth.ioc.accessor.AccessorClassLoader;
import com.koch.ambeth.service.merge.model.IEntityMetaData;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;

import net.sf.cglib.reflect.FastClass;
import net.sf.cglib.reflect.FastConstructor;

public class DefaultRootCacheValueFactoryDelegate extends RootCacheValueFactoryDelegate {

	protected final FastConstructor constructor;

	public DefaultRootCacheValueFactoryDelegate() {
		AccessorClassLoader classLoader = AccessorClassLoader.get(DefaultRootCacheValue.class);
		FastClass fastClass = FastClass.create(classLoader, DefaultRootCacheValue.class);
		try {
			constructor = fastClass
					.getConstructor(DefaultRootCacheValue.class.getConstructor(IEntityMetaData.class));
		}
		catch (Exception e) {
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Override
	public RootCacheValue createRootCacheValue(IEntityMetaData metaData) {
		try {
			return (RootCacheValue) constructor.newInstance(new Object[] {metaData});
		}
		catch (Exception e) {
			throw RuntimeExceptionUtil.mask(e);
		}
	}
}
