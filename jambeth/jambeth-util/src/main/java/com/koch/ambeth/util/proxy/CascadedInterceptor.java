package com.koch.ambeth.util.proxy;

import java.lang.reflect.InvocationHandler;

/*-
 * #%L
 * jambeth-util
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

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

public abstract class CascadedInterceptor extends AbstractSimpleInterceptor
		implements ICascadedInterceptor {
	private Object target;

	@Override
	public Object getTarget() {
		return target;
	}

	@Override
	public void setTarget(Object obj) {
		target = obj;
	}

	protected Object invokeTarget(Object obj, Method method, Object[] args, MethodProxy proxy)
			throws Throwable {
		Object target = getTarget();
		if (target instanceof MethodInterceptor) {
			return ((MethodInterceptor) target).intercept(obj, method, args, proxy);
		}
		if (target instanceof InvocationHandler) {
			return ((InvocationHandler) target).invoke(proxy, method, args);
		}
		return proxy.invoke(target, args);
	}
}
