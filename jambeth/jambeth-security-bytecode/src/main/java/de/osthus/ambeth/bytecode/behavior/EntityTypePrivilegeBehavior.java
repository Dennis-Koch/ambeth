package de.osthus.ambeth.bytecode.behavior;

import java.util.List;

import de.osthus.ambeth.bytecode.visitor.EntityTypePrivilegeVisitor;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.IEntityMetaDataProvider;
import de.osthus.ambeth.merge.model.IEntityMetaData;
import de.osthus.ambeth.privilege.bytecode.collections.EntityTypePrivilegeEnhancementHint;
import de.osthus.ambeth.repackaged.org.objectweb.asm.ClassVisitor;

public class EntityTypePrivilegeBehavior extends AbstractBehavior
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IEntityMetaDataProvider entityMetaDataProvider;

	@Override
	public ClassVisitor extend(ClassVisitor visitor, IBytecodeBehaviorState state, List<IBytecodeBehavior> remainingPendingBehaviors,
			List<IBytecodeBehavior> cascadePendingBehaviors)
	{
		final EntityTypePrivilegeEnhancementHint hint = state.getContext(EntityTypePrivilegeEnhancementHint.class);
		if (hint == null)
		{
			return visitor;
		}
		IEntityMetaData metaData = entityMetaDataProvider.getMetaData(hint.getEntityType());
		visitor = new EntityTypePrivilegeVisitor(visitor, metaData, hint.isCreate(), hint.isRead(), hint.isUpdate(), hint.isDelete(), hint.isExecute());
		return visitor;
	}
}
