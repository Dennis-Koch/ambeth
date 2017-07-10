package com.koch.ambeth.service.merge.model;

/*-
 * #%L
 * jambeth-service
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
import java.lang.reflect.Modifier;

import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.repackaged.com.esotericsoftware.reflectasm.MethodAccess;
import com.koch.ambeth.service.merge.IEntityMetaDataProvider;
import com.koch.ambeth.util.ParamChecker;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;

public abstract class AbstractMethodLifecycleExtension
		implements IEntityLifecycleExtension, IInitializingBean {
	protected static final Object[] EMPTY_ARGS = new Object[0];

	@Autowired
	protected IEntityMetaDataProvider entityMetaDataProvider;

	protected Method method;

	protected MethodAccess methodAccess;

	protected int methodIndex;

	@Override
	public void afterPropertiesSet() throws Throwable {
		ParamChecker.assertNotNull(method, "method");
		if ((method.getModifiers() & Modifier.PRIVATE) == 0) {
			methodAccess = MethodAccess.get(method.getDeclaringClass());
			methodIndex = methodAccess.getIndex(method.getName(), method.getParameterTypes());
		}
	}

	public void setMethod(Method method) {
		this.method = method;
	}

	protected void callMethod(Object entity, String message) {
		try {
			if (methodAccess != null) {
				methodAccess.invoke(entity, methodIndex, EMPTY_ARGS);
			}
			else {
				method.invoke(entity, EMPTY_ARGS);
			}
		}
		catch (Exception e) {
			Class<?> entityType = entityMetaDataProvider.getMetaData(entity.getClass()).getEntityType();
			throw RuntimeExceptionUtil.mask(e, "Error occured while handling " + message
					+ " method of entity type " + entityType.getName());
		}
	}
}
