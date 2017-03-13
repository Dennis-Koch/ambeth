package com.koch.ambeth.cache.bytecode.behavior;

import java.util.List;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Type;

import com.koch.ambeth.bytecode.behavior.AbstractBehavior;
import com.koch.ambeth.bytecode.behavior.IBytecodeBehavior;
import com.koch.ambeth.bytecode.behavior.IBytecodeBehaviorState;
import com.koch.ambeth.bytecode.visitor.InterfaceAdder;
import com.koch.ambeth.cache.bytecode.visitor.EntityMetaDataHolderVisitor;
import com.koch.ambeth.cache.bytecode.visitor.GetBaseTypeMethodCreator;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.merge.bytecode.EmbeddedEnhancementHint;
import com.koch.ambeth.merge.bytecode.EntityEnhancementHint;
import com.koch.ambeth.merge.propertychange.PropertyChangeEnhancementHint;
import com.koch.ambeth.merge.proxy.IEnhancedType;
import com.koch.ambeth.merge.proxy.IEntityMetaDataHolder;
import com.koch.ambeth.service.merge.IEntityMetaDataProvider;
import com.koch.ambeth.service.merge.model.IEntityMetaData;

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
