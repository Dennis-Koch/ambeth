﻿using System.Collections.Generic;
using System.Collections;
using De.Osthus.Ambeth.Event;
using System;
using De.Osthus.Ambeth.Datachange.Transfer;
using De.Osthus.Ambeth.Datachange.Model;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Util;
using De.Osthus.Ambeth.Threading;
using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Merge.Transfer;
using De.Osthus.Ambeth.Merge;
using De.Osthus.Ambeth.Typeinfo;
using De.Osthus.Ambeth.Collections;
using De.Osthus.Ambeth.Metadata;

namespace De.Osthus.Ambeth.Datachange
{
    public class DataChangeEventBatcher : IEventBatcher, IInitializingBean
    {
        public IConversionHelper ConversionHelper { protected get; set; }

        public IEntityMetaDataProvider EntityMetaDataProvider { protected get; set; }

        public void AfterPropertiesSet()
        {
            ParamChecker.AssertNotNull(ConversionHelper, "ConversionHelper");
            ParamChecker.AssertNotNull(EntityMetaDataProvider, "EntityMetaDataProvider");
        }

        public IList<IQueuedEvent> BatchEvents(IList<IQueuedEvent> batchableEvents)
        {
            foreach (QueuedEvent batchableEvent in batchableEvents)
            {
                if (!(batchableEvent.EventObject is IDataChange))
                {
                    throw new ArgumentException("EventObject must be of type '" + typeof(IDataChange).FullName + "': " + batchableEvent.EventObject);
                }
            }
            List<IQueuedEvent> targetBatchedEvents = new List<IQueuedEvent>();
            BatchEventsIntern(batchableEvents, targetBatchedEvents);
            return targetBatchedEvents;
        }

        protected void BatchEventsIntern(IList<IQueuedEvent> batchableEvents, IList<IQueuedEvent> targetBatchedEvents)
        {
            if (batchableEvents.Count == 1)
            {
                targetBatchedEvents.Add(batchableEvents[0]);
                return;
            }
            if (batchableEvents.Count == 0)
            {
                return;
            }
            // Check if all datachanges are in the same localsource state so that we can group them

            IDictionary<IObjRef, IObjRef> touchedObjRefSet = new Dictionary<IObjRef, IObjRef>();

            ISet<IObjRef> touchedAsInsertObjRefDict = new HashSet<IObjRef>();
            ISet<IObjRef> touchedAsUpdateObjRefDict = new HashSet<IObjRef>();
            ISet<IObjRef> touchedAsDeleteObjRefDict = new HashSet<IObjRef>();

            int splitIndex = -1;
            DateTime? lastDCETime = null, lastQueuedEventTime = null;
            long lastSequenceNumber = -1;
            bool? isLocalSource = null;

            for (int a = 0, size = batchableEvents.Count; a < size; a++)
            {
                IQueuedEvent batchableEvent = batchableEvents[a];
                IDataChange dataChange = (IDataChange)batchableEvent.EventObject;

                if (isLocalSource.HasValue && isLocalSource.Value != dataChange.IsLocalSource)
                {
                    // IsLocalSource differs, we can not batch the events here
                    splitIndex = a;
                    break;
                }
                isLocalSource = dataChange.IsLocalSource;
                lastQueuedEventTime = batchableEvent.DispatchTime;
                lastDCETime = dataChange.ChangeTime;
                lastSequenceNumber = batchableEvent.SequenceNumber;

                IList<IDataChangeEntry> insertsOfItem = dataChange.Inserts;
                IList<IDataChangeEntry> updatesOfItem = dataChange.Updates;
                IList<IDataChangeEntry> deletesOfItem = dataChange.Deletes;

                foreach (IDataChangeEntry insertOfItem in insertsOfItem)
                {
                    if (insertOfItem.EntityType == null)
                    {
                        continue;
                    }
                    IObjRef objRef = ExtractAndMergeObjRef(insertOfItem, touchedObjRefSet);

                    touchedAsInsertObjRefDict.Add(objRef);
                }
                foreach (IDataChangeEntry updateOfItem in updatesOfItem)
                {
                    if (updateOfItem.EntityType == null)
                    {
                        continue;
                    }
                    IObjRef objRef = ExtractAndMergeObjRef(updateOfItem, touchedObjRefSet);

                    if (touchedAsInsertObjRefDict.Contains(objRef))
                    {
                        // Object is still seen as new in this batch sequence
                        // So we ignore the update event for this item. The ObjRef already has the updated version
                        continue;
                    }
                    touchedAsUpdateObjRefDict.Add(objRef);
                }
                foreach (IDataChangeEntry deleteOfItem in deletesOfItem)
                {
                    if (deleteOfItem.EntityType == null)
                    {
                        continue;
                    }
                    IObjRef objRef = ExtractAndMergeObjRef(deleteOfItem, touchedObjRefSet);

                    // Object can be removed from the queue because it has been updated AND deleted within the same batched sequence
                    // From the entities point of view there is nothing we are interested in
                    touchedAsUpdateObjRefDict.Remove(objRef);

                    if (!touchedAsInsertObjRefDict.Remove(objRef))
                    {
                        // Object will only be stored as deleted if it existed BEFORE the batched sequence
                        touchedAsDeleteObjRefDict.Add(objRef);
                    }
                }
            }
            if (splitIndex != -1 && splitIndex < batchableEvents.Count - 1)
            {
                // Cleanup garbage
                touchedAsInsertObjRefDict.Clear();
                touchedAsUpdateObjRefDict.Clear();
                touchedAsDeleteObjRefDict.Clear();
                touchedObjRefSet.Clear();

                SplitDataChangeBatch(batchableEvents, splitIndex, targetBatchedEvents);
            }
            else
            {
                IList<IDataChangeEntry> inserts = touchedAsInsertObjRefDict.Count > 0 ? new List<IDataChangeEntry>(touchedAsInsertObjRefDict.Count) : EmptyList.Empty<IDataChangeEntry>();
                IList<IDataChangeEntry> updates = touchedAsUpdateObjRefDict.Count > 0 ? new List<IDataChangeEntry>(touchedAsUpdateObjRefDict.Count) : EmptyList.Empty<IDataChangeEntry>();
                IList<IDataChangeEntry> deletes = touchedAsDeleteObjRefDict.Count > 0 ? new List<IDataChangeEntry>(touchedAsDeleteObjRefDict.Count) : EmptyList.Empty<IDataChangeEntry>();
                foreach (IObjRef objRef in touchedAsInsertObjRefDict)
                {
                    inserts.Add(new DataChangeEntry(objRef.RealType, objRef.IdNameIndex, objRef.Id, objRef.Version));
                }
                foreach (IObjRef objRef in touchedAsUpdateObjRefDict)
                {
                    updates.Add(new DataChangeEntry(objRef.RealType, objRef.IdNameIndex, objRef.Id, objRef.Version));
                }
                foreach (IObjRef objRef in touchedAsDeleteObjRefDict)
                {
                    deletes.Add(new DataChangeEntry(objRef.RealType, objRef.IdNameIndex, objRef.Id, objRef.Version));
                }
                DataChangeEvent compositeDataChange = new DataChangeEvent(inserts, updates, deletes, lastDCETime.Value, isLocalSource.Value);
                targetBatchedEvents.Add(new QueuedEvent(compositeDataChange, lastQueuedEventTime.Value, lastSequenceNumber));
            }
        }

        protected IObjRef ExtractAndMergeObjRef(IDataChangeEntry dataChangeEntry, IDictionary<IObjRef, IObjRef> touchedObjRefSet)
        {
            IEntityMetaData metaData = EntityMetaDataProvider.GetMetaData(dataChangeEntry.EntityType);
            IObjRef objRef = GetObjRef(dataChangeEntry, metaData);

            IObjRef existingObjRef = DictionaryExtension.ValueOrDefault(touchedObjRefSet, objRef);
            if (existingObjRef == null)
            {
                touchedObjRefSet.Add(objRef, objRef);
                return objRef;
            }
            Object newVersion = objRef.Version;
            Object existingVersion = existingObjRef.Version;

            if (newVersion == null)
            {
                return existingObjRef;
            }
            if (existingVersion == null || ((IComparable)newVersion).CompareTo(existingVersion) >= 0)
            {
                existingObjRef.Version = newVersion;
            }
            return existingObjRef;
        }

        protected void SplitDataChangeBatch(IList<IQueuedEvent> dataChanges, int indexToSplit, IList<IQueuedEvent> targetBatchedEvents)
        {
            List<IQueuedEvent> firstSplit = new List<IQueuedEvent>(indexToSplit);
            List<IQueuedEvent> secondSplit = new List<IQueuedEvent>(dataChanges.Count - indexToSplit);

            for (int b = 0; b < indexToSplit; b++)
            {
                firstSplit.Add(dataChanges[b]);
            }
            for (int b = indexToSplit, size = dataChanges.Count; b < size; b++)
            {
                secondSplit.Add(dataChanges[b]);
            }
            BatchEventsIntern(firstSplit, targetBatchedEvents);
            BatchEventsIntern(secondSplit, targetBatchedEvents);
        }

        protected IObjRef GetObjRef(IDataChangeEntry dataChangeEntry, IEntityMetaData metaData)
        {
            if (dataChangeEntry is DirectDataChangeEntry)
            {
                return new DirectObjRef(dataChangeEntry.EntityType, ((DirectDataChangeEntry)dataChangeEntry).Entry);
            }
            Object id = dataChangeEntry.Id;
            sbyte idIndex = dataChangeEntry.IdNameIndex;

            Member idMember = metaData.GetIdMemberByIdIndex(idIndex);
            Member versionMember = metaData.VersionMember;
            id = ConversionHelper.ConvertValueToType(idMember.RealType, id);
            Object version = versionMember != null ? ConversionHelper.ConvertValueToType(versionMember.RealType, dataChangeEntry.Version) : null;
            return new ObjRef(dataChangeEntry.EntityType, idIndex, id, version);
        }
    }
}
