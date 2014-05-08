package de.osthus.ambeth.bytecode.behavior;

import java.util.List;

import de.osthus.ambeth.bytecode.EmbeddedEnhancementHint;
import de.osthus.ambeth.bytecode.visitor.PublicEmbeddedConstructorVisitor;
import de.osthus.ambeth.repackaged.org.objectweb.asm.ClassVisitor;

public class PublicEmbeddedConstructorBehavior extends AbstractBehavior
{
	@Override
	public ClassVisitor extend(ClassVisitor visitor, IBytecodeBehaviorState state, List<IBytecodeBehavior> remainingPendingBehaviors,
			List<IBytecodeBehavior> cascadePendingBehaviors)
	{
		if (state.getContext(EmbeddedEnhancementHint.class) == null)
		{
			return visitor;
		}
		visitor = new PublicEmbeddedConstructorVisitor(visitor);
		return visitor;
	}
}
