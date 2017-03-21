package com.koch.ambeth.util;

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
import java.lang.reflect.Modifier;
import java.util.Arrays;

import com.koch.ambeth.util.exception.RuntimeExceptionUtil;

public class DelegateFactory implements IDelegateFactory
{
	public static class Delegate implements IDelegate
	{
		protected final Object target;

		protected final Method targetMethod;

		public Delegate(Object target, Method targetMethod)
		{
			this.target = target;
			this.targetMethod = targetMethod;
		}

		@Override
		public Object invoke(Object... args)
		{
			try
			{
				return targetMethod.invoke(target, args);
			}
			catch (Throwable e)
			{
				throw RuntimeExceptionUtil.mask(e);
			}
		}
	}

	@Override
	public IDelegate createDelegate(Class<?> delegateType, Object target, String methodName)
	{
		ParamChecker.assertParamNotNull(delegateType, "delegateType");
		ParamChecker.assertParamNotNull(target, "target");
		ParamChecker.assertParamNotNull(methodName, "methodName");
		Method[] methods = ReflectUtil.getMethods(delegateType);
		if (methods.length != 1)
		{
			throw new IllegalArgumentException("Invalid delegate type: " + delegateType);
		}
		Method delegateMethod = methods[0];
		Class<?>[] delegateParams = delegateMethod.getParameterTypes();
		Class<?> targetType = target.getClass();
		Method[] targetMethods = ReflectUtil.getDeclaredMethods(targetType);
		Method methodOnTarget = null;
		for (int a = targetMethods.length; a-- > 0;)
		{
			Method targetMethod = targetMethods[a];
			int modifiers = targetMethod.getModifiers();
			if (Modifier.isAbstract(modifiers) || Modifier.isStatic(modifiers))
			{
				continue;
			}
			if (!targetMethod.getName().equals(methodName))
			{
				continue;
			}
			Class<?>[] targetParams = targetMethod.getParameterTypes();
			if (!Arrays.equals(delegateParams, targetParams))
			{
				continue;
			}
			if (methodOnTarget == null)
			{
				methodOnTarget = targetMethod;
			}
			else
			{
				throw new IllegalArgumentException("Illegal target for delegate: Method '" + methodName + "' is overloaded on type '" + targetType.getName()
						+ "'. This is not supported");
			}
		}
		if (methodOnTarget == null)
		{
			throw new IllegalArgumentException("Illegal target for delegate: No suitable method found with name '" + methodName + "' on target type '"
					+ targetType.getName() + "' which matches parameter signature of delegate type '" + delegateMethod + "'");
		}
		methodOnTarget.setAccessible(true);

		return new Delegate(target, methodOnTarget);
	}
}
