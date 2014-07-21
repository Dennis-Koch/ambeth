package de.osthus.ambeth.sql;

import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.EmptyList;
import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.persistence.ITable;
import de.osthus.ambeth.persistence.config.PersistenceConfigurationConstants;
import de.osthus.ambeth.util.IConversionHelper;

public abstract class AbstractCachingPrimaryKeyProvider implements IPrimaryKeyProvider
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IConversionHelper conversionHelper;

	@Property(name = PersistenceConfigurationConstants.SequencePrefetchSize, defaultValue = "200")
	protected int prefetchIdAmount;

	protected final HashMap<String, ArrayList<Object>> seqToCachedIdsMap = new HashMap<String, ArrayList<Object>>(0.5f);

	protected final Lock writeLock = new ReentrantLock();

	@Override
	public IList<Object> acquireIds(ITable table, int count)
	{
		if (count == 0)
		{
			return EmptyList.getInstance();
		}
		String sequenceName = table.getSequenceName();

		ArrayList<Object> ids = new ArrayList<Object>(count);
		int prefetchIdAmount = this.prefetchIdAmount;

		Lock writeLock = this.writeLock;
		ArrayList<Object> cachedIds = null;
		if (prefetchIdAmount > 0)
		{
			writeLock.lock();
			try
			{
				cachedIds = seqToCachedIdsMap.get(sequenceName);
				if (cachedIds == null)
				{
					cachedIds = new ArrayList<Object>(prefetchIdAmount + count);
					seqToCachedIdsMap.put(sequenceName, cachedIds);
				}
				while (count > 0 && cachedIds.size() >= count)
				{
					Object cachedId = cachedIds.remove(cachedIds.size() - 1);
					ids.add(cachedId);
					count--;
				}
			}
			finally
			{
				writeLock.unlock();
			}
			if (count == 0)
			{
				// ids could be fully satisfied by the cache
				return ids;
			}
		}
		ArrayList<Object> newIds = new ArrayList<Object>(count + prefetchIdAmount);

		// Make sure after the request are still enough ids cached
		acquireIdsIntern(table, count + prefetchIdAmount, newIds);

		IConversionHelper conversionHelper = this.conversionHelper;
		Class<?> idType = table.getIdField().getMember().getRealType();

		writeLock.lock();
		try
		{
			for (int a = 0, size = newIds.size(); a < size; a++)
			{
				Object id = newIds.get(a);
				id = conversionHelper.convertValueToType(idType, id);
				if (count > 0)
				{
					count--;
					ids.add(id);
				}
				else
				{
					cachedIds.add(id);
				}
			}
			return ids;
		}
		finally
		{
			writeLock.unlock();
		}
	}

	protected abstract void acquireIdsIntern(ITable table, int count, List<Object> targetIdList);
}
