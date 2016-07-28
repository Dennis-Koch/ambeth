package de.osthus.ambeth.bytecode.behavior;

import java.util.List;

import de.osthus.ambeth.bytecode.EmbeddedEnhancementHint;
import de.osthus.ambeth.bytecode.EntityEnhancementHint;
import de.osthus.ambeth.bytecode.visitor.EntityMetaDataHolderVisitor;
import de.osthus.ambeth.bytecode.visitor.GetBaseTypeMethodCreator;
import de.osthus.ambeth.bytecode.visitor.InterfaceAdder;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.IEntityMetaDataProvider;
import de.osthus.ambeth.merge.model.IEntityMetaData;
import de.osthus.ambeth.propertychange.PropertyChangeEnhancementHint;
import de.osthus.ambeth.proxy.IEnhancedType;
import de.osthus.ambeth.proxy.IEntityMetaDataHolder;
import de.osthus.ambeth.repackaged.org.objectweb.asm.ClassVisitor;
import de.osthus.ambeth.repackaged.org.objectweb.asm.Type;

public class EnhancedTypeBehavior extends AbstractBehavior
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IEntityMetaDataProvider entityMetaDataProvider;

	@Override
	public Class<?>[] getEnhancements()
	{
		return new Class<?>[] { IEnhancedType.class, IEntityMetaDataHolder.class };
	}

	@Override
	public ClassVisitor extend(ClassVisitor visitor, IBytecodeBehaviorState state, List<IBytecodeBehavior> remainingPendingBehaviors,
			List<IBytecodeBehavior> cascadePendingBehaviors)
	{
		if ((state.getContext(EntityEnhancementHint.class) == null && state.getContext(EmbeddedEnhancementHint.class) == null)
				&& state.getContext(PropertyChangeEnhancementHint.class) == null)
		{
			return visitor;
		}
		if (state.getContext(EntityEnhancementHint.class) != null)
		{
			IEntityMetaData metaData = entityMetaDataProvider.getMetaData(state.getOriginalType());
			visitor = new InterfaceAdder(visitor, Type.getInternalName(IEntityMetaDataHolder.class));
			visitor = new EntityMetaDataHolderVisitor(visitor, metaData);
		}
		visitor = new InterfaceAdder(visitor, Type.getInternalName(IEnhancedType.class));
		visitor = new GetBaseTypeMethodCreator(visitor);
		return visitor;
	}
}
