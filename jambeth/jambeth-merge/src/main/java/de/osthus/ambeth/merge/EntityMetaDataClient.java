package de.osthus.ambeth.merge;

import java.util.List;

import de.osthus.ambeth.cache.ICache;
import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.model.IEntityMetaData;
import de.osthus.ambeth.service.IMergeService;
import de.osthus.ambeth.util.Lock;
import de.osthus.ambeth.util.LockState;

public class EntityMetaDataClient implements IEntityMetaDataProvider
{
	protected static final Class<?>[] EMPTY_TYPES = new Class[0];

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected ICache cache;

	@Autowired
	protected IMergeService mergeService;

	@Autowired
	protected IProxyHelper proxyHelper;

	@Override
	public IEntityMetaData getMetaData(Class<?> entityType)
	{
		return getMetaData(entityType, false);
	}

	@Override
	public IEntityMetaData getMetaData(Class<?> entityType, boolean tryOnly)
	{
		ArrayList<Class<?>> entityTypes = new ArrayList<Class<?>>(1);
		entityTypes.add(entityType);
		IList<IEntityMetaData> metaData = getMetaData(entityTypes);
		if (metaData.size() > 0)
		{
			return metaData.get(0);
		}
		if (tryOnly)
		{
			return null;
		}
		throw new IllegalArgumentException("No metadata found for entity of type " + entityType.getName());
	}

	@Override
	public IList<IEntityMetaData> getMetaData(List<Class<?>> entityTypes)
	{
		ArrayList<Class<?>> realEntityTypes = new ArrayList<Class<?>>(entityTypes.size());
		for (Class<?> entityType : entityTypes)
		{
			realEntityTypes.add(proxyHelper.getRealType(entityType));
		}
		ICache cache = this.cache.getCurrentCache();
		Lock readLock = cache != null ? cache.getReadLock() : null;
		LockState lockState = readLock != null ? readLock.releaseAllLocks() : null;
		try
		{
			List<IEntityMetaData> serviceResult = mergeService.getMetaData(realEntityTypes);
			ArrayList<IEntityMetaData> result = new ArrayList<IEntityMetaData>();
			result.addAll(serviceResult);
			return result;
		}
		finally
		{
			if (readLock != null)
			{
				readLock.reacquireLocks(lockState);
			}
		}
	}

	@Override
	public IList<Class<?>> findMappableEntityTypes()
	{
		throw new UnsupportedOperationException("This method is not supported by the EMD client stub. Please use a stateful EMD instance like caching");
	}

	@Override
	public IValueObjectConfig getValueObjectConfig(Class<?> valueType)
	{
		return mergeService.getValueObjectConfig(valueType);
	}

	@Override
	public IValueObjectConfig getValueObjectConfig(String xmlTypeName)
	{
		throw new UnsupportedOperationException("This method is not supported by the EMD client stub. Please use a stateful EMD instance like caching");
	}

	@Override
	public List<Class<?>> getValueObjectTypesByEntityType(Class<?> entityType)
	{
		throw new UnsupportedOperationException("This method is not supported by the EMD client stub. Please use a stateful EMD instance like caching");
	}

	@Override
	public Class<?>[] getEntityPersistOrder()
	{
		return EMPTY_TYPES;
	}

	@Override
	public String buildDotGraph()
	{
		throw new UnsupportedOperationException();
	}
}
