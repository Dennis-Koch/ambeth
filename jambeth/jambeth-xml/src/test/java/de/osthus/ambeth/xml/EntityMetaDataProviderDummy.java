package de.osthus.ambeth.xml;

import java.util.Collections;
import java.util.List;

import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.IEntityMetaDataProvider;
import de.osthus.ambeth.merge.IValueObjectConfig;
import de.osthus.ambeth.merge.model.IEntityMetaData;

public class EntityMetaDataProviderDummy implements IEntityMetaDataProvider
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	public IEntityMetaData getMetaData(Class<?> entityType)
	{
		return null;
	}

	@Override
	public IEntityMetaData getMetaData(Class<?> entityType, boolean tryOnly)
	{
		return null;
	}

	@Override
	public IList<IEntityMetaData> getMetaData(List<Class<?>> entityTypes)
	{
		return null;
	}

	@Override
	public IList<Class<?>> findMappableEntityTypes()
	{
		return null;
	}

	@Override
	public IValueObjectConfig getValueObjectConfig(Class<?> valueType)
	{
		return null;
	}

	@Override
	public IValueObjectConfig getValueObjectConfig(String xmlTypeName)
	{
		return null;
	}

	@Override
	public Class<?>[] getEntityPersistOrder()
	{
		return null;
	}

	@Override
	public List<Class<?>> getValueObjectTypesByEntityType(Class<?> entityType)
	{
		return Collections.emptyList();
	}
}
