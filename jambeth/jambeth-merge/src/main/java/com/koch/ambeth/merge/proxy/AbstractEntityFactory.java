package com.koch.ambeth.merge.proxy;

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.merge.IEntityFactory;
import com.koch.ambeth.service.merge.IEntityMetaDataProvider;
import com.koch.ambeth.service.merge.model.IEntityMetaData;

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
