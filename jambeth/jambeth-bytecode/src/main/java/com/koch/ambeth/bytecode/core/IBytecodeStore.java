package com.koch.ambeth.bytecode.core;

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

import java.util.List;

import com.koch.ambeth.bytecode.behavior.IBytecodeBehavior;
import com.koch.ambeth.ioc.bytecode.IBytecodeEnhancer;
import com.koch.ambeth.ioc.bytecode.IEnhancementHint;
import com.koch.ambeth.util.collections.IMap;

public interface IBytecodeStore {
	IMap<BytecodeStoreKey, BytecodeStoreItem> loadEnhancedTypes(IBytecodeEnhancer bytecodeEnhancer,
			IBytecodeBehavior[] behaviors);

	void storeEnhancedType(BytecodeEnhancer bytecodeEnhancer, IBytecodeBehavior[] behaviors,
			Class<?> baseType, IEnhancementHint hint, Class<?> enhancedType,
			List<Class<?>> enhancedTypesPipeline, ClassLoader classLoader);
}
