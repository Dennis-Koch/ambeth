package de.osthus.ambeth.bytecode.core;

import java.util.List;

import de.osthus.ambeth.bytecode.IBytecodeEnhancer;
import de.osthus.ambeth.bytecode.IEnhancementHint;
import de.osthus.ambeth.bytecode.behavior.IBytecodeBehavior;
import de.osthus.ambeth.collections.HashMap;

public interface IBytecodeStore
{
	HashMap<BytecodeStoreKey, BytecodeStoreItem> loadEnhancedTypes(IBytecodeEnhancer bytecodeEnhancer, IBytecodeBehavior[] behaviors);

	void storeEnhancedType(BytecodeEnhancer bytecodeEnhancer, IBytecodeBehavior[] behaviors, Class<?> baseType, IEnhancementHint hint, Class<?> enhancedType,
			List<Class<?>> enhancedTypesPipeline);
}