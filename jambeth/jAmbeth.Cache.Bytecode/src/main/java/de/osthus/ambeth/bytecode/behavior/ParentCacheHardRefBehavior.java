package de.osthus.ambeth.bytecode.behavior;

import java.util.List;

import de.osthus.ambeth.bytecode.EntityEnhancementHint;
import de.osthus.ambeth.bytecode.visitor.InterfaceAdder;
import de.osthus.ambeth.bytecode.visitor.ParentCacheHardRefVisitor;
import de.osthus.ambeth.cache.IParentCacheValueHardRef;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.IEntityMetaDataProvider;
import de.osthus.ambeth.merge.model.IEntityMetaData;
import de.osthus.ambeth.repackaged.org.objectweb.asm.ClassVisitor;
import de.osthus.ambeth.repackaged.org.objectweb.asm.Type;

public class ParentCacheHardRefBehavior extends AbstractBehavior
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IEntityMetaDataProvider entityMetaDataProvider;

	@Override
	public Class<?>[] getEnhancements()
	{
		return new Class<?>[] { IParentCacheValueHardRef.class };
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
		visitor = new InterfaceAdder(visitor, Type.getInternalName(IParentCacheValueHardRef.class));
		visitor = new ParentCacheHardRefVisitor(visitor);
		return visitor;
	}
}
