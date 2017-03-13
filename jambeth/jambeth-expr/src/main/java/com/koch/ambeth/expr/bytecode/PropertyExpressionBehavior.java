package com.koch.ambeth.expr.bytecode;

import java.util.List;

import org.objectweb.asm.ClassVisitor;

import com.koch.ambeth.bytecode.abstractobject.ImplementAbstractObjectEnhancementHint;
import com.koch.ambeth.bytecode.behavior.AbstractBehavior;
import com.koch.ambeth.bytecode.behavior.IBytecodeBehavior;
import com.koch.ambeth.bytecode.behavior.IBytecodeBehaviorState;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.merge.bytecode.EmbeddedEnhancementHint;
import com.koch.ambeth.merge.bytecode.EntityEnhancementHint;
import com.koch.ambeth.util.typeinfo.IPropertyInfoProvider;

public class PropertyExpressionBehavior extends AbstractBehavior
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IPropertyInfoProvider propertyInfoProvider;

	@Override
	public ClassVisitor extend(ClassVisitor visitor, IBytecodeBehaviorState state, List<IBytecodeBehavior> remainingPendingBehaviors,
			List<IBytecodeBehavior> cascadePendingBehaviors)
	{
		if (state.getContext(EntityEnhancementHint.class) == null && state.getContext(EmbeddedEnhancementHint.class) == null
				&& state.getContext(ImplementAbstractObjectEnhancementHint.class) == null)
		{
			return visitor;
		}
		cascadePendingBehaviors.addAll(0, remainingPendingBehaviors);
		remainingPendingBehaviors.clear();

		visitor = new PropertyExpressionClassVisitor(visitor);
		return visitor;
	}
}
