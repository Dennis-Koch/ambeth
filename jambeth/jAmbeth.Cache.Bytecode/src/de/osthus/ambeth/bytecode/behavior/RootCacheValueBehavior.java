package de.osthus.ambeth.bytecode.behavior;

import java.util.List;

import de.osthus.ambeth.bytecode.visitor.RootCacheValueVisitor;
import de.osthus.ambeth.cache.rootcachevalue.RootCacheValueEnhancementHint;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.IEntityMetaDataProvider;
import de.osthus.ambeth.merge.model.IEntityMetaData;
import de.osthus.ambeth.repackaged.org.objectweb.asm.ClassVisitor;

public class RootCacheValueBehavior extends AbstractBehavior
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
		final RootCacheValueEnhancementHint hint = state.getContext(RootCacheValueEnhancementHint.class);
		if (hint == null)
		{
			return visitor;
		}
		IEntityMetaData metaData = entityMetaDataProvider.getMetaData(hint.getEntityType());
		visitor = new RootCacheValueVisitor(visitor, metaData);
		return visitor;
	}
}
