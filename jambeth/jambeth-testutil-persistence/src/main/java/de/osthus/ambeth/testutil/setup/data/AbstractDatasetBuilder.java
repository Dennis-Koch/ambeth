package de.osthus.ambeth.testutil.setup.data;

import java.util.Collection;

import de.osthus.ambeth.collections.IdentityHashSet;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.IEntityFactory;
import de.osthus.ambeth.merge.IEntityMetaDataProvider;
import de.osthus.ambeth.merge.IMergeProcess;
import de.osthus.ambeth.security.ISecurityActivation;

public abstract class AbstractDatasetBuilder implements IDatasetBuilder
{
	@LogInstance
	private ILogger log;

	@Autowired
	protected IEntityFactory entityFactory;

	@Autowired
	protected IEntityMetaDataProvider entityMetaDataProvider;

	@Autowired
	protected IMergeProcess mergeProcess;

	@Autowired
	protected ISecurityActivation securityActivation;

	protected final ThreadLocal<Collection<Object>> initialTestDatasetTL = new ThreadLocal<Collection<Object>>();

	@Override
	public Collection<Object> buildDataset()
	{
		beforeBuildDataset();
		try
		{
			buildDatasetInternal();
			return initialTestDatasetTL.get();
		}
		finally
		{
			afterBuildDataset();
		}
	}

	protected abstract void buildDatasetInternal();

	private void beforeBuildDataset()
	{
		IdentityHashSet<Object> initialTestDataset = new IdentityHashSet<Object>();
		initialTestDatasetTL.set(initialTestDataset);
	}

	private void afterBuildDataset()
	{
		initialTestDatasetTL.remove();
	}

	protected <V> V createEntity(Class<V> entityType)
	{
		V entity = entityFactory.createEntity(entityType);
		initialTestDatasetTL.get().add(entity);
		return entity;
	}
}
