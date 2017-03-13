package com.koch.ambeth.cache.bytecode.behavior;

import java.util.List;

import org.objectweb.asm.ClassVisitor;

import com.koch.ambeth.bytecode.abstractobject.ImplementAbstractObjectEnhancementHint;
import com.koch.ambeth.bytecode.behavior.AbstractBehavior;
import com.koch.ambeth.bytecode.behavior.IBytecodeBehavior;
import com.koch.ambeth.bytecode.behavior.IBytecodeBehaviorState;
import com.koch.ambeth.bytecode.visitor.InterfaceAdder;
import com.koch.ambeth.cache.bytecode.visitor.DefaultPropertiesMethodVisitor;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.merge.bytecode.EmbeddedEnhancementHint;
import com.koch.ambeth.merge.bytecode.EntityEnhancementHint;
import com.koch.ambeth.merge.propertychange.PropertyChangeEnhancementHint;
import com.koch.ambeth.util.collections.HashMap;
import com.koch.ambeth.util.objectcollector.IThreadLocalObjectCollector;
import com.koch.ambeth.util.typeinfo.IPropertyInfo;
import com.koch.ambeth.util.typeinfo.IPropertyInfoProvider;

public class DefaultPropertiesBehavior extends AbstractBehavior
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IThreadLocalObjectCollector objectCollector;

	@Autowired
	protected IPropertyInfoProvider propertyInfoProvider;

	@Override
	public ClassVisitor extend(ClassVisitor visitor, IBytecodeBehaviorState state, List<IBytecodeBehavior> remainingPendingBehaviors,
			List<IBytecodeBehavior> cascadePendingBehaviors)
	{
		if (state.getContext(EntityEnhancementHint.class) == null && state.getContext(EmbeddedEnhancementHint.class) == null
				&& state.getContext(ImplementAbstractObjectEnhancementHint.class) == null && state.getContext(PropertyChangeEnhancementHint.class) == null)
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
		visitor = new DefaultPropertiesMethodVisitor(visitor, allProperties.toArray(IPropertyInfo.class), objectCollector);
		if (state.getOriginalType().isInterface())
		{
			visitor = new InterfaceAdder(visitor, state.getOriginalType());
		}
		return visitor;
	}
}
