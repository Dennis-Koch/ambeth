package de.osthus.ambeth.compositeid;

import java.util.List;

import de.osthus.ambeth.bytecode.behavior.AbstractBehavior;
import de.osthus.ambeth.bytecode.behavior.IBytecodeBehavior;
import de.osthus.ambeth.bytecode.behavior.IBytecodeBehaviorState;
import de.osthus.ambeth.bytecode.visitor.CompositeIdCreator;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.repackaged.org.objectweb.asm.ClassVisitor;

public class CompositeIdBehavior extends AbstractBehavior
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	public ClassVisitor extend(ClassVisitor visitor, IBytecodeBehaviorState state, List<IBytecodeBehavior> remainingPendingBehaviors,
			List<IBytecodeBehavior> cascadePendingBehaviors)
	{
		if (state.getContext(CompositeIdEnhancementHint.class) == null)
		{
			return visitor;
		}
		visitor = new CompositeIdCreator(visitor);
		return visitor;
	}
}
