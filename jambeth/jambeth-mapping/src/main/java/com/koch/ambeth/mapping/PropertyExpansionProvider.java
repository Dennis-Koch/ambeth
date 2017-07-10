package com.koch.ambeth.mapping;

/*-
 * #%L
 * jambeth-mapping
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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.service.merge.IEntityMetaDataProvider;
import com.koch.ambeth.service.merge.model.IEntityMetaData;
import com.koch.ambeth.service.metadata.Member;
import com.koch.ambeth.util.ParamChecker;
import com.koch.ambeth.util.collections.AbstractTuple2KeyHashMap;
import com.koch.ambeth.util.collections.Tuple2KeyHashMap;

public class PropertyExpansionProvider implements IPropertyExpansionProvider {
	@Autowired
	protected IEntityMetaDataProvider entityMetaDataProvider;

	protected Tuple2KeyHashMap<Class<?>, String, PropertyExpansion> propertyExpansionCache = new Tuple2KeyHashMap<>();

	protected final Lock writeLock = new ReentrantLock();

	@Override
	public PropertyExpansion getPropertyExpansion(Class<?> entityType, String propertyPath) {
		ParamChecker.assertParamNotNull(entityType, "entityType");
		ParamChecker.assertParamNotNull(propertyPath, "propertyPath");

		PropertyExpansion propertyExpansion = propertyExpansionCache.get(entityType, propertyPath);
		if (propertyExpansion != null) {
			return propertyExpansion;
		}
		// we have a cache miss. create the propertyExpansion
		propertyExpansion = getPropertyExpansionIntern(entityType, propertyPath);
		if (propertyExpansion == null) {
			return propertyExpansion;
		}
		// here: COPY-ON-WRITE pattern to be threadsafe with reads (above) without a lock. This makes
		// sense because after a limited "warmup" phase there will
		// not be a cache miss any more with further runtime
		writeLock.lock();
		try {
			Tuple2KeyHashMap<Class<?>, String, PropertyExpansion> propertyExpansionCache = new Tuple2KeyHashMap<>(
					(int) (this.propertyExpansionCache.size() / AbstractTuple2KeyHashMap.DEFAULT_LOAD_FACTOR)
							+ 2);
			propertyExpansionCache.putAll(this.propertyExpansionCache);
			if (!propertyExpansionCache.putIfNotExists(entityType, propertyPath, propertyExpansion)) {
				return propertyExpansionCache.get(entityType, propertyPath);
			}
			this.propertyExpansionCache = propertyExpansionCache;
			return propertyExpansion;
		}
		finally {
			writeLock.unlock();
		}
	}

	protected PropertyExpansion getPropertyExpansionIntern(Class<?> entityType, String propertyPath) {
		IEntityMetaData metaData = entityMetaDataProvider.getMetaData(entityType);

		if (entityType == null) {
			return null;
		}

		List<String> path = getPath(propertyPath);
		Member[] memberPath = new Member[path.size()];
		IEntityMetaData[] metaDataPath = new IEntityMetaData[path.size()];
		Class<?> lastType = null;
		for (int a = 0, size = path.size(); a < size; a++) {
			String pathToken = path.get(a);
			if (metaData == null) {
				throw new IllegalArgumentException(
						"Could not find metaData for:" + (lastType == null ? "null" : lastType.toString()));
			}
			Member member = metaData.getMemberByName(pathToken);
			if (member == null) {
				throw new IllegalArgumentException("The provided propertyPath can not be resolved. Check: "
						+ pathToken + " (Hint: propertyNames need to start with an UpperCase letter!)");
			}
			memberPath[a] = member;
			// get next metaData
			metaData = entityMetaDataProvider.getMetaData(member.getRealType(), true);
			metaDataPath[a] = metaData;
			lastType = member.getRealType();
		}

		PropertyExpansion propertyExpansion = new PropertyExpansion(memberPath, metaDataPath);
		return propertyExpansion;
	}

	protected List<String> getPath(String propertyPath) {
		String[] pathTokens = propertyPath.split("\\.");
		if (pathTokens != null) {
			return Arrays.asList(pathTokens);
		}

		return Collections.emptyList();
	}
}
