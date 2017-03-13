package com.koch.ambeth.merge.mergecontroller;

import java.util.List;

import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.service.merge.IEntityMetaDataProvider;
import com.koch.ambeth.service.merge.IValueObjectConfig;
import com.koch.ambeth.service.merge.model.IEntityMetaData;
import com.koch.ambeth.util.collections.IList;

public class OrderedEntityMetaDataServer implements IEntityMetaDataProvider
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	private final Class<?>[] entityPersistOrder = { Parent.class, Child.class };

	private final IEntityMetaDataProvider entityMetaDataProvider;

	public OrderedEntityMetaDataServer(IEntityMetaDataProvider entityMetaDataProvider)
	{
		this.entityMetaDataProvider = entityMetaDataProvider;
	}

	@Override
	public Class<?>[] getEntityPersistOrder()
	{
		return entityPersistOrder;
	}

	@Override
	public IEntityMetaData getMetaData(Class<?> entityType)
	{
		return entityMetaDataProvider.getMetaData(entityType);
	}

	@Override
	public IEntityMetaData getMetaData(Class<?> entityType, boolean tryOnly)
	{
		return entityMetaDataProvider.getMetaData(entityType, tryOnly);
	}

	@Override
	public IList<IEntityMetaData> getMetaData(List<Class<?>> entityTypes)
	{
		return entityMetaDataProvider.getMetaData(entityTypes);
	}

	@Override
	public IList<Class<?>> findMappableEntityTypes()
	{
		return entityMetaDataProvider.findMappableEntityTypes();
	}

	@Override
	public IValueObjectConfig getValueObjectConfig(Class<?> valueType)
	{
		return entityMetaDataProvider.getValueObjectConfig(valueType);
	}

	@Override
	public IValueObjectConfig getValueObjectConfig(String xmlTypeName)
	{
		return entityMetaDataProvider.getValueObjectConfig(xmlTypeName);
	}

	@Override
	public List<Class<?>> getValueObjectTypesByEntityType(Class<?> entityType)
	{
		return entityMetaDataProvider.getValueObjectTypesByEntityType(entityType);
	}

	@Override
	public String buildDotGraph()
	{
		return entityMetaDataProvider.buildDotGraph();
	}
}
