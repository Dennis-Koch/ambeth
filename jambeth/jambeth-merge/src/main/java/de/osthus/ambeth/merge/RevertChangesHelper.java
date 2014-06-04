//package de.osthus.ambeth.merge;
//
//import java.awt.List;
//import java.util.Collection;
//import de.osthus.ambeth.cache.CacheDirective;
//import de.osthus.ambeth.cache.ICacheIntern;
//import de.osthus.ambeth.cache.IWritableCache;
//import de.osthus.ambeth.collections.Collection;
//import de.osthus.ambeth.collections.IList;
//import de.osthus.ambeth.collections.ISet;
//import de.osthus.ambeth.collections.IdentityHashSet;
//import de.osthus.ambeth.datachange.model.DirectDataChangeEntry;
//import de.osthus.ambeth.datachange.model.IDataChangeEntry;
//import de.osthus.ambeth.datachange.transfer.DataChangeEntry;
//import de.osthus.ambeth.datachange.transfer.DataChangeEvent;
//import de.osthus.ambeth.event.IEventDispatcher;
//import de.osthus.ambeth.ioc.IInitializingBean;
//import de.osthus.ambeth.log.ILogger;
//import de.osthus.ambeth.log.LoggerFactory;
//import de.osthus.ambeth.merge.model.IEntityMetaData;
//import de.osthus.ambeth.merge.model.IObjRef;
//import de.osthus.ambeth.merge.transfer.ObjRef;
//import de.osthus.ambeth.util.Lock;
//import de.osthus.ambeth.util.ParamChecker;
//
//public class RevertChangesHelper implements IRevertChangesHelper, IInitializingBean
//{
//	@SuppressWarnings("unused")
//	@LogInstance(RevertChangesHelper.class) private ILogger log;
//
//	protected IEntityMetaDataProvider EntityMetaDataProvider;
//
//	protected IEventDispatcher EventDispatcher;
//
//	protected RootCache RootCache;
//
//	// public SynchronizationContext SyncContext { get; set; }
//
//	protected IThreadPool ThreadPool;
//
//	public void AfterPropertiesSet()
//	{
//		ParamChecker.assertNotNull(EntityMetaDataProvider, "EntityMetaDataProvider");
//		ParamChecker.assertNotNull(EventDispatcher, "EventDispatcher");
//		ParamChecker.assertNotNull(RootCache, "RootCache");
//		ParamChecker.assertNotNull(SyncContext, "SyncContext");
//		ParamChecker.assertNotNull(ThreadPool, "ThreadPool");
//	}
//
//	public void revertChanges(Collection<?> objectsToRevert)
//	{
//		if (objectsToRevert == null)
//		{
//			return;
//		}
//		revertChangesIntern(objectsToRevert, false);
//	}
//
//	@Override
//	public void revertChanges(Object... objectsToRevert)
//	{
//		if (objectsToRevert == null)
//		{
//			return;
//		}
//		revertChangesIntern(objectsToRevert, false);
//	}
//
//	public void revertChangesGlobally(Collection<?> objectsToRevert)
//	{
//		if (objectsToRevert == null)
//		{
//			return;
//		}
//		revertChangesIntern(objectsToRevert, true);
//	}
//
//	@Override
//	public void revertChangesGlobally(Object... objectsToRevert)
//	{
//		if (objectsToRevert == null)
//		{
//			return;
//		}
//		revertChangesIntern(objectsToRevert, true);
//	}
//
//	protected void revertChangesIntern(Object objectsToRevert, boolean globally)
//        {
//            if (globally)
//            {
//                ThreadPool.queue(delegate()
//                {
//                    DataChangeEvent dataChange = new DataChangeEvent();
//
//                    dataChange.Inserts = new List<IDataChangeEntry>(0);
//                    dataChange.Updates = new List<IDataChangeEntry>();
//                    dataChange.Deletes = new List<IDataChangeEntry>();
//
//                    for (int a = objectsToRevert.Count; a-- > 0; )
//                    {
//                        T objectToRevert = objectsToRevert[a];
//                        IEntityMetaData metaData = EntityMetaDataProvider.GetMetaData(objectToRevert.GetType());
//                        Object id = metaData.IdMember.GetValue(objectToRevert, false);
//
//                        if (id == null)
//                        {
//                            dataChange.Deletes.Add(new DirectDataChangeEntry(objectToRevert));
//                            continue;
//                        }
//                        dataChange.Updates.Add(new DataChangeEntry(metaData.EntityType, ObjRef.PRIMARY_KEY_INDEX, id, null));
//                    }
//
//                    EventDispatcher.DispatchEvent(dataChange, DateTime.Now, -1);
//                });
//            }
//            else
//            {
//                ThreadPool.Queue(delegate()
//                {
//                    IList<IDataChangeEntry> directObjectDeletes = new List<IDataChangeEntry>();
//
//                    IList<IObjRef> orisToRevert = new List<IObjRef>();
//                    ISet<Object> persistedObjectsToRevert = new IdentityHashSet<Object>();
//                    for (int a = objectsToRevert.Count; a-- > 0; )
//                    {
//                        T objectToRevert = objectsToRevert[a];
//                        IEntityMetaData metaData = EntityMetaDataProvider.GetMetaData(objectToRevert.GetType());
//                        Object id = metaData.IdMember.GetValue(objectToRevert, false);
//
//                        if (id == null)
//                        {
//                            directObjectDeletes.Add(new DirectDataChangeEntry(objectToRevert));
//                            continue;
//                        }
//                        persistedObjectsToRevert.Add(objectToRevert);
//                        orisToRevert.Add(new ObjRef(metaData.EntityType, ObjRef.PRIMARY_KEY_INDEX, id, null));
//                    }
//                    Lock writeLock = RootCache.WriteLock;
//                    writeLock.Lock();
//                    try
//                    {
//                        IList<Object> hardRefsToRootCacheValues = RootCache.GetObjects(orisToRevert, CacheDirective.CacheValueResult | CacheDirective.ReturnMisses);
//                        for (int a = orisToRevert.Count; a-- > 0; )
//                        {
//                            if (hardRefsToRootCacheValues[a] == null)
//                            {
//                                // Object could not be loaded/retrieved any more. So the ori refers to an invalid object
//                                // We can not revert invalid objects and currently ignore them. They will raise exceptions if they will
//                                // be tried to persist in a merge process any time in the future
//                                orisToRevert.RemoveAt(a);
//                            }
//                        }
//                        // We do nothing with the hardRef-list from the RootCache. It is only necessary to keep track of the instance reference on the stack
//                        // To prohibit GC any potential WeakReferences in the meantime....
//                        SyncContext.Send(delegate(Object state)
//                        {
//                            RootCache.HandleChildCaches(delegate(Collection<IWritableCache> childCaches, ISet<IObjRef> orisToLoad)
//                            {
//                                IList<Object> hardRefsToRootCacheValuesHere = hardRefsToRootCacheValues;
//
//                                foreach (IWritableCache childCache in childCaches)
//                                {
//                                    IList<Object> persistedObjectsInThisCache = childCache.GetObjects(orisToRevert, CacheDirective.FailEarly);
//
//                                    for (int a = persistedObjectsInThisCache.Count; a-- > 0; )
//                                    {
//                                        Object persistedObjectInThisCache = persistedObjectsInThisCache[a];
//                                        if (!persistedObjectsToRevert.Contains(persistedObjectInThisCache))
//                                        {
//                                            continue;
//                                        }
//                                        RootCache.ApplyValues(persistedObjectInThisCache, (ICacheIntern)childCache);
//                                    }
//                                }
//                            });
//                            if (directObjectDeletes.Count == 0)
//                            {
//                                return;
//                            }
//                            ThreadPool.Queue(delegate()
//                            {
//                                DataChangeEvent dataChange = new DataChangeEvent();
//
//                                dataChange.Inserts = new List<IDataChangeEntry>(0);
//                                dataChange.Updates = new List<IDataChangeEntry>(0);
//                                dataChange.Deletes = directObjectDeletes;
//
//                                EventDispatcher.DispatchEvent(dataChange, DateTime.Now, -1);
//                            });
//                        }, null);
//                    }
//                    finally
//                    {
//                        writeLock.Unlock();
//                    }
//                });
//            }
//        }
// }