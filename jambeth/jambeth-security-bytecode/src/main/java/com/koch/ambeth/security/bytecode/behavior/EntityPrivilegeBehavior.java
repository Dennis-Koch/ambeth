package com.koch.ambeth.security.bytecode.behavior;

import java.util.List;

import org.objectweb.asm.ClassVisitor;

import com.koch.ambeth.bytecode.behavior.AbstractBehavior;
import com.koch.ambeth.bytecode.behavior.IBytecodeBehavior;
import com.koch.ambeth.bytecode.behavior.IBytecodeBehaviorState;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.security.bytecode.visitor.EntityPrivilegeVisitor;
import com.koch.ambeth.security.privilege.factory.EntityPrivilegeEnhancementHint;
import com.koch.ambeth.service.merge.IEntityMetaDataProvider;
import com.koch.ambeth.service.merge.model.IEntityMetaData;

public class EntityPrivilegeBehavior extends AbstractBehavior
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
		final EntityPrivilegeEnhancementHint hint = state.getContext(EntityPrivilegeEnhancementHint.class);
		if (hint == null)
		{
			return visitor;
		}
		IEntityMetaData metaData = entityMetaDataProvider.getMetaData(hint.getEntityType());
		visitor = new EntityPrivilegeVisitor(visitor, metaData, hint.isCreate(), hint.isRead(), hint.isUpdate(), hint.isDelete(), hint.isExecute());
		return visitor;
	}
}
