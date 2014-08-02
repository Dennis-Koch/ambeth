package de.osthus.ambeth.bytecode.behavior;

import java.util.List;

import de.osthus.ambeth.bytecode.EmbeddedEnhancementHint;
import de.osthus.ambeth.bytecode.util.EntityUtil;
import de.osthus.ambeth.bytecode.visitor.InitializeEmbeddedMemberVisitor;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.IEntityMetaDataProvider;
import de.osthus.ambeth.merge.model.IEntityMetaData;
import de.osthus.ambeth.repackaged.org.objectweb.asm.ClassVisitor;
import de.osthus.ambeth.typeinfo.IPropertyInfoProvider;

public class InitializeEmbeddedMemberBehavior extends AbstractBehavior
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IEntityMetaDataProvider entityMetaDataProvider;

	@Autowired
	protected IPropertyInfoProvider propertyInfoProvider;

	@Override
	public ClassVisitor extend(ClassVisitor visitor, IBytecodeBehaviorState state, List<IBytecodeBehavior> remainingPendingBehaviors,
			List<IBytecodeBehavior> cascadePendingBehaviors)
	{
		Class<?> entityType = EntityUtil.getEntityType(state.getContext());
		if (entityType == null)
		{
			return visitor;
		}
		final IEntityMetaData metaData = entityMetaDataProvider.getMetaData(entityType);
		String memberPath = EmbeddedEnhancementHint.getMemberPath(state.getContext());
		visitor = new InitializeEmbeddedMemberVisitor(visitor, metaData, memberPath, propertyInfoProvider);
		return visitor;
	}
}
