package de.osthus.ambeth.util.setup;

import java.util.Collection;

import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.IEntityFactory;
import de.osthus.ambeth.merge.IEntityMetaDataProvider;

public abstract class AbstractDatasetBuilder implements IDatasetBuilder
{
	private static final ThreadLocal<Collection<Object>> INITIAL_TEST_DATASET_TL = new ThreadLocal<Collection<Object>>();

	@LogInstance
	private ILogger log;

	@Autowired
	protected IEntityFactory entityFactory;

	@Autowired
	protected IEntityMetaDataProvider entityMetaDataProvider;

	protected abstract void buildDatasetInternal();

	@Override
	public Collection<Class<? extends IDatasetBuilder>> getDependsOn()
	{
		return null;
	}

	@Override
	public void buildDataset(Collection<Object> initialTestDataset)
	{
		Collection<Object> oldSet = INITIAL_TEST_DATASET_TL.get();
		INITIAL_TEST_DATASET_TL.set(initialTestDataset);
		try
		{
			buildDatasetInternal();
		}
		finally
		{
			if (oldSet != null)
			{
				INITIAL_TEST_DATASET_TL.set(oldSet);
			}
			else
			{
				INITIAL_TEST_DATASET_TL.remove();
			}
		}
	}

	protected <V> V createEntity(Class<V> entityType)
	{
		V entity = entityFactory.createEntity(entityType);
		INITIAL_TEST_DATASET_TL.get().add(entity);
		return entity;
	}
}
