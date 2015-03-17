package de.osthus.ambeth.datachange.store;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.datachange.model.IDataChange;
import de.osthus.ambeth.datachange.model.IDataChangeEntry;
import de.osthus.ambeth.datachange.transfer.DataChangeEntry;
import de.osthus.ambeth.datachange.transfer.DataChangeEvent;
import de.osthus.ambeth.event.store.IEventStoreHandler;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.objrefstore.IObjRefStoreEntryProvider;
import de.osthus.ambeth.objrefstore.ObjRefStore;
import de.osthus.ambeth.objrefstore.ObjRefStoreSet;

public class DataChangeEventStoreHandler implements IEventStoreHandler, IInitializingBean
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IObjRefStoreEntryProvider objRefStoreEntryProvider;

	protected ObjRefStoreSet objRefToDataChangeEntrySet;

	protected final Lock writeLock = new ReentrantLock();

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		objRefToDataChangeEntrySet = new ObjRefStoreSet(objRefStoreEntryProvider);
	}

	protected ObjRefStore createObjRef(Class<?> entityType, int idIndex, Object id, Object version)
	{
		ObjRefStore objRefStore = objRefStoreEntryProvider.createObjRefStore(entityType, (byte) idIndex, id);
		objRefStore.setVersion(version);
		return objRefStore;
	}

	@Override
	public Object preSaveInStore(Object eventObject)
	{
		IDataChange dataChange = (IDataChange) eventObject;
		List<IDataChangeEntry> inserts = dataChange.getInserts();
		List<IDataChangeEntry> updates = dataChange.getUpdates();
		List<IDataChangeEntry> deletes = dataChange.getDeletes();

		int insertCount = inserts.size(), updateCount = updates.size();
		ObjRefStore[] allArray = new ObjRefStore[insertCount + updateCount + deletes.size()];
		int index = 0;
		Lock writeLock = this.writeLock;
		writeLock.lock();
		try
		{
			for (int a = insertCount; a-- > 0;)
			{
				allArray[index++] = writeToCacheEntry(inserts.get(a), false);
			}
			for (int a = updateCount; a-- > 0;)
			{
				allArray[index++] = writeToCacheEntry(updates.get(a), false);
			}
			for (int a = deletes.size(); a-- > 0;)
			{
				allArray[index++] = writeToCacheEntry(deletes.get(a), true);
			}
		}
		finally
		{
			writeLock.unlock();
		}
		DataChangeStoreItem dcStoreItem = new DataChangeStoreItem(allArray, insertCount, updateCount, dataChange.getChangeTime());
		return dcStoreItem;
	}

	@Override
	public Object postLoadFromStore(Object eventObject)
	{
		DataChangeStoreItem dataChangeStoreItem = (DataChangeStoreItem) eventObject;
		ObjRefStore[] items = dataChangeStoreItem.getBackingArray();
		int insertCount = dataChangeStoreItem.insertCount;
		int updateCount = dataChangeStoreItem.updateCount;
		int deleteCount = items.length - insertCount - updateCount;
		List<IDataChangeEntry> inserts = insertCount > 0 ? new ArrayList<IDataChangeEntry>(insertCount) : Collections.<IDataChangeEntry> emptyList();
		List<IDataChangeEntry> updates = updateCount > 0 ? new ArrayList<IDataChangeEntry>(updateCount) : Collections.<IDataChangeEntry> emptyList();
		List<IDataChangeEntry> deletes = deleteCount > 0 ? new ArrayList<IDataChangeEntry>(deleteCount) : Collections.<IDataChangeEntry> emptyList();

		int index = 0;
		for (int a = insertCount; a-- > 0;)
		{
			inserts.add(readFromCacheEntry(items[index++]));
		}
		for (int a = updateCount; a-- > 0;)
		{
			updates.add(readFromCacheEntry(items[index++]));
		}
		for (int a = deleteCount; a-- > 0;)
		{
			deletes.add(readFromCacheEntry(items[index++]));
		}
		return new DataChangeEvent(inserts, updates, deletes, dataChangeStoreItem.changeTime, false);
	}

	protected IDataChangeEntry readFromCacheEntry(ObjRefStore cachedObjRefStore)
	{
		return new DataChangeEntry(cachedObjRefStore.getRealType(), cachedObjRefStore.getIdNameIndex(), cachedObjRefStore.getId(),
				cachedObjRefStore.getVersion());
	}

	protected ObjRefStore writeToCacheEntry(IDataChangeEntry dataChangeEntry, boolean isDeleteEntry)
	{
		ObjRefStoreSet objRefToDataChangeEntrySet = this.objRefToDataChangeEntrySet;
		ObjRefStore cachedEntry;
		if (isDeleteEntry)
		{
			// After a specific delete the ObjRefStore will never be used by any other insert/update/delete again
			// So there is no need to hold an entry in our usage-set
			cachedEntry = objRefToDataChangeEntrySet.remove(dataChangeEntry.getEntityType(), dataChangeEntry.getIdNameIndex(), dataChangeEntry.getId());
			if (cachedEntry == null)
			{
				cachedEntry = createObjRef(dataChangeEntry.getEntityType(), dataChangeEntry.getIdNameIndex(), dataChangeEntry.getId(),
						dataChangeEntry.getVersion());
			}
			else
			{
				cachedEntry.setVersion(dataChangeEntry.getVersion());
			}
			cachedEntry.setUsageCount(ObjRefStore.UNDEFINED_USAGE);
		}
		else
		{
			cachedEntry = objRefToDataChangeEntrySet.get(dataChangeEntry.getEntityType(), dataChangeEntry.getIdNameIndex(), dataChangeEntry.getId());
			if (cachedEntry == null)
			{
				cachedEntry = objRefToDataChangeEntrySet.put(dataChangeEntry.getEntityType(), dataChangeEntry.getIdNameIndex(), dataChangeEntry.getId());
			}
			// Cached version is supposed to be always OLDER than the given one
			cachedEntry.setVersion(dataChangeEntry.getVersion());
			cachedEntry.incUsageCount();
		}
		return cachedEntry;
	}

	@Override
	public void eventRemovedFromStore(Object eventObject)
	{
		DataChangeStoreItem dataChangeStoreItem = (DataChangeStoreItem) eventObject;
		ObjRefStore[] items = dataChangeStoreItem.getBackingArray();

		ObjRefStoreSet objRefToDataChangeEntrySet = this.objRefToDataChangeEntrySet;
		Lock writeLock = this.writeLock;
		writeLock.lock();
		try
		{
			for (int a = items.length; a-- > 0;)
			{
				ObjRefStore objRefStore = items[a];
				objRefStore.decUsageCount();
				if (objRefStore.getUsageCount() == 0)
				{
					objRefToDataChangeEntrySet.remove(objRefStore);
				}
			}
		}
		finally
		{
			writeLock.unlock();
		}
	}
}
