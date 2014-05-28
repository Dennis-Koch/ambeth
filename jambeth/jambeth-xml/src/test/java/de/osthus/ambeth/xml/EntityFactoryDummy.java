package de.osthus.ambeth.xml;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.IEntityFactory;
import de.osthus.ambeth.merge.model.IEntityMetaData;

public class EntityFactoryDummy implements IEntityFactory
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	public <T> T createEntity(Class<T> entityType)
	{
		return null;
	}

	@Override
	public Object createEntity(IEntityMetaData metaData)
	{
		return null;
	}

	@Override
	public boolean supportsEnhancement(Class<?> enhancementType)
	{
		return false;
	}
}
