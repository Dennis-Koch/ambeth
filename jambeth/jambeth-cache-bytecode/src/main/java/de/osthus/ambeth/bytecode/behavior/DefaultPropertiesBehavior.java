package de.osthus.ambeth.bytecode.behavior;

import java.util.List;

import de.osthus.ambeth.bytecode.EmbeddedEnhancementHint;
import de.osthus.ambeth.bytecode.EntityEnhancementHint;
import de.osthus.ambeth.bytecode.abstractobject.ImplementAbstractObjectEnhancementHint;
import de.osthus.ambeth.bytecode.visitor.DefaultPropertiesMethodVisitor;
import de.osthus.ambeth.bytecode.visitor.InterfaceAdder;
import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.objectcollector.IThreadLocalObjectCollector;
import de.osthus.ambeth.propertychange.PropertyChangeEnhancementHint;
import de.osthus.ambeth.repackaged.org.objectweb.asm.ClassVisitor;
import de.osthus.ambeth.typeinfo.IPropertyInfo;
import de.osthus.ambeth.typeinfo.IPropertyInfoProvider;

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
