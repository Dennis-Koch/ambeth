package de.osthus.ambeth.bytecode.behavior;

import java.util.List;

import de.osthus.ambeth.repackaged.org.objectweb.asm.ClassVisitor;

public interface WaitForApplyBehaviorDelegate
{
	ClassVisitor extend(ClassVisitor visitor, IBytecodeBehaviorState state, List<IBytecodeBehavior> remainingPendingBehaviors,
			List<IBytecodeBehavior> cascadePendingBehaviors);
}
