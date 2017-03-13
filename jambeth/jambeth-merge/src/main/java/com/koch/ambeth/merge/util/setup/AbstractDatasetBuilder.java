package com.koch.ambeth.merge.util.setup;

import java.util.Collection;

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.merge.IEntityFactory;
import com.koch.ambeth.merge.IMergeProcess;
import com.koch.ambeth.service.merge.IEntityMetaDataProvider;
import com.koch.ambeth.util.collections.IdentityHashSet;

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

	protected void beforeBuildDataset()
	{
		IdentityHashSet<Object> initialTestDataset = new IdentityHashSet<Object>();
		initialTestDatasetTL.set(initialTestDataset);
	}

	protected void afterBuildDataset()
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
