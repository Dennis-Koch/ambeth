package de.osthus.ambeth.expr.bytecode;

import java.util.List;

import de.osthus.ambeth.bytecode.EmbeddedEnhancementHint;
import de.osthus.ambeth.bytecode.EntityEnhancementHint;
import de.osthus.ambeth.bytecode.abstractobject.ImplementAbstractObjectEnhancementHint;
import de.osthus.ambeth.bytecode.behavior.AbstractBehavior;
import de.osthus.ambeth.bytecode.behavior.IBytecodeBehavior;
import de.osthus.ambeth.bytecode.behavior.IBytecodeBehaviorState;
import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.repackaged.org.objectweb.asm.ClassVisitor;
import de.osthus.ambeth.typeinfo.IPropertyInfo;
import de.osthus.ambeth.typeinfo.IPropertyInfoProvider;

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

		HashMap<String, IPropertyInfo> allProperties = new HashMap<String, IPropertyInfo>();
		IPropertyInfo[] properties = propertyInfoProvider.getProperties(state.getCurrentType());
		for (IPropertyInfo pi : properties)
		{
			allProperties.put(pi.getName(), pi);
		}
		properties = propertyInfoProvider.getProperties(state.getOriginalType());
		for (IPropertyInfo pi : properties)
		{
			// Only add property if it is not already declared by the current type
			allProperties.putIfNotExists(pi.getName(), pi);
		}
		visitor = new PropertyExpressionClassVisitor(visitor, allProperties.toArray(IPropertyInfo.class));
		return visitor;
	}
}
