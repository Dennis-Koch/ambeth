package com.koch.ambeth.bytecode.core;

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
