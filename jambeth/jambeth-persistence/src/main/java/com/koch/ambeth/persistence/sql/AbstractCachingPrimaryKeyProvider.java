package com.koch.ambeth.persistence.sql;

import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.koch.ambeth.ioc.IDisposableBean;
import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.merge.transfer.ObjRef;
import com.koch.ambeth.persistence.api.IPrimaryKeyProvider;
import com.koch.ambeth.persistence.api.ITableMetaData;
import com.koch.ambeth.persistence.config.PersistenceConfigurationConstants;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.service.metadata.Member;
import com.koch.ambeth.util.IConversionHelper;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.EmptyList;
import com.koch.ambeth.util.collections.HashMap;
import com.koch.ambeth.util.collections.IList;
import com.koch.ambeth.util.collections.SmartCopyMap;

public abstract class AbstractCachingPrimaryKeyProvider implements IPrimaryKeyProvider, IDisposableBean
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IServiceContext beanContext;

	@Autowired
	protected IConversionHelper conversionHelper;

	@Property(name = PersistenceConfigurationConstants.SequencePrefetchSize, defaultValue = "200")
	protected int prefetchIdAmount;

	protected final SmartCopyMap<String, IPrimaryKeyProvider> seqNameToPrimaryKeyProviderMap = new SmartCopyMap<String, IPrimaryKeyProvider>(0.5f);

	protected final HashMap<String, ArrayList<Object>> seqToCachedIdsMap = new HashMap<String, ArrayList<Object>>(0.5f);

	protected final Lock writeLock = new ReentrantLock();

	@Override
	public void destroy() throws Throwable
	{
		for (Entry<String, IPrimaryKeyProvider> entry : seqNameToPrimaryKeyProviderMap)
		{
			IPrimaryKeyProvider bean = entry.getValue();
			if (bean instanceof IDisposableBean)
			{
				((IDisposableBean) bean).destroy();
			}
		}
		seqNameToPrimaryKeyProviderMap.clear();
	}

	@Override
	public void acquireIds(ITableMetaData table, IList<IObjRef> idlessObjRefs)
	{
		String sequenceName = table.getSequenceName();
		if (sequenceName == null)
		{
			throw new IllegalStateException("No sequence configured for table " + table);
		}
		IPrimaryKeyProvider primaryKeyProvider = seqNameToPrimaryKeyProviderMap.get(sequenceName);
		if (primaryKeyProvider != null)
		{
			primaryKeyProvider.acquireIds(table, idlessObjRefs);
			return;
		}
		Lock writeLock = this.writeLock;
		writeLock.lock();
		try
		{
			primaryKeyProvider = seqNameToPrimaryKeyProviderMap.get(sequenceName);
			if (primaryKeyProvider != null)
			{
				primaryKeyProvider.acquireIds(table, idlessObjRefs);
				return;
			}
			Class<?> customSequenceType = null;
			try
			{
				customSequenceType = Thread.currentThread().getContextClassLoader().loadClass(sequenceName);
			}
			catch (Throwable e)
			{
				// intended blank
			}
			if (customSequenceType == null)
			{
				primaryKeyProvider = new IPrimaryKeyProvider()
				{
					@Override
					public void acquireIds(ITableMetaData table, IList<IObjRef> idlessObjRefs)
					{
						IList<Object> acquiredIds = acquireIdsIntern(table, idlessObjRefs.size());

						for (int i = idlessObjRefs.size(); i-- > 0;)
						{
							IObjRef reference = idlessObjRefs.get(i);
							reference.setId(acquiredIds.get(i));
							reference.setIdNameIndex(ObjRef.PRIMARY_KEY_INDEX);
						}
					}
				};
			}
			else
			{
				primaryKeyProvider = (IPrimaryKeyProvider) beanContext.registerBean(customSequenceType).finish();
			}
			seqNameToPrimaryKeyProviderMap.put(sequenceName, primaryKeyProvider);
		}
		finally
		{
			writeLock.unlock();
		}
		primaryKeyProvider.acquireIds(table, idlessObjRefs);
	}

	protected IList<Object> acquireIdsIntern(ITableMetaData table, int count)
	{
		if (count == 0)
		{
			return EmptyList.getInstance();
		}
		String sequenceName = table.getSequenceName();
		if (sequenceName == null)
		{
			throw new IllegalStateException("No sequence configured for table " + table);
		}
		ArrayList<Object> ids = new ArrayList<Object>(count);
		int requestCount = count + prefetchIdAmount;

		Lock writeLock = this.writeLock;
		ArrayList<Object> cachedIds = null;

		writeLock.lock();
		try
		{
			cachedIds = seqToCachedIdsMap.get(sequenceName);
			if (cachedIds == null)
			{
				cachedIds = new ArrayList<Object>(requestCount);
				seqToCachedIdsMap.put(sequenceName, cachedIds);
			}
			while (count > 0 && cachedIds.size() >= count)
			{
				Object cachedId = cachedIds.popLastElement();
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
		ArrayList<Object> newIds = new ArrayList<Object>(requestCount);

		// Make sure after the request are still enough ids cached
		acquireIdsIntern(table, requestCount, newIds);

		IConversionHelper conversionHelper = this.conversionHelper;
		Member idMember = table.getIdField().getMember();
		Class<?> idType = idMember != null ? idMember.getRealType() : null;

		if (newIds.size() < requestCount)
		{
			throw new IllegalStateException("Requested at least " + requestCount + " ids from sequence '" + sequenceName + "' but retrieved only "
					+ newIds.size());
		}
		for (int a = 0; a < count; a++)
		{
			Object id = newIds.get(a);
			id = idType != null ? conversionHelper.convertValueToType(idType, id) : id;
			ids.add(id);
		}
		writeLock.lock();
		try
		{
			for (int a = newIds.size(); a-- > count;)
			{
				Object id = newIds.get(a);
				id = idType != null ? conversionHelper.convertValueToType(idType, id) : id;
				cachedIds.add(id);
			}
		}
		finally
		{
			writeLock.unlock();
		}
		return ids;
	}

	protected abstract void acquireIdsIntern(ITableMetaData table, int count, List<Object> targetIdList);
}
