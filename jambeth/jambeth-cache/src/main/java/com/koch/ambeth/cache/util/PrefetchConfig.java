package com.koch.ambeth.cache.util;

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

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.merge.util.IPrefetchConfig;
import com.koch.ambeth.merge.util.IPrefetchHandle;
import com.koch.ambeth.service.merge.IEntityMetaDataProvider;
import com.koch.ambeth.service.merge.model.IEntityMetaData;
import com.koch.ambeth.service.metadata.Member;
import com.koch.ambeth.util.ListUtil;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.HashMap;
import com.koch.ambeth.util.collections.LinkedHashMap;
import com.koch.ambeth.util.collections.LinkedHashSet;
import com.koch.ambeth.util.proxy.AbstractSimpleInterceptor;
import com.koch.ambeth.util.proxy.IProxyFactory;
import com.koch.ambeth.util.typeinfo.IPropertyInfoProvider;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

public class PrefetchConfig implements IPrefetchConfig {
	public class PrefetchConfigInterceptor implements MethodInterceptor {
		private final IEntityMetaData currMetaData;

		private final Class<?> baseEntityType;

		private final String propertyPath;

		public PrefetchConfigInterceptor(IEntityMetaData currMetaData, Class<?> baseEntityType,
				String propertyPath) {
			this.currMetaData = currMetaData;
			this.baseEntityType = baseEntityType;
			this.propertyPath = propertyPath;
		}

		@Override
		public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy)
				throws Throwable {
			if (AbstractSimpleInterceptor.finalizeMethod.equals(method)) {
				return null;
			}
			String propertyName = propertyInfoProvider.getPropertyNameFor(method);
			if (propertyName == null) {
				return null;
			}
			Member member = currMetaData.getMemberByName(propertyName);
			IEntityMetaData targetMetaData = entityMetaDataProvider.getMetaData(member.getElementType());

			Object childPlan = planIntern(baseEntityType,
					propertyPath != null ? propertyPath + "." + propertyName : propertyName, targetMetaData);

			if (member.getElementType().equals(member.getRealType())) {
				// to-one relation
				return childPlan;
			}
			Collection<Object> list = ListUtil.createCollectionOfType(member.getRealType(), 1);
			list.add(childPlan);
			return list;
		}
	}

	@Autowired
	protected ICachePathHelper cachePathHelper;

	@Autowired
	protected IEntityMetaDataProvider entityMetaDataProvider;

	@Autowired
	protected IPropertyInfoProvider propertyInfoProvider;

	@Autowired
	protected IProxyFactory proxyFactory;

	protected final HashMap<Class<?>, ArrayList<String>> entityTypeToPrefetchPaths =
			new HashMap<>();

	@Override
	public <T> T plan(Class<T> entityType) {
		return planIntern(entityType, null, entityMetaDataProvider.getMetaData(entityType));
	}

	@SuppressWarnings("unchecked")
	protected <T> T planIntern(final Class<?> baseEntityType, final String propertyPath,
			final IEntityMetaData currMetaData) {
		if (propertyPath != null) {
			add(baseEntityType, propertyPath);
		}
		return (T) proxyFactory.createProxy(getClass().getClassLoader(), currMetaData.getEntityType(),
				new PrefetchConfigInterceptor(currMetaData, baseEntityType, propertyPath));
	}

	@Override
	public IPrefetchConfig add(Class<?> entityType, String propertyPath) {
		entityType = entityMetaDataProvider.getMetaData(entityType).getEntityType();
		ArrayList<String> membersToInitialize = entityTypeToPrefetchPaths.get(entityType);
		if (membersToInitialize == null) {
			membersToInitialize = new ArrayList<>();
			entityTypeToPrefetchPaths.put(entityType, membersToInitialize);
		}
		membersToInitialize.add(propertyPath);
		return this;
	}

	@Override
	public IPrefetchConfig add(Class<?> entityType, String... propertyPaths) {
		entityType = entityMetaDataProvider.getMetaData(entityType).getEntityType();
		ArrayList<String> membersToInitialize = entityTypeToPrefetchPaths.get(entityType);
		if (membersToInitialize == null) {
			membersToInitialize = new ArrayList<>();
			entityTypeToPrefetchPaths.put(entityType, membersToInitialize);
		}
		membersToInitialize.addAll(propertyPaths);
		return this;
	}

	@Override
	public IPrefetchHandle build() {
		LinkedHashMap<Class<?>, PrefetchPath[]> entityTypeToPrefetchSteps =
				LinkedHashMap.create(entityTypeToPrefetchPaths.size());
		for (Entry<Class<?>, ArrayList<String>> entry : entityTypeToPrefetchPaths) {
			Class<?> entityType = entry.getKey();
			ArrayList<String> membersToInitialize = entry.getValue();
			entityTypeToPrefetchSteps.put(entityType, buildCachePath(entityType, membersToInitialize));
		}
		return new PrefetchHandle(entityTypeToPrefetchSteps, cachePathHelper);
	}

	protected PrefetchPath[] buildCachePath(Class<?> entityType, List<String> membersToInitialize) {
		LinkedHashSet<AppendableCachePath> cachePaths = new LinkedHashSet<>();
		for (int a = membersToInitialize.size(); a-- > 0;) {
			String memberName = membersToInitialize.get(a);
			cachePathHelper.buildCachePath(entityType, memberName, cachePaths);
		}
		return cachePathHelper.copyAppendableToCachePath(cachePaths);
	}
}
