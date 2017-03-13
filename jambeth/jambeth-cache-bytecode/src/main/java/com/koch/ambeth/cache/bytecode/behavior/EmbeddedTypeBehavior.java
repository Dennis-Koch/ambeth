package com.koch.ambeth.cache.bytecode.behavior;

import java.util.List;

import org.objectweb.asm.ClassVisitor;

import com.koch.ambeth.bytecode.behavior.AbstractBehavior;
import com.koch.ambeth.bytecode.behavior.IBytecodeBehavior;
import com.koch.ambeth.bytecode.behavior.IBytecodeBehaviorState;
import com.koch.ambeth.bytecode.visitor.InterfaceAdder;
import com.koch.ambeth.cache.bytecode.visitor.EmbeddedTypeVisitor;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.merge.bytecode.EmbeddedEnhancementHint;
import com.koch.ambeth.util.model.IEmbeddedType;

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
