package de.osthus.ambeth.bytecode.behavior;

import java.util.List;

import de.osthus.ambeth.bytecode.visitor.CacheMapEntryVisitor;
import de.osthus.ambeth.cache.collections.CacheMapEntryEnhancementHint;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.IEntityMetaDataProvider;
import de.osthus.ambeth.merge.model.IEntityMetaData;
import de.osthus.ambeth.repackaged.org.objectweb.asm.ClassVisitor;

public class CacheMapEntryBehavior extends AbstractBehavior
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
		final CacheMapEntryEnhancementHint hint = state.getContext(CacheMapEntryEnhancementHint.class);
		if (hint == null)
		{
			return visitor;
		}
		IEntityMetaData metaData = entityMetaDataProvider.getMetaData(hint.getEntityType());
		visitor = new CacheMapEntryVisitor(visitor, metaData, hint.getIdIndex());
		return visitor;
	}
}
