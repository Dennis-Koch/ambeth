package de.osthus.ambeth.bytecode.behavior;

import java.util.List;

import de.osthus.ambeth.bytecode.EntityEnhancementHint;
import de.osthus.ambeth.bytecode.visitor.PublicConstructorVisitor;
import de.osthus.ambeth.repackaged.org.objectweb.asm.ClassVisitor;

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
