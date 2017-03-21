package com.koch.ambeth.util.proxy;

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

import com.koch.ambeth.util.IDisposable;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;

import net.sf.cglib.proxy.MethodProxy;

public class TargetingInterceptor extends AbstractSimpleInterceptor
{
	protected ITargetProvider targetProvider;

	public void setTargetProvider(ITargetProvider targetProvider)
	{
		this.targetProvider = targetProvider;
	}

	public ITargetProvider getTargetProvider()
	{
		return targetProvider;
	}

	@Override
	protected Object interceptIntern(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable
	{
		if (obj instanceof IDisposable && method.getName().equals("dispose") && method.getParameterTypes().length == 0)
		{
			return null;
		}
		Object target = targetProvider.getTarget();
		if (target == null)
		{
			throw new NullPointerException("Object reference has to be valid. TargetProvider: " + targetProvider);
		}
		try
		{
			return proxy.invoke(target, args);
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e, method.getExceptionTypes());
		}
	}
}
