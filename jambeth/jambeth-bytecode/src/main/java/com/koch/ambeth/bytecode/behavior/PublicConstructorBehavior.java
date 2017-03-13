package com.koch.ambeth.bytecode.behavior;

import java.util.List;

import org.objectweb.asm.ClassVisitor;

import com.koch.ambeth.bytecode.visitor.PublicConstructorVisitor;
import com.koch.ambeth.merge.bytecode.EntityEnhancementHint;

public class PublicConstructorBehavior extends AbstractBehavior
{
	@Override
	public ClassVisitor extend(ClassVisitor visitor, IBytecodeBehaviorState state, List<IBytecodeBehavior> remainingPendingBehaviors,
			List<IBytecodeBehavior> cascadePendingBehaviors)
	{
		if (state.getContext(EntityEnhancementHint.class) == null)
		{
			return visitor;
		}
		visitor = new PublicConstructorVisitor(visitor);
		return visitor;
	}
}
