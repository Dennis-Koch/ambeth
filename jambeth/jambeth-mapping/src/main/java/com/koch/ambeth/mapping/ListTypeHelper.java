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

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.util.ParamChecker;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
import com.koch.ambeth.util.typeinfo.IPropertyInfo;
import com.koch.ambeth.util.typeinfo.IPropertyInfoProvider;

public class ListTypeHelper implements IListTypeHelper, IInitializingBean {
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	private IPropertyInfoProvider propertyInfoProvider;

	@Override
	public void afterPropertiesSet() throws Throwable {
		ParamChecker.assertNotNull(propertyInfoProvider, "propertyInfoProvider");
	}

	public void setPropertyInfoProvider(IPropertyInfoProvider propertyInfoProvider) {
		this.propertyInfoProvider = propertyInfoProvider;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <L> L packInListType(Collection<?> referencedVOs, Class<L> listType) {
		L listTypeInst;
		try {
			listTypeInst = listType.newInstance();
		}
		catch (Exception e) {
			throw RuntimeExceptionUtil.mask(e);
		}

		if (referencedVOs == null) {
			return listTypeInst;
		}

		IPropertyInfo accessor = getListTypeAccessor(listType);
		if (accessor.isWritable()) {
			if (!accessor.getPropertyType().isAssignableFrom(referencedVOs.getClass())) {
				Collection<Object> targetCollection;
				Class<?> propertyType = accessor.getPropertyType();
				if (List.class.isAssignableFrom(propertyType)) {
					targetCollection = new java.util.ArrayList<>(referencedVOs);
				}
				else if (Set.class.isAssignableFrom(propertyType)) {
					targetCollection = new java.util.HashSet<>(referencedVOs);
				}
				else {
					throw new IllegalArgumentException(
							"Collection type of '" + propertyType.getName() + "' is not supported");
				}
				referencedVOs = targetCollection;
			}
			accessor.setValue(listTypeInst, referencedVOs);
		}
		else {
			((Collection<Object>) accessor.getValue(listTypeInst)).addAll(referencedVOs);
		}

		return listTypeInst;
	}

	@Override
	public Object unpackListType(Object item) {
		IPropertyInfo accessor = getListTypeAccessor(item.getClass());
		Object value = accessor.getValue(item);
		return value;
	}

	@Override
	public boolean isListType(Class<?> type) {
		IPropertyInfo[] properties = propertyInfoProvider.getProperties(type);
		return properties.length == 1;
	}

	protected IPropertyInfo getListTypeAccessor(Class<?> type) {
		IPropertyInfo[] properties = propertyInfoProvider.getProperties(type);
		if (properties.length != 1) {
			throw new IllegalArgumentException(
					"ListTypes must have exactly one property: '" + type + "'");
		}
		return properties[0];
	}
}
