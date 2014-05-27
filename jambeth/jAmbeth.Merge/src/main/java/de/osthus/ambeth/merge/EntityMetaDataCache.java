package de.osthus.ambeth.merge;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.Lock;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.HashSet;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.collections.LinkedHashMap;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.ioc.extendable.ClassExtendableContainer;
import de.osthus.ambeth.merge.model.IEntityMetaData;
import de.osthus.ambeth.proxy.IProxyFactory;
import de.osthus.ambeth.typeinfo.ITypeInfoItem;
import de.osthus.ambeth.xml.IXmlTypeHelper;

public class EntityMetaDataCache extends ClassExtendableContainer<IEntityMetaData> implements IEntityMetaDataProvider, IValueObjectConfigExtendable,
		IInitializingBean
{
	protected static final Class<?>[] EMPTY_TYPES = new Class[0];

	@Autowired
	protected IEntityMetaDataProvider entityMetaDataProvider;

	@Autowired
	protected IProxyFactory proxyFactory;

	@Autowired
	protected ValueObjectMap valueObjectMap;

	@Autowired
	protected IXmlTypeHelper xmlTypeHelper;

	protected IEntityMetaData alreadyHandled;

	public EntityMetaDataCache()
	{
		super("metaData", "type");
	}

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		alreadyHandled = proxyFactory.createProxy(IEntityMetaData.class);
	}

	@Override
	public IEntityMetaData getMetaData(Class<?> entityType)
	{
		return getMetaData(entityType, false);
	}

	@Override
	public IEntityMetaData getMetaData(Class<?> entityType, boolean tryOnly)
	{
		IEntityMetaData metaDataItem = getExtension(entityType);
		if (metaDataItem != null)
		{
			if (metaDataItem == alreadyHandled)
			{
				if (tryOnly)
				{
					return null;
				}
				throw new IllegalArgumentException("No metadata found for entity of type " + entityType.getName());
			}
			return metaDataItem;
		}
		ArrayList<Class<?>> missingEntityTypes = new ArrayList<Class<?>>(1);
		missingEntityTypes.add(entityType);
		IList<IEntityMetaData> missingMetaData = getMetaData(missingEntityTypes);
		if (missingMetaData.size() > 0)
		{
			return missingMetaData.get(0);
		}
		if (tryOnly)
		{
			return null;
		}
		throw new IllegalArgumentException("No metadata found for entity of type " + entityType.getName());
	}

	protected IList<Class<?>> addLoadedMetaData(List<Class<?>> entityTypes, List<IEntityMetaData> loadedMetaData)
	{
		HashSet<Class<?>> cascadeMissingEntityTypes = null;
		Lock writeLock = getWriteLock();
		writeLock.lock();
		try
		{
			for (int a = loadedMetaData.size(); a-- > 0;)
			{
				IEntityMetaData missingMetaDataItem = loadedMetaData.get(a);
				Class<?> entityType = missingMetaDataItem.getEntityType();
				if (getExtension(entityType) != null)
				{
					continue;
				}
				register(missingMetaDataItem, entityType);

				for (ITypeInfoItem relationMember : missingMetaDataItem.getRelationMembers())
				{
					Class<?> relationMemberType = relationMember.getElementType();
					if (!containsKey(relationMemberType))
					{
						if (cascadeMissingEntityTypes == null)
						{
							cascadeMissingEntityTypes = new HashSet<Class<?>>();
						}
						cascadeMissingEntityTypes.add(relationMemberType);
					}
				}
			}
			for (int a = entityTypes.size(); a-- > 0;)
			{
				Class<?> entityType = entityTypes.get(a);
				if (!containsKey(entityType))
				{
					// add dummy items to ensure that this type does not
					// get queried a second time
					register(alreadyHandled, entityType);
				}
			}
			return cascadeMissingEntityTypes != null ? cascadeMissingEntityTypes.toList() : null;
		}
		finally
		{
			writeLock.unlock();
		}
	}

	@Override
	public IList<IEntityMetaData> getMetaData(List<Class<?>> entityTypes)
	{
		ArrayList<IEntityMetaData> result = new ArrayList<IEntityMetaData>(entityTypes.size());
		IList<Class<?>> missingEntityTypes = null;
		for (int a = entityTypes.size(); a-- > 0;)
		{
			Class<?> entityType = entityTypes.get(a);
			IEntityMetaData metaDataItem = getExtension(entityType);
			if (metaDataItem != null)
			{
				if (metaDataItem != null)
				{
					if (metaDataItem != alreadyHandled)
					{
						result.add(metaDataItem);
					}
					continue;
				}
			}
			if (missingEntityTypes == null)
			{
				missingEntityTypes = new ArrayList<Class<?>>();
			}
			missingEntityTypes.add(entityType);
		}
		if (missingEntityTypes == null)
		{
			return result;
		}
		IEntityMetaDataProvider entityMetaDataProvider = this.entityMetaDataProvider;
		while (missingEntityTypes != null && missingEntityTypes.size() > 0)
		{
			IList<IEntityMetaData> loadedMetaData = entityMetaDataProvider.getMetaData(missingEntityTypes);

			IList<Class<?>> cascadeMissingEntityTypes = addLoadedMetaData(missingEntityTypes, loadedMetaData);

			if (cascadeMissingEntityTypes != null && cascadeMissingEntityTypes.size() > 0)
			{
				missingEntityTypes = cascadeMissingEntityTypes;
			}
			else
			{
				missingEntityTypes.clear();
			}
		}
		return getMetaData(entityTypes);
	}

	@Override
	public IList<Class<?>> findMappableEntityTypes()
	{
		ArrayList<Class<?>> mappableEntities = new ArrayList<Class<?>>();
		LinkedHashMap<Class<?>, IValueObjectConfig> targetExtensionMap = new LinkedHashMap<Class<?>, IValueObjectConfig>();
		valueObjectMap.getExtensions(targetExtensionMap);
		mappableEntities.addAll(targetExtensionMap.keySet());

		return mappableEntities;
	}

	@Override
	public void registerValueObjectConfig(IValueObjectConfig config)
	{
		valueObjectMap.register(config, null);
	}

	@Override
	public void unregisterValueObjectConfig(IValueObjectConfig config)
	{
		valueObjectMap.unregister(config, null);
	}

	@Override
	public IValueObjectConfig getValueObjectConfig(Class<?> valueType)
	{
		IValueObjectConfig config = valueObjectMap.getExtension(valueType);
		if (config == null)
		{
			config = entityMetaDataProvider.getValueObjectConfig(valueType);
			if (config != null)
			{
				registerValueObjectConfig(config);
			}
		}

		return config;
	}

	@Override
	public IValueObjectConfig getValueObjectConfig(String xmlTypeName)
	{
		Class<?> valueType = xmlTypeHelper.getType(xmlTypeName);
		return getValueObjectConfig(valueType);
	}

	@Override
	public Class<?>[] getEntityPersistOrder()
	{
		return EMPTY_TYPES;
	}

	@Override
	public List<Class<?>> getValueObjectTypesByEntityType(Class<?> entityType)
	{
		List<Class<?>> valueObjectTypes = valueObjectMap.getValueObjectTypesByEntityType(entityType);
		if (valueObjectTypes == null)
		{
			valueObjectTypes = Collections.emptyList();
		}
		return valueObjectTypes;
	}
}
