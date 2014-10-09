package de.osthus.ambeth.bytecode.behavior;

import java.util.List;

import de.osthus.ambeth.bytecode.visitor.ObjRefVisitor;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.IEntityMetaDataProvider;
import de.osthus.ambeth.merge.model.IEntityMetaData;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.metadata.ObjRefEnhancementHint;
import de.osthus.ambeth.repackaged.org.objectweb.asm.ClassVisitor;

public class ObjRefBehavior extends AbstractBehavior
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IEntityMetaDataProvider entityMetaDataProvider;

	@Override
	public Class<?>[] getEnhancements()
	{
		return new Class<?>[] { IObjRef.class };
	}

	@Override
	public ClassVisitor extend(ClassVisitor visitor, IBytecodeBehaviorState state, List<IBytecodeBehavior> remainingPendingBehaviors,
			List<IBytecodeBehavior> cascadePendingBehaviors)
	{
		ObjRefEnhancementHint memberHint = state.getContext(ObjRefEnhancementHint.class);
		if (memberHint == null)
		{
			return visitor;
		}
		IEntityMetaData metaData = entityMetaDataProvider.getMetaData(memberHint.getEntityType());
		visitor = new ObjRefVisitor(visitor, metaData, memberHint.getIdIndex());
		return visitor;
	}
}
