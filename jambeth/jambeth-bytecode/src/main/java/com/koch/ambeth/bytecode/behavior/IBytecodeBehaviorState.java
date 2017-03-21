package com.koch.ambeth.bytecode.behavior;

/*-
 * #%L
 * jambeth-bytecode
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

import org.objectweb.asm.Type;

import com.koch.ambeth.bytecode.FieldInstance;
import com.koch.ambeth.bytecode.MethodInstance;
import com.koch.ambeth.bytecode.PropertyInstance;
import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.ioc.bytecode.IEnhancementHint;

public interface IBytecodeBehaviorState {
	Class<?> getOriginalType();

	Class<?> getCurrentType();

	Type getNewType();

	IServiceContext getBeanContext();

	IEnhancementHint getContext();

	<T extends IEnhancementHint> T getContext(Class<T> contextType);

	PropertyInstance getProperty(String propertyName, Class<?> propertyType);

	PropertyInstance getProperty(String propertyName, Type propertyType);

	MethodInstance[] getAlreadyImplementedMethodsOnNewType();

	FieldInstance getAlreadyImplementedField(String fieldName);

	boolean hasMethod(MethodInstance method);

	boolean isMethodAlreadyImplementedOnNewType(MethodInstance method);
}
