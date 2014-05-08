package de.osthus.ambeth.bytecode.behavior;

import java.util.List;

import de.osthus.ambeth.bytecode.EmbeddedEnhancementHint;
import de.osthus.ambeth.bytecode.visitor.EmbeddedTypeVisitor;
import de.osthus.ambeth.bytecode.visitor.InterfaceAdder;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.model.IEmbeddedType;
import de.osthus.ambeth.repackaged.org.objectweb.asm.ClassVisitor;

public class EmbeddedTypeBehavior extends AbstractBehavior
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	public Class<?>[] getEnhancements()
	{
		return new Class<?>[] { IEmbeddedType.class };
	}

	@Override
	public ClassVisitor extend(ClassVisitor visitor, IBytecodeBehaviorState state, List<IBytecodeBehavior> remainingPendingBehaviors,
			List<IBytecodeBehavior> cascadePendingBehaviors)
	{
		if (state.getContext(EmbeddedEnhancementHint.class) == null)
		{
			return visitor;
		}
		visitor = new InterfaceAdder(visitor, IEmbeddedType.class);
		visitor = new EmbeddedTypeVisitor(visitor);
		return visitor;
	}
}
