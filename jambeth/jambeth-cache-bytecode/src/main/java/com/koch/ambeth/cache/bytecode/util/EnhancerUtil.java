package com.koch.ambeth.cache.bytecode.util;

/*-
 * #%L
 * jambeth-cache-bytecode
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

import com.koch.ambeth.bytecode.MethodInstance;
import com.koch.ambeth.bytecode.PropertyInstance;
import com.koch.ambeth.bytecode.behavior.BytecodeBehaviorState;
import com.koch.ambeth.bytecode.behavior.IBytecodeBehaviorState;
import com.koch.ambeth.cache.ValueHolderIEC;
import com.koch.ambeth.util.ReflectUtil;

public class EnhancerUtil {
	public static final MethodInstance getSuperSetter(PropertyInstance propertyInfo) {
		IBytecodeBehaviorState state = BytecodeBehaviorState.getState();
		Class<?> superType = state.getCurrentType();
		MethodInstance setter = propertyInfo.getSetter();
		java.lang.reflect.Method superSetter = ReflectUtil.getDeclaredMethod(false, superType,
				setter.getReturnType(), setter.getName(), setter.getParameters());
		return new MethodInstance(superSetter);
	}

	public static final MethodInstance getSuperGetter(PropertyInstance propertyInfo) {
		IBytecodeBehaviorState state = BytecodeBehaviorState.getState();
		Class<?> superType = state.getCurrentType();
		MethodInstance getter = propertyInfo.getGetter();
		java.lang.reflect.Method superGetter = ReflectUtil.getDeclaredMethod(true, superType,
				getter.getReturnType(), getGetterNameOfRelationPropertyWithNoInit(propertyInfo.getName()),
				getter.getParameters());
		if (superGetter == null) {
			// not a relation -> no lazy loading
			superGetter =
					ReflectUtil.getDeclaredMethod(false, superType, propertyInfo.getGetter().getReturnType(),
							propertyInfo.getGetter().getName(), propertyInfo.getGetter().getParameters());
		}
		return new MethodInstance(superGetter);
	}

	public static final String getGetterNameOfRelationPropertyWithNoInit(String propertyName) {
		return "get" + propertyName + ValueHolderIEC.getNoInitSuffix();
	}
}
