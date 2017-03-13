package com.koch.ambeth.cache.server.datachange.store;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.koch.ambeth.datachange.model.IDataChange;
import com.koch.ambeth.datachange.model.IDataChangeEntry;
import com.koch.ambeth.datachange.transfer.DataChangeEntry;
import com.koch.ambeth.datachange.transfer.DataChangeEvent;
import com.koch.ambeth.event.store.IEventStoreHandler;
import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.merge.objrefstore.IObjRefStoreEntryProvider;
import com.koch.ambeth.merge.objrefstore.ObjRefStore;
import com.koch.ambeth.merge.objrefstore.ObjRefStoreSet;
import com.koch.ambeth.util.collections.ArrayList;

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
			// important to process the deletes first: technically it may be possible that a new entity in the same transaction
			// got the (released) primary id of a deleted entity. So we need to release the deletes first
			for (int a = deletes.size(); a-- > 0;)
			{
				ObjRefStore objRefStore = writeToCacheEntry(deletes.get(a), true);
				if (objRefStore == null)
				{
					continue;
				}
				allArray[index++] = objRefStore;
			}
			if (index != deletes.size())
			{
				// at least one deleted entity has no assigned ObjRefStore
				ObjRefStore[] newAllArray = new ObjRefStore[allArray.length - deletes.size() + index];
				System.arraycopy(allArray, 0, newAllArray, 0, index);
				allArray = newAllArray;
			}
			for (int a = insertCount; a-- > 0;)
			{
				allArray[index++] = writeToCacheEntry(inserts.get(a), false);
			}
			for (int a = updateCount; a-- > 0;)
			{
				allArray[index++] = writeToCacheEntry(updates.get(a), false);
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
		Class<?> entityType = dataChangeEntry.getEntityType();
		byte idIndex = dataChangeEntry.getIdNameIndex();
		Object id = dataChangeEntry.getId();
		if (isDeleteEntry)
		{
			if (id == null)
			{
				// this happens if an unpersisted entity gets deleted in the same transaction
				return null;
			}
			// After a specific delete the ObjRefStore will never be used by any other insert/update/delete again
			// So there is no need to hold an entry in our usage-set
			cachedEntry = objRefToDataChangeEntrySet.remove(entityType, idIndex, id);
			if (cachedEntry == null)
			{
				cachedEntry = createObjRef(entityType, idIndex, id, dataChangeEntry.getVersion());
			}
			else
			{
				cachedEntry.setVersion(dataChangeEntry.getVersion());
			}
			cachedEntry.setUsageCount(ObjRefStore.UNDEFINED_USAGE);
		}
		else
		{
			cachedEntry = objRefToDataChangeEntrySet.get(entityType, idIndex, id);
			if (cachedEntry == null)
			{
				cachedEntry = objRefToDataChangeEntrySet.put(entityType, idIndex, id);
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
