package de.osthus.ambeth.proxy;

import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.IEntityFactory;
import de.osthus.ambeth.merge.IEntityMetaDataProvider;
import de.osthus.ambeth.merge.model.IEntityMetaData;

public abstract class AbstractEntityFactory implements IEntityFactory
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IEntityMetaDataProvider entityMetaDataProvider;

	@SuppressWarnings("unchecked")
	@Override
	public <T> T createEntity(Class<T> entityType)
	{
		IEntityMetaData metaData = entityMetaDataProvider.getMetaData(entityType);
		return (T) createEntity(metaData);
	}

	@Override
	public boolean supportsEnhancement(Class<?> enhancementType)
	{
		return false;
	}
}
