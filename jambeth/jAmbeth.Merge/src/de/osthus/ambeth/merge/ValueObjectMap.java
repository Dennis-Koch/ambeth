package de.osthus.ambeth.merge;

import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.EmptyList;
import de.osthus.ambeth.collections.ILinkedMap;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.collections.SmartCopyMap;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.ioc.extendable.IMapExtendableContainer;
import de.osthus.ambeth.ioc.extendable.MapExtendableContainer;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;

public class ValueObjectMap extends SmartCopyMap<Class<?>, List<Class<?>>> implements IMapExtendableContainer<Class<?>, IValueObjectConfig>
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	protected final MapExtendableContainer<Class<?>, IValueObjectConfig> typeToValueObjectConfig = new MapExtendableContainer<Class<?>, IValueObjectConfig>(
			"configuration", "value object class");

	@Autowired
	protected IEntityMetaDataProvider entityMetaDataProvider;

	@Override
	public IList<IValueObjectConfig> getExtensions(Class<?> key)
	{
		return typeToValueObjectConfig.getExtensions(key);
	}

	@Override
	public ILinkedMap<Class<?>, IValueObjectConfig> getExtensions()
	{
		return typeToValueObjectConfig.getExtensions();
	}

	public IList<Class<?>> getValueObjectTypesByEntityType(Class<?> entityType)
	{
		List<Class<?>> valueObjectTypes = get(entityType);
		if (valueObjectTypes == null)
		{
			// Check if the entityType is really an entity type
			if (entityMetaDataProvider.getMetaData(entityType, true) == null)
			{
				throw new IllegalStateException("'" + entityType + "' is no valid entity type");
			}
			return EmptyList.getInstance();
		}
		ArrayList<Class<?>> resultList = new ArrayList<Class<?>>(valueObjectTypes.size());
		for (int a = 0, size = valueObjectTypes.size(); a < size; a++)
		{
			Class<?> valueObjectType = valueObjectTypes.get(a);
			resultList.add(valueObjectType);
		}
		return resultList;
	}

	@Override
	public IValueObjectConfig getExtension(Class<?> key)
	{
		return typeToValueObjectConfig.getExtension(key);
	}

	@Override
	public void getExtensions(Map<Class<?>, IValueObjectConfig> targetExtensionMap)
	{
		typeToValueObjectConfig.getExtensions(targetExtensionMap);
	}

	@Override
	public void register(IValueObjectConfig config, Class<?> key)
	{
		Class<?> entityType = config.getEntityType();
		Class<?> valueType = config.getValueType();

		Lock writeLock = getWriteLock();
		writeLock.lock();
		try
		{
			typeToValueObjectConfig.register(config, valueType);

			// Clone list because of SmartCopy behavior
			List<Class<?>> valueObjectTypes = get(entityType);
			if (valueObjectTypes == null)
			{
				valueObjectTypes = new ArrayList<Class<?>>(1);
			}
			else
			{
				valueObjectTypes = new ArrayList<Class<?>>(valueObjectTypes);
			}
			valueObjectTypes.add(valueType);
			put(entityType, valueObjectTypes);
		}
		finally
		{
			writeLock.unlock();
		}
	}

	@Override
	public void unregister(IValueObjectConfig config, Class<?> key)
	{
		Class<?> entityType = config.getEntityType();
		Class<?> valueType = config.getValueType();

		Lock writeLock = getWriteLock();
		writeLock.lock();
		try
		{
			typeToValueObjectConfig.unregister(config, valueType);
			List<Class<?>> valueObjectTypes = get(entityType);
			valueObjectTypes.remove(valueType);
			if (valueObjectTypes.size() == 0)
			{
				remove(entityType);
			}
		}
		finally
		{
			writeLock.unlock();
		}
	}
}
