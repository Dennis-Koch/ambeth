package de.osthus.ambeth.bytecode.behavior;

import java.util.List;

import de.osthus.ambeth.bytecode.IEnhancementHint;
import de.osthus.ambeth.repackaged.org.objectweb.asm.ClassVisitor;

public interface IBytecodeBehavior
{
	ClassVisitor extend(ClassVisitor visitor, IBytecodeBehaviorState state, List<IBytecodeBehavior> remainingPendingBehaviors,
			List<IBytecodeBehavior> cascadePendingBehaviors);

	Class<?>[] getEnhancements();

	Class<?> getTypeToExtendFrom(Class<?> originalType, Class<?> currentType, IEnhancementHint hint);
}
