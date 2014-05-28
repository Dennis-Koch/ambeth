package de.osthus.ambeth.datachange.store;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.HashSet;
import de.osthus.ambeth.datachange.model.IDataChange;
import de.osthus.ambeth.datachange.model.IDataChangeEntry;
import de.osthus.ambeth.datachange.transfer.DataChangeEntry;
import de.osthus.ambeth.datachange.transfer.DataChangeEvent;
import de.osthus.ambeth.event.store.IEventStoreHandler;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;

public class DataChangeEventStoreHandler implements IEventStoreHandler, IInitializingBean
{
	protected final HashSet<ObjRefStore> objRefToDataChangeEntrySet = new HashSet<ObjRefStore>();

	protected final Lock writeLock = new ReentrantLock();

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		// Intended blank
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
		ObjRefStore objRef = new ObjRefStore();
		Lock writeLock = this.writeLock;
		writeLock.lock();
		try
		{
			for (int a = insertCount; a-- > 0;)
			{
				allArray[index++] = writeToCacheEntry(objRef, inserts.get(a), false);
			}
			for (int a = updateCount; a-- > 0;)
			{
				allArray[index++] = writeToCacheEntry(objRef, updates.get(a), false);
			}
			for (int a = deletes.size(); a-- > 0;)
			{
				allArray[index++] = writeToCacheEntry(objRef, deletes.get(a), true);
			}
		}
		finally
		{
			writeLock.unlock();
			objRef = null;
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

	protected ObjRefStore writeToCacheEntry(ObjRefStore tempObjRef, IDataChangeEntry dataChangeEntry, boolean isDeleteEntry)
	{
		HashSet<ObjRefStore> objRefToDataChangeEntrySet = this.objRefToDataChangeEntrySet;
		tempObjRef.init(dataChangeEntry.getEntityType(), dataChangeEntry.getIdNameIndex(), dataChangeEntry.getId(), dataChangeEntry.getVersion());
		ObjRefStore cachedEntry;
		if (isDeleteEntry)
		{
			// After a specific delete the ObjRefStore will never be used by any other insert/update/delete again
			// So there is no need to hold an entry in our usage-set
			cachedEntry = objRefToDataChangeEntrySet.removeAndGet(tempObjRef);
			if (cachedEntry == null)
			{
				cachedEntry = new ObjRefStore(dataChangeEntry.getEntityType(), dataChangeEntry.getIdNameIndex(), dataChangeEntry.getId(),
						dataChangeEntry.getVersion());
			}
			else
			{
				cachedEntry.setVersion(tempObjRef.getVersion());
			}
			cachedEntry.usageCount = ObjRefStore.UNDEFINED_USAGE;
		}
		else
		{
			cachedEntry = objRefToDataChangeEntrySet.get(tempObjRef);
			if (cachedEntry == null)
			{
				cachedEntry = new ObjRefStore(dataChangeEntry.getEntityType(), dataChangeEntry.getIdNameIndex(), dataChangeEntry.getId(),
						dataChangeEntry.getVersion());
				objRefToDataChangeEntrySet.add(cachedEntry);
			}
			else
			{
				// Cached version is supposed to be always OLDER than the given one
				cachedEntry.setVersion(tempObjRef.getVersion());
			}
			cachedEntry.usageCount++;
		}
		return cachedEntry;
	}

	@Override
	public void eventRemovedFromStore(Object eventObject)
	{
		DataChangeStoreItem dataChangeStoreItem = (DataChangeStoreItem) eventObject;
		ObjRefStore[] items = dataChangeStoreItem.getBackingArray();

		HashSet<ObjRefStore> objRefToDataChangeEntrySet = this.objRefToDataChangeEntrySet;
		Lock writeLock = this.writeLock;
		writeLock.lock();
		try
		{
			for (int a = items.length; a-- > 0;)
			{
				ObjRefStore objRefStore = items[a];
				objRefStore.usageCount--;
				if (objRefStore.usageCount == 0)
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
