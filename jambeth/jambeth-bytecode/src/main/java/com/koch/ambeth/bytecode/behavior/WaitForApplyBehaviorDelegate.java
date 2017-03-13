package com.koch.ambeth.bytecode.behavior;

import java.util.List;

import org.objectweb.asm.ClassVisitor;

public interface WaitForApplyBehaviorDelegate
{
	ClassVisitor extend(ClassVisitor visitor, IBytecodeBehaviorState state, List<IBytecodeBehavior> remainingPendingBehaviors,
			List<IBytecodeBehavior> cascadePendingBehaviors);
}
