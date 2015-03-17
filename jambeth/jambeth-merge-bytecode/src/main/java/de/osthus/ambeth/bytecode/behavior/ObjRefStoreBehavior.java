package de.osthus.ambeth.bytecode.behavior;

import java.util.List;

import de.osthus.ambeth.bytecode.visitor.ObjRefStoreVisitor;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.IEntityMetaDataProvider;
import de.osthus.ambeth.merge.model.IEntityMetaData;
import de.osthus.ambeth.objrefstore.ObjRefStore;
import de.osthus.ambeth.objrefstore.ObjRefStoreEnhancementHint;
import de.osthus.ambeth.repackaged.org.objectweb.asm.ClassVisitor;

public class ObjRefStoreBehavior extends AbstractBehavior
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IEntityMetaDataProvider entityMetaDataProvider;

	@Override
	public Class<?>[] getEnhancements()
	{
		return new Class<?>[] { ObjRefStore.class };
	}

	@Override
	public ClassVisitor extend(ClassVisitor visitor, IBytecodeBehaviorState state, List<IBytecodeBehavior> remainingPendingBehaviors,
			List<IBytecodeBehavior> cascadePendingBehaviors)
	{
		ObjRefStoreEnhancementHint memberHint = state.getContext(ObjRefStoreEnhancementHint.class);
		if (memberHint == null)
		{
			return visitor;
		}
		IEntityMetaData metaData = entityMetaDataProvider.getMetaData(memberHint.getEntityType());
		visitor = new ObjRefStoreVisitor(visitor, metaData, memberHint.getIdIndex());
		return visitor;
	}
}
