package de.osthus.ambeth.datachange;

import java.util.Collections;
import java.util.List;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.HashSet;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.collections.ISet;
import de.osthus.ambeth.datachange.model.DirectDataChangeEntry;
import de.osthus.ambeth.datachange.model.IDataChange;
import de.osthus.ambeth.datachange.model.IDataChangeEntry;
import de.osthus.ambeth.datachange.transfer.DataChangeEntry;
import de.osthus.ambeth.datachange.transfer.DataChangeEvent;
import de.osthus.ambeth.event.IEventBatcher;
import de.osthus.ambeth.event.IQueuedEvent;
import de.osthus.ambeth.event.QueuedEvent;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.IEntityMetaDataProvider;
import de.osthus.ambeth.merge.model.IEntityMetaData;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.merge.transfer.DirectObjRef;
import de.osthus.ambeth.merge.transfer.ObjRef;
import de.osthus.ambeth.metadata.Member;
import de.osthus.ambeth.util.IConversionHelper;
import de.osthus.ambeth.util.ParamChecker;

public class DataChangeEventBatcher implements IEventBatcher, IInitializingBean
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	protected IConversionHelper conversionHelper;

	protected IEntityMetaDataProvider entityMetaDataProvider;

	@Override
	public void afterPropertiesSet()
	{
		ParamChecker.assertNotNull(conversionHelper, "ConversionHelper");
		ParamChecker.assertNotNull(entityMetaDataProvider, "EntityMetaDataProvider");
	}

	public void setConversionHelper(IConversionHelper conversionHelper)
	{
		this.conversionHelper = conversionHelper;
	}

	public void setEntityMetaDataProvider(IEntityMetaDataProvider entityMetaDataProvider)
	{
		this.entityMetaDataProvider = entityMetaDataProvider;
	}

	@Override
	public IList<IQueuedEvent> batchEvents(List<IQueuedEvent> batchableEvents)
	{
		for (int a = batchableEvents.size(); a-- > 0;)
		{
			IQueuedEvent batchableEvent = batchableEvents.get(a);
			if (!(batchableEvent.getEventObject() instanceof IDataChange))
			{
				throw new IllegalArgumentException("EventObject must be of type '" + IDataChange.class.getName() + "': " + batchableEvent.getEventObject());
			}
		}
		ArrayList<IQueuedEvent> targetBatchedEvents = new ArrayList<IQueuedEvent>();
		batchEventsIntern(batchableEvents, targetBatchedEvents);
		return targetBatchedEvents;
	}

	protected void batchEventsIntern(List<IQueuedEvent> batchableEvents, List<IQueuedEvent> targetBatchedEvents)
	{
		if (batchableEvents.size() == 1)
		{
			targetBatchedEvents.add(batchableEvents.get(0));
			return;
		}
		if (batchableEvents.size() == 0)
		{
			return;
		}
		// Check if all datachanges are in the same localsource state so that we can group them
		HashSet<IObjRef> touchedObjRefSet = new HashSet<IObjRef>();

		HashSet<IObjRef> touchedAsInsertObjRefDict = new HashSet<IObjRef>();
		HashSet<IObjRef> touchedAsUpdateObjRefDict = new HashSet<IObjRef>();
		HashSet<IObjRef> touchedAsDeleteObjRefDict = new HashSet<IObjRef>();
		int splitIndex = -1;
		long lastDCETime = 0, lastQueuedEventTime = 0, lastSequenceNumber = -1;
		Boolean isLocalSource = null;

		for (int a = 0, size = batchableEvents.size(); a < size; a++)
		{
			IQueuedEvent batchableEvent = batchableEvents.get(a);
			IDataChange dataChange = (IDataChange) batchableEvent.getEventObject();

			if (isLocalSource != null && isLocalSource.booleanValue() != dataChange.isLocalSource())
			{
				// IsLocalSource differs, we can not batch the events here
				splitIndex = a;
				break;
			}
			isLocalSource = dataChange.isLocalSource();
			lastQueuedEventTime = batchableEvent.getDispatchTime();
			lastDCETime = dataChange.getChangeTime();
			lastSequenceNumber = batchableEvent.getSequenceNumber();

			List<IDataChangeEntry> insertsOfItem = dataChange.getInserts();
			List<IDataChangeEntry> updatesOfItem = dataChange.getUpdates();
			List<IDataChangeEntry> deletesOfItem = dataChange.getDeletes();

			for (int b = insertsOfItem.size(); b-- > 0;)
			{
				IDataChangeEntry insertOfItem = insertsOfItem.get(b);
				if (insertOfItem.getEntityType() == null)
				{
					continue;
				}
				IObjRef objRef = extractAndMergeObjRef(insertOfItem, touchedObjRefSet);

				touchedAsInsertObjRefDict.add(objRef);
			}
			for (int b = updatesOfItem.size(); b-- > 0;)
			{
				IDataChangeEntry updateOfItem = updatesOfItem.get(b);
				if (updateOfItem.getEntityType() == null)
				{
					continue;
				}
				IObjRef objRef = extractAndMergeObjRef(updateOfItem, touchedObjRefSet);

				if (touchedAsInsertObjRefDict.contains(objRef))
				{
					// Object is still seen as new in this batch sequence
					// So we ignore the update event for this item. The ObjRef already has the updated version
					continue;
				}
				touchedAsUpdateObjRefDict.add(objRef);
			}
			for (int b = deletesOfItem.size(); b-- > 0;)
			{
				IDataChangeEntry deleteOfItem = deletesOfItem.get(b);
				if (deleteOfItem.getEntityType() == null)
				{
					continue;
				}
				IObjRef objRef = extractAndMergeObjRef(deleteOfItem, touchedObjRefSet);

				// Object can be removed from the queue because it has been updated AND deleted within the same batched sequence
				// From the entities point of view there is nothing we are interested in the update
				touchedAsUpdateObjRefDict.remove(objRef);

				if (!touchedAsInsertObjRefDict.remove(objRef))
				{
					// Object will only be stored as deleted if it existed BEFORE the batched sequence
					touchedAsDeleteObjRefDict.add(objRef);
				}
			}
		}
		if (splitIndex != -1 && splitIndex < batchableEvents.size() - 1)
		{
			// Cleanup garbage
			touchedAsInsertObjRefDict.clear();
			touchedAsUpdateObjRefDict.clear();
			touchedAsDeleteObjRefDict.clear();
			touchedObjRefSet.clear();

			splitDataChangeBatch(batchableEvents, splitIndex, targetBatchedEvents);
		}
		else
		{
			List<IDataChangeEntry> inserts = touchedAsInsertObjRefDict.size() > 0 ? new ArrayList<IDataChangeEntry>(touchedAsInsertObjRefDict.size())
					: Collections.<IDataChangeEntry> emptyList();
			List<IDataChangeEntry> updates = touchedAsUpdateObjRefDict.size() > 0 ? new ArrayList<IDataChangeEntry>(touchedAsUpdateObjRefDict.size())
					: Collections.<IDataChangeEntry> emptyList();
			List<IDataChangeEntry> deletes = touchedAsDeleteObjRefDict.size() > 0 ? new ArrayList<IDataChangeEntry>(touchedAsDeleteObjRefDict.size())
					: Collections.<IDataChangeEntry> emptyList();
			for (IObjRef objRef : touchedAsInsertObjRefDict)
			{
				inserts.add(new DataChangeEntry(objRef.getRealType(), objRef.getIdNameIndex(), objRef.getId(), objRef.getVersion()));
			}
			for (IObjRef objRef : touchedAsUpdateObjRefDict)
			{
				updates.add(new DataChangeEntry(objRef.getRealType(), objRef.getIdNameIndex(), objRef.getId(), objRef.getVersion()));
			}
			for (IObjRef objRef : touchedAsDeleteObjRefDict)
			{
				deletes.add(new DataChangeEntry(objRef.getRealType(), objRef.getIdNameIndex(), objRef.getId(), objRef.getVersion()));
			}
			DataChangeEvent compositeDataChange = new DataChangeEvent(inserts, updates, deletes, lastDCETime, isLocalSource.booleanValue());
			targetBatchedEvents.add(new QueuedEvent(compositeDataChange, lastQueuedEventTime, lastSequenceNumber));
		}
	}

	protected void splitDataChangeBatch(List<IQueuedEvent> dataChanges, int indexToSplit, List<IQueuedEvent> targetBatchedEvents)
	{
		ArrayList<IQueuedEvent> firstSplit = new ArrayList<IQueuedEvent>(indexToSplit);
		ArrayList<IQueuedEvent> secondSplit = new ArrayList<IQueuedEvent>(dataChanges.size() - indexToSplit);

		for (int b = 0; b < indexToSplit; b++)
		{
			firstSplit.add(dataChanges.get(b));
		}
		for (int b = indexToSplit, size = dataChanges.size(); b < size; b++)
		{
			secondSplit.add(dataChanges.get(b));
		}
		batchEventsIntern(firstSplit, targetBatchedEvents);
		batchEventsIntern(secondSplit, targetBatchedEvents);
	}

	@SuppressWarnings("unchecked")
	protected IObjRef extractAndMergeObjRef(IDataChangeEntry dataChangeEntry, ISet<IObjRef> touchedObjRefSet)
	{
		IEntityMetaData metaData = entityMetaDataProvider.getMetaData(dataChangeEntry.getEntityType());
		IObjRef objRef = getObjRef(dataChangeEntry, metaData);

		IObjRef existingObjRef = touchedObjRefSet.get(objRef);
		if (existingObjRef == null)
		{
			touchedObjRefSet.add(objRef);
			return objRef;
		}
		Object newVersion = objRef.getVersion();
		Object existingVersion = existingObjRef.getVersion();

		if (newVersion == null)
		{
			return existingObjRef;
		}
		if (existingVersion == null || ((Comparable<Object>) newVersion).compareTo(existingVersion) >= 0)
		{
			existingObjRef.setVersion(newVersion);
		}
		return existingObjRef;
	}

	protected IObjRef getObjRef(IDataChangeEntry dataChangeEntry, IEntityMetaData metaData)
	{
		if (dataChangeEntry instanceof DirectDataChangeEntry)
		{
			return new DirectObjRef(dataChangeEntry.getEntityType(), ((DirectDataChangeEntry) dataChangeEntry).getEntry());
		}
		Object id = dataChangeEntry.getId();
		byte idIndex = dataChangeEntry.getIdNameIndex();

		Member idMember = metaData.getIdMemberByIdIndex(idIndex);
		Member versionMember = metaData.getVersionMember();
		id = conversionHelper.convertValueToType(idMember.getRealType(), id);
		Object version = versionMember != null ? conversionHelper.convertValueToType(versionMember.getRealType(), dataChangeEntry.getVersion()) : null;
		return new ObjRef(dataChangeEntry.getEntityType(), idIndex, id, version);
	}
}
