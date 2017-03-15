package com.koch.ambeth.bytecode.behavior;

import java.util.List;

import org.objectweb.asm.ClassVisitor;

import com.koch.ambeth.ioc.bytecode.IEnhancementHint;

public interface IBytecodeBehavior {
	ClassVisitor extend(ClassVisitor visitor, IBytecodeBehaviorState state,
			List<IBytecodeBehavior> remainingPendingBehaviors,
			List<IBytecodeBehavior> cascadePendingBehaviors);

	Class<?>[] getEnhancements();

	Class<?> getTypeToExtendFrom(Class<?> originalType, Class<?> currentType, IEnhancementHint hint);
}
