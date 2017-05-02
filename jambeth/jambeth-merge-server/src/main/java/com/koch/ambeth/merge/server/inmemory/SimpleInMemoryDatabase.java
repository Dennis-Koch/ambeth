package com.koch.ambeth.merge.server.inmemory;

/*-
 * #%L
 * jambeth-merge-server
 * %%
 * Copyright (C) 2017 Koch Softwaredevelopment
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 * #L%
 */

import java.util.Collection;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import javax.persistence.OptimisticLockException;
import javax.persistence.PersistenceException;

import com.koch.ambeth.cache.IRootCache;
import com.koch.ambeth.cache.RootCache;
import com.koch.ambeth.cache.service.ICacheRetriever;
import com.koch.ambeth.cache.transfer.LoadContainer;
import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.threadlocal.IThreadLocalCleanupBean;
import com.koch.ambeth.merge.IMergeServiceExtension;
import com.koch.ambeth.merge.ITransactionState;
import com.koch.ambeth.merge.cache.AbstractCacheValue;
import com.koch.ambeth.merge.cache.CacheDirective;
import com.koch.ambeth.merge.compositeid.ICompositeIdFactory;
import com.koch.ambeth.merge.copy.IObjectCopier;
import com.koch.ambeth.merge.incremental.IIncrementalMergeState;
import com.koch.ambeth.merge.metadata.IObjRefFactory;
import com.koch.ambeth.merge.model.ICUDResult;
import com.koch.ambeth.merge.model.IChangeContainer;
import com.koch.ambeth.merge.model.IOriCollection;
import com.koch.ambeth.merge.model.IPrimitiveUpdateItem;
import com.koch.ambeth.merge.model.IRelationUpdateItem;
import com.koch.ambeth.merge.proxy.PersistenceContext;
import com.koch.ambeth.merge.proxy.PersistenceContextType;
import com.koch.ambeth.merge.server.service.ChangeAggregator;
import com.koch.ambeth.merge.server.service.IChangeAggregator;
import com.koch.ambeth.merge.transfer.CreateContainer;
import com.koch.ambeth.merge.transfer.DeleteContainer;
import com.koch.ambeth.merge.transfer.ObjRef;
import com.koch.ambeth.merge.transfer.OriCollection;
import com.koch.ambeth.merge.transfer.UpdateContainer;
import com.koch.ambeth.persistence.api.IContextProvider;
import com.koch.ambeth.persistence.api.IDatabase;
import com.koch.ambeth.persistence.event.DatabaseAcquireEvent;
import com.koch.ambeth.persistence.event.DatabaseFailEvent;
import com.koch.ambeth.persistence.event.DatabasePreCommitEvent;
import com.koch.ambeth.service.cache.model.ILoadContainer;
import com.koch.ambeth.service.cache.model.IObjRelation;
import com.koch.ambeth.service.cache.model.IObjRelationResult;
import com.koch.ambeth.service.merge.IEntityMetaDataProvider;
import com.koch.ambeth.service.merge.IValueObjectConfig;
import com.koch.ambeth.service.merge.model.IEntityMetaData;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.service.metadata.Member;
import com.koch.ambeth.service.metadata.RelationMember;
import com.koch.ambeth.util.IConversionHelper;
import com.koch.ambeth.util.Lock;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.HashSet;
import com.koch.ambeth.util.collections.IList;
import com.koch.ambeth.util.collections.IMap;
import com.koch.ambeth.util.collections.ISet;
import com.koch.ambeth.util.collections.IdentityHashMap;
import com.koch.ambeth.util.model.IMethodDescription;

@PersistenceContext(PersistenceContextType.NOT_REQUIRED)
public class SimpleInMemoryDatabase implements ICacheRetriever, IMergeServiceExtension,
		IInitializingBean, IInMemoryDatabase, IThreadLocalCleanupBean {
	protected static final Set<CacheDirective> failEntryLoadContainerResult =
			EnumSet.of(CacheDirective.FailEarly, CacheDirective.LoadContainerResult);

	protected static final Object[] EMPTY_PRIMITIVES = new Object[0];

	@Autowired
	protected IServiceContext beanContext;

	@Autowired
	protected ICompositeIdFactory compositeIdFactory;

	@Autowired
	protected IConversionHelper conversionHelper;

	@Autowired
	protected IDatabase database;

	@Autowired
	protected IEntityMetaDataProvider entityMetaDataProvider;

	@Autowired
	protected IObjectCopier objectCopier;

	@Autowired
	protected IObjRefFactory objRefFactory;

	@Autowired
	protected ITransactionState transactionState;

	protected RootCache committedData;

	protected final java.util.concurrent.locks.Lock sequenceLock = new ReentrantLock();

	protected final ThreadLocal<SimpleInMemorySession> sessionTL = new ThreadLocal<>();

	protected final HashSet<IObjRef> pendingChangesInSessionsMap = new HashSet<>();

	protected long sequenceValue = 0;

	@Override
	public void afterPropertiesSet() throws Throwable {
		committedData = beanContext.registerBean(RootCache.class)//
				.propertyValue("WeakEntries", Boolean.FALSE)//
				.propertyValue("Privileged", Boolean.TRUE)//
				.ignoreProperties("CacheRetriever", "EventQueue", "PrivilegeProvider", "SecurityActivation",
						"SecurityScopeProvider")//
				.finish();
	}

	@Override
	public void cleanupThreadLocal() {
		sessionTL.remove();
	}

	//
	// protected LoadContainer createLoadContainer(IObjRef ori, Object entity)
	// {
	// LoadContainer loadContainer = new LoadContainer();
	// IEntityMetaData metaData = this.entityMetaDataProvider.getMetaData(construction.getClass());
	//
	// // if (((Long) construction.getId()).equals(ori.getId()))
	// // {
	// loadContainer.setPrimitives(this.cacheHelper.extractPrimitives(metaData, construction));
	// loadContainer.setRelations(this.cacheHelper.extractRelations(metaData, construction));
	// // }
	// IObjRef oriForResult = this.objRefHelper.entityToObjRef(construction);
	// loadContainer.setReference(oriForResult);
	//
	// return loadContainer;
	// }

	@Override
	public void initialSetup(Collection<?> entities) {
		if (transactionState.isTransactionActive()) {
			throw new UnsupportedOperationException();
		}
		committedData.put(entities);
	}

	// @Override
	// public void handlePostBegin(long sessionId) throws Throwable
	// {
	// handleDatabaseAcquire(Long.valueOf(sessionId));
	// }
	//
	// @Override
	// public void handlePostRollback(long sessionId) throws Throwable
	// {
	// handleDatabaseFail(Long.valueOf(sessionId));
	// }
	//
	// @Override
	// public void handlePreCommit(long sessionId) throws Throwable
	// {
	// handleDatabaseCommit(Long.valueOf(sessionId));
	// }

	// @Override
	// public void handleEvent(Object eventObject, long dispatchTime, long sequenceId) throws
	// Exception
	// {
	// // if (eventObject instanceof DatabaseAcquireEvent)
	// // {
	// // handleDatabaseAcquire(Long.valueOf(((DatabaseAcquireEvent) eventObject).getSessionId()));
	// // return;
	// // }
	// // if (eventObject instanceof DatabaseFailEvent)
	// // {
	// // handleDatabaseFail(Long.valueOf(((DatabaseFailEvent) eventObject).getSessionId()));
	// // return;
	// // }
	// }

	protected SimpleInMemorySession getOrCreateSession() {
		SimpleInMemorySession session = sessionTL.get();
		if (session != null) {
			return session;
		}
		RootCache transactionalData = beanContext.registerBean(RootCache.class)//
				.propertyValue("WeakEntries", Boolean.FALSE)//
				.propertyValue("Privileged", Boolean.TRUE)//
				.propertyValue("CacheRetriever", committedData)//
				.ignoreProperties("EventQueue", "PrivilegeProvider", "SecurityActivation",
						"SecurityScopeProvider")//
				.finish();
		session = new SimpleInMemorySession(this, transactionalData);
		sessionTL.set(session);
		return session;
	}

	public void handleDatabaseAcquire(DatabaseAcquireEvent evnt) {
		// intended blank
	}

	public void handleDatabaseFail(DatabaseFailEvent evnt) {
		SimpleInMemorySession state = sessionTL.get();
		if (state != null) {
			sessionTL.remove();
			state.dispose();
		}
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	public void handleDatabasePreCommit(DatabasePreCommitEvent evnt) {
		SimpleInMemorySession session = sessionTL.get();
		if (session == null) {
			// nothing do to
			return;
		}
		sessionTL.remove();
		try {
			if (session.createdObjRefs.size() == 0 && session.updatedObjRefs.size() == 0
					&& session.deletedObjRefs.size() == 0) {
				return;
			}
			// Save information into second level cache for committed data
			IList<IObjRef> deletedObjRefs = session.deletedObjRefs.toList();
			IList<IObjRef> createdObjRefs = session.createdObjRefs.toList();
			IList<IObjRef> updatedObjRefs = session.updatedObjRefs.toList();
			List updatedContent =
					session.data.getObjects(updatedObjRefs, CacheDirective.cacheValueResult());
			List createdContent =
					session.data.getObjects(createdObjRefs, CacheDirective.cacheValueResult());
			Lock writeLock = committedData.getWriteLock();
			writeLock.lock();
			try {
				ArrayList<IObjRef> existingObjRefs = new ArrayList<>();
				existingObjRefs.addAll(deletedObjRefs);
				existingObjRefs.addAll(updatedObjRefs);

				ArrayList<Object> changedContent = new ArrayList<>();
				changedContent.addAll(createdContent);
				changedContent.addAll(updatedContent);

				if (existingObjRefs.size() > 0) {
					IList<Object> existingCommittedValues = committedData.getObjects(existingObjRefs,
							EnumSet.of(CacheDirective.ReturnMisses, CacheDirective.CacheValueResult));
					for (int a = existingCommittedValues.size(); a-- > 0;) {
						IObjRef objRef = existingObjRefs.get(a);
						AbstractCacheValue existingCommittedValue =
								(AbstractCacheValue) existingCommittedValues.get(a);
						if (existingCommittedValue == null) {
							throw new OptimisticLockException("Object not found or outdated: " + objRef);
						}
						IEntityMetaData metaData = entityMetaDataProvider.getMetaData(objRef.getRealType());
						checkVersionForOptimisticLock(metaData, objRef, existingCommittedValue);
					}
				}
				if (deletedObjRefs.size() > 0) {
					committedData.remove(deletedObjRefs);
				}
				if (changedContent.size() > 0) {
					committedData.put(changedContent);
				}
			}
			finally {
				writeLock.unlock();
			}
		}
		finally {
			session.dispose();
		}
	}

	protected IRootCache getData() {
		SimpleInMemorySession session = sessionTL.get();
		if (session == null) {
			return committedData;
		}
		return session.data;
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	@Override
	public List<ILoadContainer> getEntities(List<IObjRef> orisToLoad) {
		IList result = getData().getObjects(orisToLoad, CacheDirective.loadContainerResult());
		return result;
	}

	@Override
	public List<IObjRelationResult> getRelations(List<IObjRelation> objRelations) {
		return getData().getObjRelations(objRelations, null, CacheDirective.none());
	}

	@Override
	public ICUDResult evaluateImplictChanges(ICUDResult cudResult,
			IIncrementalMergeState incrementalState) {
		return cudResult;
	}

	@Override
	@PersistenceContext(PersistenceContextType.REQUIRED)
	public IOriCollection merge(ICUDResult cudResult, IMethodDescription methodDescription) {
		if (!transactionState.isTransactionActive()) {
			throw new IllegalStateException(
					"No transaction active. This operation-mode is currently not supported!");
		}
		IConversionHelper conversionHelper = this.conversionHelper;
		IEntityMetaDataProvider entityMetaDataProvider = this.entityMetaDataProvider;
		List<IChangeContainer> changes = cudResult.getAllChanges();
		IdentityHashMap<IObjRef, IObjRef> givenObjRefToCopyMap = new IdentityHashMap<>();
		IdentityHashMap<IObjRef, ILoadContainer> alreadyAcquiredLoadContainerMap =
				new IdentityHashMap<>();

		SimpleInMemorySession session = getOrCreateSession();
		ArrayList<IObjRef> objRefList = new ArrayList<>(changes.size());
		LoadContainer[] newLCs = new LoadContainer[changes.size()];
		com.koch.ambeth.util.Lock writeLock = session.data.getWriteLock();
		writeLock.lock();
		try {
			buildCopyOfAllObjRefs(changes, givenObjRefToCopyMap);
			IObjRef[] changedOrDeletedObjRefs = new IObjRef[changes.size()];
			for (int a = 0, size = changes.size(); a < size; a++) {
				IChangeContainer changeContainer = changes.get(a);
				if (changeContainer instanceof CreateContainer) {
					continue;
				}
				IObjRef objRef = changeContainer.getReference();
				objRef = givenObjRefToCopyMap.get(objRef);
				changedOrDeletedObjRefs[a] = objRef;
			}
			IList<Object> existingLCs = session.data.getObjects(changedOrDeletedObjRefs,
					EnumSet.of(CacheDirective.LoadContainerResult, CacheDirective.ReturnMisses));
			for (int a = 0, size = changes.size(); a < size; a++) {
				IChangeContainer changeContainer = changes.get(a);
				ILoadContainer existingLC = (ILoadContainer) existingLCs.get(a);
				IObjRef objRef = changeContainer.getReference();
				objRef = givenObjRefToCopyMap.get(objRef);
				boolean isUpdate = (changeContainer instanceof UpdateContainer);
				IEntityMetaData metaData = entityMetaDataProvider.getMetaData(objRef.getRealType());
				if (!(changeContainer instanceof CreateContainer)) {
					if (existingLC == null) {
						throw new OptimisticLockException(
								"Object not found to " + (isUpdate ? "update: " : "delete: ") + objRef);
					}
					checkVersionForOptimisticLock(metaData, objRef, existingLC);
					if (!isUpdate) {
						continue;
					}
				}
				IPrimitiveUpdateItem[] puis;
				IRelationUpdateItem[] ruis;
				if (isUpdate) {
					alreadyAcquiredLoadContainerMap.put(objRef, existingLC);
					puis = ((UpdateContainer) changeContainer).getPrimitives();
					ruis = ((UpdateContainer) changeContainer).getRelations();
				}
				else {
					puis = ((CreateContainer) changeContainer).getPrimitives();
					ruis = ((CreateContainer) changeContainer).getRelations();
				}

				LoadContainer newLC = createNewLoadContainer(metaData, objRef, isUpdate);
				if (isUpdate) {
					Object[] primitives = existingLC.getPrimitives();
					if (primitives.length > 0) {
						Object[] newPrimitives = newLC.getPrimitives();
						System.arraycopy(primitives, 0, newPrimitives, 0, primitives.length);
					}
					updatePrimitives(metaData, puis, newLC);
					setUpdated(metaData, newLC);
					IObjRef[][] relations = existingLC.getRelations();
					if (relations.length > 0) {
						IObjRef[][] newRelations = newLC.getRelations();
						System.arraycopy(relations, 0, newRelations, 0, relations.length);
						for (int b = newRelations.length; b-- > 0;) {
							IObjRef[] relationsOfMember = newRelations[b];
							if (relationsOfMember == null || relationsOfMember.length == 0) {
								continue;
							}
							IObjRef[] newRelationsOfMember = new IObjRef[relationsOfMember.length];
							for (int c = relationsOfMember.length; c-- > 0;) {
								newRelationsOfMember[c] =
										dupIfNecessary(relationsOfMember[c], givenObjRefToCopyMap);
							}
							newRelations[b] = newRelationsOfMember;
						}
					}
				}
				else {
					updatePrimitives(metaData, puis, newLC);
					setCreated(metaData, newLC);
					if (metaData.getVersionMember() == null) {
						newLC.getReference().setVersion(null);
					}
					else {
						Object version = conversionHelper
								.convertValueToType(metaData.getVersionMember().getRealType(), Integer.valueOf(1));
						newLC.getReference().setVersion(version);
					}
				}
				updateRelations(metaData, ruis, newLC, givenObjRefToCopyMap);
				newLCs[a] = newLC;
			}
			markChangesOfSession(changedOrDeletedObjRefs, session);
			doChanges(newLCs, changes, objRefList, givenObjRefToCopyMap, session);

			OriCollection oriCollection = new OriCollection(objectCopier.clone(objRefList));
			IContextProvider contextProvider = database.getContextProvider();
			oriCollection.setChangedBy(contextProvider.getCurrentUser());
			oriCollection.setChangedOn(contextProvider.getCurrentTime());
			return oriCollection;
		}
		finally {
			writeLock.unlock();
		}
	}

	protected void markChangesOfSession(IObjRef[] changedOrDeletedObjRefs,
			SimpleInMemorySession session) {
		sequenceLock.lock();
		try {
			for (int a = changedOrDeletedObjRefs.length; a-- > 0;) {
				IObjRef changedOrDeletedObjRef = changedOrDeletedObjRefs[a];
				if (changedOrDeletedObjRef == null) {
					// this is an insert which can never collide due to our consistent sequencing
					continue;
				}
				// check if it is a new change in our session
				if (!session.changesOfSession.add(changedOrDeletedObjRef)) {
					IObjRef existingChangedOrDeletedObjRef =
							session.changesOfSession.get(changedOrDeletedObjRef);
					existingChangedOrDeletedObjRef.setVersion(changedOrDeletedObjRef.getVersion());
					// is is nothing new for our session. so we know we have already registered it globally,
					// too
					continue;
				}
				if (pendingChangesInSessionsMap.add(changedOrDeletedObjRef)) {
					// everything ok our global registration was a success
					continue;
				}
				// cleanup previous successful (but not obsolete) registrations in the global map
				int cleanupIndex = a + 1;
				for (int b = changedOrDeletedObjRefs.length; b-- > cleanupIndex;) {
					pendingChangesInSessionsMap.remove(changedOrDeletedObjRefs[b]);
				}
				IObjRef concurrentTransactionObjRef =
						pendingChangesInSessionsMap.get(changedOrDeletedObjRef);
				throw new OptimisticLockException("Object is already modified in a concurrent transaction: "
						+ concurrentTransactionObjRef);
			}
		}
		finally {
			sequenceLock.unlock();
		}
	}

	public void releaseChangesOfSession(ISet<IObjRef> changesOfSession) {
		sequenceLock.lock();
		try {
			pendingChangesInSessionsMap.removeAll(changesOfSession);
		}
		finally {
			sequenceLock.unlock();
		}
	}

	protected IObjRef dupIfNecessary(IObjRef objRef, IMap<IObjRef, IObjRef> givenObjRefToCopyMap) {
		IObjRef objRefDup = givenObjRefToCopyMap.get(objRef);
		if (objRefDup != null) {
			return objRefDup;
		}
		objRefDup = dupObjRef(objRef);
		givenObjRefToCopyMap.put(objRef, objRefDup);
		return objRefDup;
	}

	protected void buildCopyOfAllObjRefs(List<IChangeContainer> changes,
			IMap<IObjRef, IObjRef> givenObjRefToCopyMap) {
		sequenceLock.lock();
		try {
			for (int a = 0, size = changes.size(); a < size; a++) {
				buildCopyOfAllObjRefs(changes.get(a), givenObjRefToCopyMap);
			}
		}
		finally {
			sequenceLock.unlock();
		}
	}

	protected void buildCopyOfAllObjRefs(IChangeContainer changeContainer,
			IMap<IObjRef, IObjRef> givenObjRefToCopyMap) {
		if (changeContainer instanceof DeleteContainer) {
			dupIfNecessary(changeContainer.getReference(), givenObjRefToCopyMap);
			return;
		}
		IObjRef objRef = changeContainer.getReference();
		IRelationUpdateItem[] ruis = null;
		if (changeContainer instanceof CreateContainer) {
			IObjRef newObjRef = givenObjRefToCopyMap.get(objRef);
			if (newObjRef == null) {
				newObjRef = objRefFactory.createObjRef(objRef.getRealType(), ObjRef.PRIMARY_KEY_INDEX,
						Long.valueOf(++sequenceValue), null);
				givenObjRefToCopyMap.put(objRef, newObjRef);
				givenObjRefToCopyMap.put(newObjRef, newObjRef);
			}
			ruis = ((CreateContainer) changeContainer).getRelations();
		}
		else {
			dupIfNecessary(changeContainer.getReference(), givenObjRefToCopyMap);
			ruis = ((UpdateContainer) changeContainer).getRelations();
		}
		if (ruis == null) {
			return;
		}
		for (IRelationUpdateItem rui : ruis) {
			IObjRef[] addedORIs = rui.getAddedORIs();
			if (addedORIs != null) {
				for (IObjRef addedORI : addedORIs) {
					dupIfNecessary(addedORI, givenObjRefToCopyMap);;
				}
			}
			IObjRef[] removedORIs = rui.getRemovedORIs();
			if (removedORIs != null) {
				for (IObjRef removedORI : removedORIs) {
					dupIfNecessary(removedORI, givenObjRefToCopyMap);
				}
			}
		}
	}

	protected LoadContainer createNewLoadContainer(IEntityMetaData metaData, IObjRef objRef,
			boolean isUpdate) {
		if (metaData.getVersionMember() == null) {
			objRef.setVersion(null);
		}
		else if (isUpdate) {
			Number oldVersion = conversionHelper.convertValueToType(Number.class, objRef.getVersion());
			Object version = conversionHelper.convertValueToType(
					metaData.getVersionMember().getRealType(), Long.valueOf(oldVersion.longValue() + 1));
			objRef.setVersion(version);
		}
		else {
			Object version = conversionHelper
					.convertValueToType(metaData.getVersionMember().getRealType(), Long.valueOf(1));
			objRef.setVersion(version);
		}
		LoadContainer newLC = new LoadContainer();
		newLC.setReference(objRef);
		newLC.setPrimitives(metaData.getPrimitiveMembers().length == 0 ? EMPTY_PRIMITIVES
				: new Object[metaData.getPrimitiveMembers().length]);
		newLC.setRelations(metaData.getRelationMembers().length == 0 ? ObjRef.EMPTY_ARRAY_ARRAY
				: new IObjRef[metaData.getRelationMembers().length][]);
		return newLC;
	}

	protected IObjRef dupObjRef(IObjRef objRef) {
		return objRefFactory.dup(objRef);
	}

	protected void doChanges(ILoadContainer[] newLCs, List<IChangeContainer> changes,
			List<IObjRef> objRefList, Map<IObjRef, IObjRef> givenToInternalObjRefMap,
			SimpleInMemorySession session) {
		IChangeAggregator changeAggregator = beanContext.registerBean(ChangeAggregator.class).finish();
		IList<IObjRef> toRemove = new ArrayList<>(newLCs.length);
		for (int a = newLCs.length; a-- > 0;) {
			IObjRef objRef = givenToInternalObjRefMap.get(changes.get(a).getReference());
			toRemove.add(objRef);
		}
		session.data.remove(toRemove);
		session.data.put(newLCs);
		for (int a = 0, size = newLCs.length; a < size; a++) {
			ILoadContainer newLC = newLCs[a];
			IObjRef oldObjRef = dupObjRef(changes.get(a).getReference());
			if (newLC == null) {
				objRefList.add(null);
				changeAggregator.dataChangeDelete(oldObjRef);
				if (session.createdObjRefs.remove(oldObjRef)) {
					// object has been created & deleted within same session
					// nothing to do regarding commit
					continue;
				}
				if (session.updatedObjRefs.remove(oldObjRef)) {
					// object has been updated & deleted within same session
				}
				// so our delete is the real thing we are interested in
				session.deletedObjRefs.add(oldObjRef); // intentionally the "oldObjRef" because on the
																								// OptimisticLockCheck in preCommit we need the
																								// ORIGINAL
				// version
				continue;
			}
			IObjRef objRef = newLC.getReference();
			objRefList.add(objRef);

			if (changes.get(a) instanceof CreateContainer) {
				changeAggregator.dataChangeInsert(objRef);
				session.createdObjRefs.add(objRef);
			}
			else {
				if (session.createdObjRefs.contains(objRef)) {
					// object has been created & update within same session
					// so the "created" is the main important thing regarding commit
					continue;
				}
				session.updatedObjRefs.add(oldObjRef); // intentionally the "oldObjRef" because on the
																								// OptimisticLockCheck in preCommit we need the
																								// ORIGINAL
				// version
				changeAggregator.dataChangeUpdate(objRef);
			}
		}
		changeAggregator.createDataChange();
	}

	@SuppressWarnings("unchecked")
	protected void checkVersionForOptimisticLock(IEntityMetaData metaData, IObjRef objRef,
			Object oldLC) {
		Object requestedVersion = conversionHelper
				.convertValueToType(metaData.getVersionMember().getRealType(), objRef.getVersion());
		if (requestedVersion == null) {
			throw new OptimisticLockException("Mandatory entity version not provided: " + objRef);
		}
		Object existingVersion;
		if (oldLC instanceof ILoadContainer) {
			existingVersion = ((ILoadContainer) oldLC).getReference().getVersion();
		}
		else {
			existingVersion = ((AbstractCacheValue) oldLC).getVersion();
		}
		if (((Comparable<Object>) requestedVersion).compareTo(existingVersion) != 0) {
			throw new OptimisticLockException("Provided entity version not valid: " + objRef
					+ ". Expected version: " + existingVersion);
		}
	}

	protected void setCreated(IEntityMetaData metaData, ILoadContainer lc) {
		IContextProvider contextProvider = database.getContextProvider();
		String currentUser = contextProvider.getCurrentUser();
		Long currentTime = contextProvider.getCurrentTime();
		Object[] primitives = lc.getPrimitives();
		if (metaData.getCreatedByMember() != null) {
			int primitiveIndex = metaData.getIndexByPrimitive(metaData.getCreatedByMember());
			primitives[primitiveIndex] = currentUser;
		}
		if (metaData.getUpdatedByMember() != null) {
			int primitiveIndex = metaData.getIndexByPrimitive(metaData.getUpdatedByMember());
			primitives[primitiveIndex] = currentTime;
		}
	}

	protected void setUpdated(IEntityMetaData metaData, ILoadContainer lc) {
		IContextProvider contextProvider = database.getContextProvider();
		String currentUser = contextProvider.getCurrentUser();
		Long currentTime = contextProvider.getCurrentTime();
		Object[] primitives = lc.getPrimitives();
		if (metaData.getUpdatedByMember() != null) {
			int primitiveIndex = metaData.getIndexByPrimitive(metaData.getUpdatedByMember());
			primitives[primitiveIndex] = currentUser;
		}
		if (metaData.getUpdatedOnMember() != null) {
			int primitiveIndex = metaData.getIndexByPrimitive(metaData.getUpdatedOnMember());
			primitives[primitiveIndex] = currentTime;
		}
	}

	protected void updatePrimitives(IEntityMetaData metaData, IPrimitiveUpdateItem[] puis,
			ILoadContainer lc) {
		if (puis == null) {
			return;
		}
		IConversionHelper conversionHelper = this.conversionHelper;
		Object[] primitives = lc.getPrimitives();
		Member[] primitiveMembers = metaData.getPrimitiveMembers();
		for (int a = puis.length; a-- > 0;) {
			IPrimitiveUpdateItem pui = puis[a];
			int primitiveIndex = metaData.getIndexByPrimitiveName(pui.getMemberName());
			Member primitiveMember = primitiveMembers[primitiveIndex];

			Object value =
					conversionHelper.convertValueToType(primitiveMember.getRealType(), pui.getNewValue());
			if (value instanceof Date) {
				// optimize later clone performance (because Long is immutable)
				value = conversionHelper.convertValueToType(Long.class, value);
			}
			primitives[primitiveIndex] = value;
		}
	}

	protected void updateRelations(IEntityMetaData metaData, IRelationUpdateItem[] ruis,
			ILoadContainer lc, Map<IObjRef, IObjRef> givenToInternalObjRefMap) {
		if (ruis == null) {
			return;
		}
		IObjRef primaryObjRef = lc.getReference();
		IObjRef[][] relations = lc.getRelations();
		RelationMember[] relationMembers = metaData.getRelationMembers();
		for (int a = ruis.length; a-- > 0;) {
			IRelationUpdateItem rui = ruis[a];
			int relationIndex = metaData.getIndexByRelationName(rui.getMemberName());
			RelationMember relationMember = relationMembers[relationIndex];

			IObjRef[] existingObjRefs = relations[relationIndex];
			ISet<IObjRef> existingObjRefsSet =
					existingObjRefs != null ? new HashSet<>(existingObjRefs) : new HashSet<IObjRef>();
			IObjRef[] addedObjRefs = rui.getAddedORIs();
			IObjRef[] removedObjRefs = rui.getRemovedORIs();
			if (removedObjRefs != null) {
				for (IObjRef removedObjRef : removedObjRefs) {

					if (!existingObjRefsSet.remove(removedObjRef)) {
						throw new PersistenceException("Relation to remove does not exist: " + removedObjRef
								+ " on member '" + relationMember.getName() + "' " + primaryObjRef + "");
					}
				}
			}
			if (addedObjRefs != null) {
				for (IObjRef addedObjRef : addedObjRefs) {
					if (!existingObjRefsSet.add(givenToInternalObjRefMap.get(addedObjRef))) {
						throw new PersistenceException("Relation to add does already exist: " + addedObjRef
								+ " on member '" + relationMember.getName() + "' " + primaryObjRef + "");
					}
				}
			}
			relations[relationIndex] = existingObjRefsSet.size() == 0 ? ObjRef.EMPTY_ARRAY
					: existingObjRefsSet.toArray(IObjRef.class);
		}
	}

	@Override
	public List<IEntityMetaData> getMetaData(List<Class<?>> entityTypes) {
		throw new UnsupportedOperationException("Must never happen");
	}

	@Override
	public IValueObjectConfig getValueObjectConfig(Class<?> valueType) {
		throw new UnsupportedOperationException("Must never happen");
	}

	@Override
	public String createMetaDataDOT() {
		throw new UnsupportedOperationException("Must never happen");
	}
}
