package de.osthus.ambeth.bytecode.behavior;

import java.util.List;

import de.osthus.ambeth.annotation.EntityEqualsAspect;
import de.osthus.ambeth.bytecode.EntityEnhancementHint;
import de.osthus.ambeth.bytecode.visitor.EntityEqualsVisitor;
import de.osthus.ambeth.bytecode.visitor.GetBaseTypeMethodCreator;
import de.osthus.ambeth.bytecode.visitor.GetIdMethodCreator;
import de.osthus.ambeth.bytecode.visitor.InterfaceAdder;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.IEntityMetaDataProvider;
import de.osthus.ambeth.merge.model.IEntityMetaData;
import de.osthus.ambeth.proxy.IEntityEquals;
import de.osthus.ambeth.repackaged.org.objectweb.asm.ClassVisitor;
import de.osthus.ambeth.repackaged.org.objectweb.asm.Type;
import de.osthus.ambeth.util.IPrintable;

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
