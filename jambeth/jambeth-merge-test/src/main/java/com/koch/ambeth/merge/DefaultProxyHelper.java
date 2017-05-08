package com.koch.ambeth.merge;

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

import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.bytecode.IBytecodeEnhancer;
import com.koch.ambeth.ioc.proxy.ICgLibUtil;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;

public class DefaultProxyHelper implements IProxyHelper, IInitializingBean {
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected ICgLibUtil cgLibUtil;

	@Autowired(optional = true)
	protected IBytecodeEnhancer entityEnhancer;

	@Override
	public void afterPropertiesSet() throws Throwable {
		// Intended blank
	}

	@Override
	public Class<?> getRealType(Class<?> type) {
		IBytecodeEnhancer entityEnhancer = this.entityEnhancer;
		if (entityEnhancer != null) {
			Class<?> baseType = entityEnhancer.getBaseType(type);
			if (baseType != null) {
				return baseType;
			}
		}
		return cgLibUtil.getOriginalClass(type);
	}

	@Override
	public boolean objectEquals(Object leftObject, Object rightObject) {
		if (leftObject == null) {
			return rightObject == null;
		}
		if (rightObject == null) {
			return false;
		}
		if (leftObject == rightObject) {
			return true;
		}
		return leftObject.equals(rightObject);
	}
}
