package com.koch.ambeth.cache.datachange.revert;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import com.koch.ambeth.cache.AbstractCache;
import com.koch.ambeth.cache.ICacheIntern;
import com.koch.ambeth.cache.IFirstLevelCacheManager;
import com.koch.ambeth.cache.IRootCache;
import com.koch.ambeth.datachange.model.DirectDataChangeEntry;
import com.koch.ambeth.datachange.model.IDataChangeEntry;
import com.koch.ambeth.datachange.transfer.DataChangeEntry;
import com.koch.ambeth.datachange.transfer.DataChangeEvent;
import com.koch.ambeth.event.IEventDispatcher;
import com.koch.ambeth.event.IProcessResumeItem;
import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.util.ImmutableTypeSet;
import com.koch.ambeth.merge.IMergeController;
import com.koch.ambeth.merge.IProxyHelper;
import com.koch.ambeth.merge.IRevertChangesHelper;
import com.koch.ambeth.merge.IRevertChangesSavepoint;
import com.koch.ambeth.merge.RevertChangesFinishedCallback;
import com.koch.ambeth.merge.cache.CacheDirective;
import com.koch.ambeth.merge.cache.ICacheModification;
import com.koch.ambeth.merge.cache.IWritableCache;
import com.koch.ambeth.merge.cache.ValueHolderState;
import com.koch.ambeth.merge.proxy.IEntityMetaDataHolder;
import com.koch.ambeth.merge.proxy.IObjRefContainer;
import com.koch.ambeth.merge.transfer.ObjRef;
import com.koch.ambeth.merge.util.ValueHolderRef;
import com.koch.ambeth.service.merge.IEntityMetaDataProvider;
import com.koch.ambeth.service.merge.model.IEntityMetaData;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.service.metadata.Member;
import com.koch.ambeth.service.metadata.RelationMember;
import com.koch.ambeth.util.IParamHolder;
import com.koch.ambeth.util.ParamHolder;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.IList;
import com.koch.ambeth.util.collections.IMap;
import com.koch.ambeth.util.collections.ISet;
import com.koch.ambeth.util.collections.IdentityHashSet;
import com.koch.ambeth.util.collections.IdentityLinkedMap;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
import com.koch.ambeth.util.model.IDataObject;
import com.koch.ambeth.util.threading.IBackgroundWorkerDelegate;
import com.koch.ambeth.util.threading.IBackgroundWorkerParamDelegate;
import com.koch.ambeth.util.threading.IGuiThreadHelper;
import com.koch.ambeth.util.typeinfo.ITypeInfo;
import com.koch.ambeth.util.typeinfo.ITypeInfoItem;
import com.koch.ambeth.util.typeinfo.ITypeInfoProvider;

public class RevertChangesHelper implements IRevertChangesHelper {
	@Autowired
	protected IServiceContext beanContext;

	@Autowired
	protected ICacheModification cacheModification;

	@Autowired
	protected IEntityMetaDataProvider entityMetaDataProvider;

	@Autowired
	protected IEventDispatcher eventDispatcher;

	@Autowired
	protected IFirstLevelCacheManager firstLevelCacheManager;

	@Autowired
	protected IGuiThreadHelper guiThreadHelper;

	@Autowired
	protected IMergeController mergeController;

	@Autowired
	protected IProxyHelper proxyHelper;

	@Autowired
	protected IRootCache rootCache;

	@Autowired
	protected ITypeInfoProvider typeInfoProvider;

	protected void backupObjects(Object obj, IMap<Object, IBackup> originalToValueBackup) {
		if (obj == null) {
			return;
		}
		Class<?> objType = proxyHelper.getRealType(obj.getClass());
		if (ImmutableTypeSet.isImmutableType(objType) || originalToValueBackup.containsKey(obj)) {
			return;
		}
		if (obj.getClass().isArray()) {
			Class<?> elementType = obj.getClass().getComponentType();
			int length = Array.getLength(obj);
			Object clone = Array.newInstance(elementType, length);
			System.arraycopy(objType, 0, clone, 0, length);
			ArrayBackup arrayBackup = new ArrayBackup(clone);
			originalToValueBackup.put(obj, arrayBackup);
			if (!ImmutableTypeSet.isImmutableType(elementType)) {
				for (int a = length; a-- > 0;) {
					Object arrayItem = Array.get(obj, a);
					backupObjects(arrayItem, originalToValueBackup);
				}
			}
			return;
		}
		if (obj instanceof List) {
			List<?> list = (List<?>) obj;
			Object[] array = list.toArray(new Object[list.size()]);
			IBackup listBackup = ListBackup.create(array);
			originalToValueBackup.put(obj, listBackup);
			for (Object item : array) {
				backupObjects(item, originalToValueBackup);
			}
			return;
		}
		else if (obj instanceof Collection) {
			Collection<?> coll = (Collection<?>) obj;
			Object[] array = coll.toArray(new Object[coll.size()]);
			IBackup collBackup = CollectionBackup.create(array);
			originalToValueBackup.put(obj, collBackup);
			for (Object item : array) {
				backupObjects(item, originalToValueBackup);
			}
			return;
		}
		ITypeInfo typeInfo = typeInfoProvider.getTypeInfo(objType);
		IEntityMetaData metaData = entityMetaDataProvider.getMetaData(objType, true);

		ITypeInfoItem[] members = typeInfo.getMembers();
		Object[] originalValues = new Object[members.length];
		ObjectBackup objBackup = new ObjectBackup(members, originalValues);
		originalToValueBackup.put(obj, objBackup);

		for (int b = members.length; b-- > 0;) {
			ITypeInfoItem typeInfoMember = members[b];
			if (metaData != null) {
				Member member = metaData.getMemberByName(typeInfoMember.getName());
				if (member instanceof RelationMember) {
					int relationIndex = metaData.getIndexByRelation(member);
					ValueHolderState state = ((IObjRefContainer) obj).get__State(relationIndex);
					switch (state) {
						case INIT: {
							// nothing to do
							break;
						}
						case LAZY: {
							IObjRef[] objRefs = ((IObjRefContainer) obj).get__ObjRefs(relationIndex);
							originalValues[b] = ObjRefBackup.create(objRefs, relationIndex);
							continue;
						}
						case PENDING: {
							// TODO: wait till pending relation is fetched now
							throw RuntimeExceptionUtil.createEnumNotSupportedException(state);
						}
						default:
							throw RuntimeExceptionUtil.createEnumNotSupportedException(state);
					}
				}
			}
			Object originalValue = typeInfoMember.getValue(obj);
			originalValues[b] = originalValue;

			backupObjects(originalValue, originalToValueBackup);
		}
	}

	private void callWaitEventToResumeInGui(final IList<IDataChangeEntry> directObjectDeletes,
			final IList<IObjRef> orisToRevert, final ISet<Object> persistedObjectsToRevert,
			final IList<Object> objectsToRevert, final IList<Object> hardRefsToRootCacheValues,
			final IParamHolder<Boolean> success1, final IParamHolder<Boolean> success2,
			final IParamHolder<Boolean> success3,
			final RevertChangesFinishedCallback revertChangesFinishedCallback) {
		guiThreadHelper.invokeInGui(new IBackgroundWorkerDelegate() {
			@Override
			public void invoke() throws Throwable {
				waitEventToResume(new IBackgroundWorkerParamDelegate<IProcessResumeItem>() {
					@Override
					public void invoke(IProcessResumeItem processResumeItem) throws Throwable {
						try {
							boolean oldCacheModificationValue = cacheModification.isActive();
							boolean oldFailEarlyModeActive = AbstractCache.isFailInCacheHierarchyModeActive();
							cacheModification.setActive(true);
							AbstractCache.setFailInCacheHierarchyModeActive(true);
							try {
								IList<IWritableCache> firstLevelCaches =
										firstLevelCacheManager.selectFirstLevelCaches();

								// need a hard GC ref to the given collection during asynchronous
								// processing
								@SuppressWarnings("unused")
								IList<Object> hardRefsToRootCacheValuesHere = hardRefsToRootCacheValues;

								for (IWritableCache firstLevelCache : firstLevelCaches) {
									IList<Object> persistedObjectsInThisCache =
											firstLevelCache.getObjects(orisToRevert, CacheDirective.failEarly());

									for (int a = persistedObjectsInThisCache.size(); a-- > 0;) {
										Object persistedObjectInThisCache = persistedObjectsInThisCache.get(a);
										if (!persistedObjectsToRevert.contains(persistedObjectInThisCache)) {
											continue;
										}
										rootCache.applyValues(persistedObjectInThisCache,
												(ICacheIntern) firstLevelCache, null);
									}
								}
								for (int a = objectsToRevert.size(); a-- > 0;) {
									Object objectToRevert = objectsToRevert.get(a);
									if (objectToRevert instanceof IDataObject) {
										// Objects which are specified to be reverted loose their
										// flags
										((IDataObject) objectToRevert).setToBeDeleted(false);
									}
								}
								if (directObjectDeletes.size() == 0) {
									success2.setValue(Boolean.TRUE);
									return;
								}
							}
							finally {
								AbstractCache.setFailInCacheHierarchyModeActive(oldFailEarlyModeActive);
								cacheModification.setActive(oldCacheModificationValue);
							}
						}
						finally {
							if (processResumeItem != null) {
								processResumeItem.resumeProcessingFinished();
							}
						}
						success3.setValue(Boolean.FALSE);
						guiThreadHelper.invokeOutOfGui(new IBackgroundWorkerDelegate() {
							@Override
							public void invoke() {
								try {
									DataChangeEvent dataChange = DataChangeEvent.create(0, 0, 0);
									dataChange.setDeletes(directObjectDeletes);

									eventDispatcher.dispatchEvent(dataChange, System.currentTimeMillis(), -1);
									success3.setValue(Boolean.TRUE);
								}
								finally {
									if (revertChangesFinishedCallback != null) {
										revertChangesFinishedCallback.invoke(success3.getValue());
									}
								}
							}
						});
						success2.setValue(Boolean.TRUE);

					}
				}, new IBackgroundWorkerParamDelegate<Throwable>() {
					@Override
					public void invoke(Throwable state) throws Throwable {
						if (revertChangesFinishedCallback != null && success3.getValue() == null) {
							revertChangesFinishedCallback.invoke(success2.getValue());
						}
					}
				});
				success1.setValue(Boolean.TRUE);
			}
		});
	}

	@Override
	public IRevertChangesSavepoint createSavepoint(Object source) {
		if (source == null) {
			return null;
		}
		ArrayList<Object> objList = new ArrayList<>();
		findAllObjectsToBackup(source, objList, null, new IdentityHashSet<>());
		return createSavepointIntern(objList);
	}

	@Override
	public IRevertChangesSavepoint createSavepoint(Object... sources) {
		if (sources == null || sources.length == 0) {
			return null;
		}
		ArrayList<Object> objList = new ArrayList<>();
		findAllObjectsToBackup(sources, objList, null, new IdentityHashSet<>());
		return createSavepointIntern(objList);
	}

	protected IRevertChangesSavepoint createSavepointIntern(IList<Object> objList) {
		IdentityLinkedMap<Object, IBackup> originalToValueBackup = new IdentityLinkedMap<>();

		// Iterate manually through the list because the list itself should not be 'backuped'
		for (int a = 0, size = objList.size(); a < size; a++) {
			backupObjects(objList.get(a), originalToValueBackup);
		}
		Iterator<Entry<Object, IBackup>> iter = originalToValueBackup.iterator();
		while (iter.hasNext()) {
			Entry<Object, IBackup> entry = iter.next();
			IBackup backup = entry.getValue();
			if (backup == null) {
				iter.remove();
			}
		}
		return beanContext.registerBean(RevertChangesSavepoint.class)
				.propertyValue(RevertChangesSavepoint.P_CHANGES, originalToValueBackup).finish();
	}

	protected void fillRevertList(Object obj, ISet<Object> alreadyScannedSet,
			IList<Object> revertList, boolean recursive) {
		if (!alreadyScannedSet.add(obj)) {
			return;
		}
		if (obj instanceof List) {
			List<?> list = (List<?>) obj;
			for (int a = list.size(); a-- > 0;) {
				fillRevertList(list.get(a), alreadyScannedSet, revertList, recursive);
			}
			return;
		}
		else if (obj instanceof Iterable) {
			for (Object item : (Iterable<?>) obj) {
				fillRevertList(item, alreadyScannedSet, revertList, recursive);
			}
			return;
		}
		revertList.add(obj);
		if (recursive) {
			IEntityMetaData metaData = ((IEntityMetaDataHolder) obj).get__EntityMetaData();
			RelationMember[] relations = metaData.getRelationMembers();
			for (RelationMember relation : relations) {
				Object value = relation.getValue(obj);
				fillRevertList(value, alreadyScannedSet, revertList, recursive);
			}
		}
	}

	protected void findAllObjectsToBackup(Object obj, IList<Object> objList, IList<IObjRef> objRefs,
			ISet<Object> alreadyProcessedSet) {
		if (obj == null || !alreadyProcessedSet.add(obj)) {
			return;
		}
		// In java there has to be checked (in addition) for array-instance, too
		if (obj instanceof List) {
			List<?> list = (List<?>) obj;
			for (int a = list.size(); a-- > 0;) {
				Object item = list.get(a);
				findAllObjectsToBackup(item, objList, objRefs, alreadyProcessedSet);
			}
			return;
		}
		else if (obj instanceof Iterable) {
			for (Object item : (Iterable<?>) obj) {
				findAllObjectsToBackup(item, objList, objRefs, alreadyProcessedSet);
			}
			return;
		}
		IEntityMetaData metaData = ((IEntityMetaDataHolder) obj).get__EntityMetaData();
		objList.add(obj);
		if (objRefs != null) {
			Object id = metaData.getIdMember().getValue(obj);
			objRefs.add(new ObjRef(metaData.getEntityType(), ObjRef.PRIMARY_KEY_INDEX, id, null));
		}
		RelationMember[] relationMembers = metaData.getRelationMembers();
		for (int relationIndex = relationMembers.length; relationIndex-- > 0;) {
			RelationMember relationMember = relationMembers[relationIndex];
			if (!((IObjRefContainer) obj).is__Initialized(relationIndex)) {
				continue;
			}
			Object item = relationMember.getValue(obj);
			findAllObjectsToBackup(item, objList, objRefs, alreadyProcessedSet);
		}
	}

	@Override
	public void revertChanges(Object objectsToRevert) {
		revertChanges(objectsToRevert, null, false);
	}

	@Override
	public void revertChanges(Object objectsToRevert, boolean recursive) {
		revertChanges(objectsToRevert, null, recursive);
	}

	@Override
	public void revertChanges(Object objectsToRevert,
			RevertChangesFinishedCallback revertChangesFinishedCallback) {
		revertChanges(objectsToRevert, revertChangesFinishedCallback, false);
	}

	@Override
	public void revertChanges(Object objectsToRevert,
			RevertChangesFinishedCallback revertChangesFinishedCallback, boolean recursive) {
		if (objectsToRevert == null) {
			return;
		}
		ArrayList<Object> revertList = new ArrayList<>();
		fillRevertList(objectsToRevert, new IdentityHashSet<>(), revertList, recursive);
		revertChangesIntern(null, revertList, false, revertChangesFinishedCallback);
	}

	@Override
	public void revertChangesGlobally(Object objectsToRevert) {
		revertChangesGlobally(objectsToRevert, null, false);
	}

	@Override
	public void revertChangesGlobally(Object objectsToRevert, boolean recursive) {
		revertChangesGlobally(objectsToRevert, null, recursive);
	}

	@Override
	public void revertChangesGlobally(Object objectsToRevert,
			RevertChangesFinishedCallback revertChangesFinishedCallback) {
		revertChangesGlobally(objectsToRevert, revertChangesFinishedCallback, false);
	}

	@Override
	public void revertChangesGlobally(Object objectsToRevert,
			RevertChangesFinishedCallback revertChangesFinishedCallback, boolean recursive) {
		if (objectsToRevert == null) {
			return;
		}
		ArrayList<Object> revertList = new ArrayList<>();
		fillRevertList(objectsToRevert, new IdentityHashSet<>(), revertList, recursive);
		revertChangesIntern(null, revertList, true, revertChangesFinishedCallback);
	}

	protected void revertChangesIntern(IRevertChangesSavepoint savepoint,
			final IList<Object> objectsToRevert, boolean globally,
			final RevertChangesFinishedCallback revertChangesFinishedCallback) {
		// Store the RevertChangesFinishedCallback from this thread on the stack and set the
		// property null (for following calls):
		if (objectsToRevert == null || objectsToRevert.size() == 0) {
			if (revertChangesFinishedCallback != null) {
				revertChangesFinishedCallback.invoke(true);
			}
			return;
		}
		if (globally) {
			guiThreadHelper.invokeOutOfGui(new IBackgroundWorkerDelegate() {
				@Override
				public void invoke() throws Throwable {
					boolean success = false;
					try {
						DataChangeEvent dataChange = DataChangeEvent.create(0, -1, -1);

						for (int a = objectsToRevert.size(); a-- > 0;) {
							Object objectToRevert = objectsToRevert.get(a);
							IEntityMetaData metaData =
									((IEntityMetaDataHolder) objectToRevert).get__EntityMetaData();
							Object id = metaData.getIdMember().getValue(objectToRevert, false);

							if (id == null) {
								dataChange.getDeletes().add(new DirectDataChangeEntry(objectToRevert));
								continue;
							}
							dataChange.getUpdates().add(new DataChangeEntry(metaData.getEntityType(),
									ObjRef.PRIMARY_KEY_INDEX, id, null));
						}

						eventDispatcher.dispatchEvent(dataChange, System.currentTimeMillis(), -1);
						success = true;
					}
					finally {
						if (revertChangesFinishedCallback != null) {
							revertChangesFinishedCallback.invoke(success);
						}
					}
				}
			});
			return;
		}
		// Commented the following part from Ambeth 0.130 and use the part from Ambeth 0.129 due to
		// a
		// deadlock in the merge process:
		// GuiThreadHelper.InvokeOutOfGui(delegate()
		// {
		// bool success1 = false;
		// try
		// {
		// IList<IDataChangeEntry> directObjectDeletes = new List<IDataChangeEntry>();
		// IList<Object> initializedObjects =
		// MergeController.ScanForInitializedObjects(objectsToRevert,
		// true, null);

		// IList<IObjRef> orisToRevert = new List<IObjRef>();
		// ISet<Object> persistedObjectsToRevert = new IdentityHashSet<Object>();
		// for (int a = initializedObjects.Count; a-- > 0; )
		// {
		// Object objectToRevert = initializedObjects[a];
		// IEntityMetaData metaData = EntityMetaDataProvider.GetMetaData(objectToRevert.GetType());
		// Object id = metaData.IdMember.GetValue(objectToRevert, false);

		// if (id == null)
		// {
		// directObjectDeletes.Add(new DirectDataChangeEntry(objectToRevert));
		// continue;
		// }
		// persistedObjectsToRevert.Add(objectToRevert);
		// orisToRevert.Add(new ObjRef(metaData.EntityType, ObjRef.PRIMARY_KEY_INDEX, id, null));
		// }
		// IList<Object> hardRefsToRootCacheValues = RootCache.GetObjects(orisToRevert,
		// CacheDirective.CacheValueResult | CacheDirective.ReturnMisses);

		// for (int a = orisToRevert.Count; a-- > 0; )
		// {
		// if (hardRefsToRootCacheValues[a] == null)
		// {
		// // Object could not be loaded/retrieved any more. So the ori refers to an invalid object
		// // We can not revert invalid objects and currently ignore them. They will raise
		// exceptions if
		// they will
		// // be tried to persist in a merge process any time in the future
		// orisToRevert.RemoveAt(a);
		// }
		// }
		// // We do nothing with the hardRef-list from the RootCache. It is only necessary to keep
		// track
		// of the instance reference on the stack
		// // To prohibit GC any potential WeakReferences in the meantime....
		// GuiThreadHelper.InvokeInGuiAndWait(delegate()
		// {
		// IProcessResumeItem processResumeItem = WaitEventToResume();
		// try
		// {
		// bool oldCacheModificationValue = CacheModification.IsActive;
		// CacheModification.IsActive = true;
		// bool oldFailEarlyModeActive = AbstractCache<Object>.FailEarlyModeActive;
		// AbstractCache<Object>.FailEarlyModeActive = true;
		// try
		// {
		// IList<IWritableCache> firstLevelCaches = FirstLevelCacheManager.SelectFirstLevelCaches();
		// IList<Object> hardRefsToRootCacheValuesHere = hardRefsToRootCacheValues;

		// foreach (IWritableCache firstLevelCache in firstLevelCaches)
		// {
		// IList<Object> persistedObjectsInThisCache = firstLevelCache.GetObjects(orisToRevert,
		// CacheDirective.FailEarly);

		// for (int a = persistedObjectsInThisCache.Count; a-- > 0; )
		// {
		// Object persistedObjectInThisCache = persistedObjectsInThisCache[a];
		// if (!persistedObjectsToRevert.Contains(persistedObjectInThisCache))
		// {
		// continue;
		// }
		// RootCache.ApplyValues(persistedObjectInThisCache, (ICacheIntern)firstLevelCache);
		// }
		// }
		// for (int a = objectsToRevert.Count; a-- > 0; )
		// {
		// Object objectToRevert = objectsToRevert[a];
		// if (objectToRevert is IDataObject)
		// {
		// // Objects which are specified to be reverted loose their delete flag
		// ((IDataObject)objectToRevert).ToBeDeleted = false;
		// }
		// }
		// }
		// finally
		// {
		// AbstractCache<Object>.FailEarlyModeActive = oldFailEarlyModeActive;
		// CacheModification.IsActive = oldCacheModificationValue;
		// }
		// }
		// finally
		// {
		// if (processResumeItem != null)
		// {
		// processResumeItem.ResumeProcessingFinished();
		// processResumeItem = null;
		// }
		// }
		// });
		// if (directObjectDeletes.Count > 0)
		// {
		// DataChangeEvent dataChange = DataChangeEvent.Create(0, 0, 0);
		// dataChange.Deletes = directObjectDeletes;

		// EventDispatcher.DispatchEvent(dataChange, DateTime.Now, -1);
		// }
		// success1 = true;
		// }
		// finally
		// {
		// if (revertChangesFinishedCallback != null)
		// {
		// revertChangesFinishedCallback.Invoke(success1);
		// }
		// }
		// });

		// Here comes the part from Ambeth 0.129:
		guiThreadHelper.invokeOutOfGui(new IBackgroundWorkerDelegate() {
			@Override
			public void invoke() throws Throwable {
				revertChangesInternOutOfGui(objectsToRevert, revertChangesFinishedCallback);
			}
		});
	}

	protected void revertChangesInternOutOfGui(IList<Object> objectsToRevert,
			RevertChangesFinishedCallback revertChangesFinishedCallback) {
		ParamHolder<Boolean> success1 = new ParamHolder<>();
		ParamHolder<Boolean> success2 = new ParamHolder<>();
		ParamHolder<Boolean> success3 = new ParamHolder<>();
		try {
			ArrayList<IDataChangeEntry> directObjectDeletes = new ArrayList<>();
			ArrayList<IObjRef> objRefs = new ArrayList<>();
			ArrayList<IObjRef> privilegedObjRefs = new ArrayList<>();
			ArrayList<ValueHolderRef> valueHolderKeys = new ArrayList<>();
			IList<Object> initializedObjects = mergeController.scanForInitializedObjects(objectsToRevert,
					true, null, objRefs, privilegedObjRefs, valueHolderKeys);

			IList<IObjRef> orisToRevert = new ArrayList<>();
			ISet<Object> persistedObjectsToRevert = new IdentityHashSet<>();
			for (int a = initializedObjects.size(); a-- > 0;) {
				Object objectToRevert = initializedObjects.get(a);
				IEntityMetaData metaData = ((IEntityMetaDataHolder) objectToRevert).get__EntityMetaData();
				Object id = metaData.getIdMember().getValue(objectToRevert, false);

				if (id == null) {
					directObjectDeletes.add(new DirectDataChangeEntry(objectToRevert));
					continue;
				}
				persistedObjectsToRevert.add(objectToRevert);
				orisToRevert.add(new ObjRef(metaData.getEntityType(), ObjRef.PRIMARY_KEY_INDEX, id, null));
			}
			IList<Object> hardRefsToRootCacheValues = rootCache.getObjects(orisToRevert,
					EnumSet.of(CacheDirective.CacheValueResult, CacheDirective.ReturnMisses));

			for (int a = orisToRevert.size(); a-- > 0;) {
				if (hardRefsToRootCacheValues.get(a) == null) {
					// Object could not be loaded/retrieved any more. So the ori refers to an
					// invalid object. We can not revert invalid objects and currently ignore them.
					// They will raise exceptions if they will be tried to persist in a merge
					// process any time in the future
					orisToRevert.remove(a);
				}
			}
			// We do nothing with the hardRef-list from the RootCache. It is only necessary to keep
			// track of the instance reference on the stack
			// To prohibit GC any potential WeakReferences in the meantime....
			success2.setValue(Boolean.FALSE);
			callWaitEventToResumeInGui(directObjectDeletes, orisToRevert, persistedObjectsToRevert,
					objectsToRevert, hardRefsToRootCacheValues, success1, success2, success3,
					revertChangesFinishedCallback);
		}
		finally

		{
			if (revertChangesFinishedCallback != null && success2 == null && success3 == null) {
				revertChangesFinishedCallback.invoke(success1);
			}
		}
	}

	protected void waitEventToResume(
			IBackgroundWorkerParamDelegate<IProcessResumeItem> resumeDelegate,
			IBackgroundWorkerParamDelegate<Throwable> errorDelegate) {
		IRootCache rootCache = this.rootCache;
		IList<IWritableCache> selectedFirstLevelCaches =
				firstLevelCacheManager.selectFirstLevelCaches();

		ISet<Object> collisionSet = new IdentityHashSet<>();
		collisionSet.add(rootCache);
		for (int a = selectedFirstLevelCaches.size(); a-- > 0;) {
			collisionSet.add(selectedFirstLevelCaches.get(a));
		}
		// Without the current rootcache we can not handle the event now. We have to block till the
		// rootCache and all childCaches get valid
		eventDispatcher.waitEventToResume(collisionSet, -1, resumeDelegate, errorDelegate);
	}
}
