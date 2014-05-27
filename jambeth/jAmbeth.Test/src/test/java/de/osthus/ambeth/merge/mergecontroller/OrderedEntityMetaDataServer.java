package de.osthus.ambeth.merge.mergecontroller;

import java.util.List;

import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.IEntityMetaDataProvider;
import de.osthus.ambeth.merge.IValueObjectConfig;
import de.osthus.ambeth.merge.model.IEntityMetaData;

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
}
