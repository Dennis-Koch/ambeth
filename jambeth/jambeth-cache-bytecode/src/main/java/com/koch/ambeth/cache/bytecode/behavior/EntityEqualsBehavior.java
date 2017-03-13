package com.koch.ambeth.cache.bytecode.behavior;

import java.util.List;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Type;

import com.koch.ambeth.bytecode.behavior.AbstractBehavior;
import com.koch.ambeth.bytecode.behavior.IBytecodeBehavior;
import com.koch.ambeth.bytecode.behavior.IBytecodeBehaviorState;
import com.koch.ambeth.bytecode.visitor.InterfaceAdder;
import com.koch.ambeth.cache.bytecode.visitor.EntityEqualsVisitor;
import com.koch.ambeth.cache.bytecode.visitor.GetBaseTypeMethodCreator;
import com.koch.ambeth.cache.bytecode.visitor.GetIdMethodCreator;
import com.koch.ambeth.cache.proxy.IEntityEquals;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.merge.bytecode.EntityEnhancementHint;
import com.koch.ambeth.service.merge.IEntityMetaDataProvider;
import com.koch.ambeth.service.merge.model.IEntityMetaData;
import com.koch.ambeth.util.IPrintable;
import com.koch.ambeth.util.annotation.EntityEqualsAspect;

public class EntityEqualsBehavior extends AbstractBehavior
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IEntityMetaDataProvider entityMetaDataProvider;

	@Override
	public Class<?>[] getEnhancements()
	{
		return new Class<?>[] { IEntityEquals.class };
	}

	@Override
	public ClassVisitor extend(ClassVisitor visitor, IBytecodeBehaviorState state, List<IBytecodeBehavior> remainingPendingBehaviors,
			List<IBytecodeBehavior> cascadePendingBehaviors)
	{
		if (state.getContext(EntityEnhancementHint.class) == null)
		{
			return visitor;
		}
		IEntityMetaData metaData = entityMetaDataProvider.getMetaData(state.getOriginalType(), true);
		if (metaData == null)
		{
			return visitor;
		}
		if (!isAnnotationPresent(state.getCurrentType(), EntityEqualsAspect.class))
		{
			return visitor;
		}
		visitor = new InterfaceAdder(visitor, Type.getInternalName(IEntityEquals.class), Type.getInternalName(IPrintable.class));
		visitor = new GetIdMethodCreator(visitor, metaData);
		visitor = new GetBaseTypeMethodCreator(visitor);
		visitor = new EntityEqualsVisitor(visitor);
		return visitor;
	}
}
