package com.koch.ambeth.audit.server;

import java.io.IOException;

/*-
 * #%L
 * jambeth-audit-server
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

import java.security.Signature;
import java.security.SignatureException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.locks.Lock;
import java.util.function.BiFunction;

import com.koch.ambeth.audit.IAuditEntryVerifier;
import com.koch.ambeth.audit.model.AuditedEntityChangeType;
import com.koch.ambeth.audit.model.IAuditEntry;
import com.koch.ambeth.audit.model.IAuditedEntity;
import com.koch.ambeth.audit.model.IAuditedEntityPrimitiveProperty;
import com.koch.ambeth.audit.model.IAuditedEntityRef;
import com.koch.ambeth.audit.model.IAuditedEntityRelationProperty;
import com.koch.ambeth.audit.model.IAuditedEntityRelationPropertyItem;
import com.koch.ambeth.audit.server.config.AuditConfigurationConstants;
import com.koch.ambeth.cache.audit.IVerifyOnLoad;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.ioc.threadlocal.Forkable;
import com.koch.ambeth.ioc.threadlocal.IForkProcessor;
import com.koch.ambeth.ioc.threadlocal.IThreadLocalCleanupBean;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.merge.ILightweightTransaction;
import com.koch.ambeth.merge.IObjRefHelper;
import com.koch.ambeth.merge.cache.CacheDirective;
import com.koch.ambeth.merge.cache.ICache;
import com.koch.ambeth.merge.cache.ICacheContext;
import com.koch.ambeth.merge.cache.ICacheFactory;
import com.koch.ambeth.merge.metadata.IObjRefFactory;
import com.koch.ambeth.merge.metadata.IPreparedObjRefFactory;
import com.koch.ambeth.merge.proxy.IEntityMetaDataHolder;
import com.koch.ambeth.merge.proxy.IObjRefContainer;
import com.koch.ambeth.merge.security.ISecurityActivation;
import com.koch.ambeth.merge.transfer.ObjRef;
import com.koch.ambeth.merge.util.DirectValueHolderRef;
import com.koch.ambeth.merge.util.IPrefetchHandle;
import com.koch.ambeth.merge.util.IPrefetchHelper;
import com.koch.ambeth.merge.util.IPrefetchState;
import com.koch.ambeth.persistence.api.IDatabase;
import com.koch.ambeth.persistence.api.ITable;
import com.koch.ambeth.query.IOperand;
import com.koch.ambeth.query.IOperator;
import com.koch.ambeth.query.IQuery;
import com.koch.ambeth.query.IQueryBuilder;
import com.koch.ambeth.query.IQueryBuilderFactory;
import com.koch.ambeth.query.OrderByType;
import com.koch.ambeth.query.persistence.IVersionCursor;
import com.koch.ambeth.query.persistence.IVersionItem;
import com.koch.ambeth.security.model.ISignature;
import com.koch.ambeth.security.server.ISignatureUtil;
import com.koch.ambeth.security.server.config.SecurityServerConfigurationConstants;
import com.koch.ambeth.service.cache.ClearAllCachesEvent;
import com.koch.ambeth.service.cache.model.ILoadContainer;
import com.koch.ambeth.service.merge.IEntityMetaDataProvider;
import com.koch.ambeth.service.merge.model.IEntityMetaData;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.service.metadata.Member;
import com.koch.ambeth.service.metadata.PrimitiveMember;
import com.koch.ambeth.service.metadata.RelationMember;
import com.koch.ambeth.util.EqualsUtil;
import com.koch.ambeth.util.IClassCache;
import com.koch.ambeth.util.IConversionHelper;
import com.koch.ambeth.util.StringBuilderUtil;
import com.koch.ambeth.util.codec.Base64;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.EmptySet;
import com.koch.ambeth.util.collections.HashMap;
import com.koch.ambeth.util.collections.HashSet;
import com.koch.ambeth.util.collections.ILinkedMap;
import com.koch.ambeth.util.collections.IList;
import com.koch.ambeth.util.collections.IMap;
import com.koch.ambeth.util.collections.ISet;
import com.koch.ambeth.util.collections.IdentityHashSet;
import com.koch.ambeth.util.collections.IdentityLinkedMap;
import com.koch.ambeth.util.collections.LinkedHashMap;
import com.koch.ambeth.util.collections.SmartCopyMap;
import com.koch.ambeth.util.collections.Tuple2KeyHashMap;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
import com.koch.ambeth.util.objectcollector.IThreadLocalObjectCollector;
import com.koch.ambeth.util.state.IStateRollback;
import com.koch.ambeth.util.threading.IBackgroundWorkerDelegate;
import com.koch.ambeth.util.threading.IResultingBackgroundWorkerDelegate;

public class AuditEntryVerifier
		implements IAuditEntryVerifier, IVerifyOnLoad, IThreadLocalCleanupBean {
	public static class ForkableProcessor implements IForkProcessor {
		@Override
		public Object createForkedValue(Object value) {
			if (value == null) {
				return null;
			}
			return new ArrayList<IObjRef>();
		}

		@Override
		public Object resolveOriginalValue(Object bean, String fieldName, ThreadLocal<?> fieldValueTL) {
			return fieldValueTL.get();
		}

		@SuppressWarnings("unchecked")
		@Override
		public void returnForkedValue(Object value, Object forkedValue) {
			if (forkedValue == null) {
				return;
			}
			((ArrayList<IObjRef>) value).addAll((Collection<? extends IObjRef>) forkedValue);
		}
	}

	public static final boolean[] EMPTY_VALIDATION_RESULT = new boolean[0];

	public static final String HANDLE_CLEAR_ALL_CACHES_EVENT = "handleClearAllCachesEvent";

	@LogInstance
	private ILogger log;

	@Autowired
	protected IAuditConfigurationProvider auditConfigurationProvider;

	@Autowired
	protected IAuditInfoController auditInfoController;

	@Autowired
	protected IAuditEntryToSignature auditEntryToSignature;

	@Autowired
	protected IAuditVerifyOnLoadTask auditVerifyOnLoadTask;

	@Autowired
	protected ICache cache;

	@Autowired
	protected ICacheContext cacheContext;

	@Autowired
	protected ICacheFactory cacheFactory;

	@Autowired
	protected IClassCache classCache;

	@Autowired
	protected IConversionHelper conversionHelper;

	@Autowired
	protected IDatabase database;

	@Autowired
	protected IEntityMetaDataProvider entityMetaDataProvider;

	@Autowired
	protected IThreadLocalObjectCollector objectCollector;

	@Autowired
	protected IObjRefFactory objRefFactory;

	@Autowired
	protected IObjRefHelper objRefHelper;

	@Autowired
	protected IPrefetchHelper prefetchHelper;

	@Autowired
	protected IQueryBuilderFactory queryBuilderFactory;

	@Autowired
	protected ISecurityActivation securityActivation;

	@Autowired
	protected ISignatureUtil signatureUtil;

	@Autowired
	protected ILightweightTransaction transaction;

	@Property(name = SecurityServerConfigurationConstants.SignatureActive, defaultValue = "false")
	protected boolean signatureActive;

	@Property(name = AuditConfigurationConstants.VerifyEntitiesOnLoad, defaultValue = "NONE")
	protected VerifyOnLoadMode verifyOnLoadMode;

	protected final SmartCopyMap<Integer, IQuery<IAuditedEntity>> entityTypeCountToQuery =
			new SmartCopyMap<>();

	// this feature is still alpha status: optimize audit entry verification scope (do only set it to
	// TRUE if you know what you do)
	protected boolean filterAuditedEntities = true;

	protected IPrefetchHandle pref_filterAuditedEntities, pref_verifyAuditEntries,
			prefetchSignaturesOfUser, prefetchSignaturesOfUserFromAuditedEntity,
			pref_verifyAuditEntriesFromAuditedEntity;

	protected int maxDebugItems = 50;

	@Forkable(processor = ForkableProcessor.class)
	protected final ThreadLocal<ArrayList<IObjRef>> objRefsToVerifyTL = new ThreadLocal<>();

	protected final ThreadLocal<Boolean> verifyOnStackTL = new ThreadLocal<>();

	@Override
	public void cleanupThreadLocal() {
		if (objRefsToVerifyTL.get() != null) {
			throw new IllegalStateException("Must never happen");
		}
	}

	protected IPrefetchHandle getPref_filterAuditedEntities() {
		if (pref_filterAuditedEntities != null) {
			return pref_filterAuditedEntities;
		}
		pref_filterAuditedEntities = prefetchHelper.createPrefetch()//
				.add(IAuditedEntity.class, IAuditedEntity.Entry, IAuditedEntity.Ref,
						IAuditedEntity.Primitives, IAuditedEntity.Relations)//
				.add(IAuditedEntityRelationProperty.class, IAuditedEntityRelationProperty.Items)//
				.add(IAuditedEntityRelationPropertyItem.class, IAuditedEntityRelationPropertyItem.Ref)//
				.build();
		return pref_filterAuditedEntities;
	}

	protected IPrefetchHandle getPref_verifyAuditEntries() {
		if (pref_verifyAuditEntries != null) {
			return pref_verifyAuditEntries;
		}
		pref_verifyAuditEntries = prefetchHelper.createPrefetch()//
				.add(IAuditEntry.class, IAuditEntry.Services, IAuditEntry.Entities)//
				.add(IAuditedEntity.class, IAuditedEntity.Ref, IAuditedEntity.Primitives,
						IAuditedEntity.Relations)//
				.add(IAuditedEntityRelationProperty.class, IAuditedEntityRelationProperty.Items)//
				.add(IAuditedEntityRelationPropertyItem.class, IAuditedEntityRelationPropertyItem.Ref)//
				.build();
		return pref_verifyAuditEntries;
	}

	protected IPrefetchHandle getPref_SignaturesOfUser() {
		if (prefetchSignaturesOfUser != null) {
			return prefetchSignaturesOfUser;
		}
		prefetchSignaturesOfUser = prefetchHelper.createPrefetch()//
				.add(IAuditEntry.class, IAuditEntry.SignatureOfUser)//
				.build();
		return prefetchSignaturesOfUser;
	}

	protected IPrefetchHandle getPref_SignaturesOfUserFromAuditedEntity() {
		if (prefetchSignaturesOfUserFromAuditedEntity != null) {
			return prefetchSignaturesOfUserFromAuditedEntity;
		}
		prefetchSignaturesOfUserFromAuditedEntity = prefetchHelper.createPrefetch()//
				.add(IAuditedEntity.class, IAuditedEntity.Entry + "." + IAuditEntry.SignatureOfUser)//
				.build();
		return prefetchSignaturesOfUserFromAuditedEntity;
	}

	protected IPrefetchHandle getPref_verifyAuditEntriesFromAuditedEntity() {
		if (pref_verifyAuditEntriesFromAuditedEntity != null) {
			return pref_verifyAuditEntriesFromAuditedEntity;
		}
		pref_verifyAuditEntriesFromAuditedEntity = prefetchHelper.createPrefetch()//
				.add(IAuditedEntity.class, IAuditedEntity.Ref, IAuditedEntity.Primitives,
						IAuditedEntity.Relations)//
				.add(IAuditedEntityRelationProperty.class, IAuditedEntityRelationProperty.Items)//
				.add(IAuditedEntityRelationPropertyItem.class, IAuditedEntityRelationPropertyItem.Ref)//
				.build();
		return pref_verifyAuditEntriesFromAuditedEntity;
	}

	public void handleClearAllCachesEvent(ClearAllCachesEvent evnt) {
		Lock writeLock = entityTypeCountToQuery.getWriteLock();
		writeLock.lock();
		try {
			for (IQuery<?> query : entityTypeCountToQuery.values()) {
				query.dispose();
			}
			entityTypeCountToQuery.clear();
		}
		finally {
			writeLock.unlock();
		}
	}

	protected IQuery<IAuditedEntity> resolveQuery(
			ILinkedMap<IEntityMetaData, IList<IObjRef>> bucketSortObjRefs) {
		IQuery<IAuditedEntity> query = entityTypeCountToQuery
				.get(Integer.valueOf(bucketSortObjRefs.size()));
		if (query != null) {
			return query;
		}
		IQueryBuilder<IAuditedEntity> qb = queryBuilderFactory.create(IAuditedEntity.class);

		IOperand entityTypeProp = qb.property(IAuditedEntity.Ref + "." + IAuditedEntityRef.EntityType);
		IOperand entityIdProp = qb.property(IAuditedEntity.Ref + "." + IAuditedEntityRef.EntityId);
		int index = 0;
		IOperator op = null;
		for (int a = bucketSortObjRefs.size(); a-- > 0;) {
			IOperator typeMatchOp = qb.isEqualTo(entityTypeProp, qb.valueName("param" + index++));
			IOperator idMatchOp = qb.isIn(entityIdProp, qb.valueName("param" + index++));
			IOperator matchOp = qb.and(typeMatchOp, idMatchOp);

			if (op == null) {
				op = matchOp;
			}
			else {
				op = qb.or(op, matchOp);
			}
		}
		query = qb
				.orderBy(qb.property(IAuditedEntity.Entry + "." + IAuditEntry.Timestamp), OrderByType.ASC)
				.build(op);

		entityTypeCountToQuery.put(Integer.valueOf(bucketSortObjRefs.size()), query);
		return query;
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	protected boolean isTrailedVersionTooNew(IEntityMetaData metaData, IObjRef tempObjRef,
			IAuditedEntityRef ref, IMap<IObjRef, IObjRefContainer> objRefToEntityMap) {
		IObjRefContainer entity = objRefToEntityMap.get(tempObjRef);

		Comparable versionOfEntity = (Comparable) metaData.getVersionMember().getValue(entity);
		Comparable versionOfAuditedEntityRef = conversionHelper
				.convertValueToType(versionOfEntity.getClass(), ref.getEntityVersion());

		// versionOfEntity is smaller than the audited version. This means we do not want to check the
		// WHOLE audit trail for this entity
		// but only to a specific time-point
		return (versionOfEntity.compareTo(versionOfAuditedEntityRef) < 0);
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	protected boolean isTrailedVersionTooOld(IEntityMetaData metaData, IObjRef tempObjRef,
			Object entity, Tuple2KeyHashMap<Class<?>, Object, Object> objRefToMaxVersionOfAuditTrailMap) {
		Comparable versionOfEntity = (Comparable) metaData.getVersionMember().getValue(entity);
		Comparable maxVersionOfAuditTrail = (Comparable) objRefToMaxVersionOfAuditTrailMap
				.get(tempObjRef.getRealType(), tempObjRef.getId());

		// maxVersionOfAuditTrail is smaller than the entity from cache. our audit trail is incomplete.
		// we need to re-search for AuditedEntity and reverify this
		// entity in another iteration
		return (maxVersionOfAuditTrail.compareTo(versionOfEntity) < 0);
	}

	protected void filterAuditedEntities(IList<IAuditedEntity> auditedEntities,
			IMap<IObjRef, ISet<String>> objRefToPrimitiveMap, IList<IObjRef> objRefs,
			boolean[] entitiesDataInvalid, ISet<IAuditedEntity> auditedEntitiesToVerify) {
		if (!filterAuditedEntities || auditedEntities.isEmpty()) {
			auditedEntitiesToVerify.addAll(auditedEntities);
			return;
		}
		@SuppressWarnings("unused")
		IPrefetchState prefetch = getPref_filterAuditedEntities().prefetch(auditedEntities);

		IConversionHelper conversionHelper = this.conversionHelper;
		IList<Object> entities = cache.getObjects(objRefs, CacheDirective.returnMisses());
		HashMap<IObjRef, IObjRefContainer> objRefToEntityMap = HashMap
				.<IObjRef, IObjRefContainer>create(objRefs.size());
		HashMap<IObjRef, HashMap<String, Boolean>> objRefToValidPropertyMap = HashMap
				.<IObjRef, HashMap<String, Boolean>>create(objRefs.size());
		ArrayList<DirectValueHolderRef> valueHoldersToPrefetch = new ArrayList<>(objRefs.size());
		for (int a = objRefs.size(); a-- > 0;) {
			IObjRef objRef = objRefs.get(a);
			IObjRefContainer entity = (IObjRefContainer) entities.get(a);
			objRefToEntityMap.put(objRef, entity);
			IEntityMetaData metaData = entity.get__EntityMetaData();
			HashMap<String, Boolean> invalidPropertyMap = HashMap.<String, Boolean>create(
					objRefToPrimitiveMap.get(objRef).size() + metaData.getRelationMembers().length);
			objRefToValidPropertyMap.put(objRef, invalidPropertyMap);

			for (RelationMember relationMember : metaData.getRelationMembers()) {
				valueHoldersToPrefetch.add(new DirectValueHolderRef(entity, relationMember));
			}
		}
		prefetchHelper.prefetch(valueHoldersToPrefetch);

		Tuple2KeyHashMap<Class<?>, String, IMap<String, Tuple2KeyHashMap<Class<?>, String, Boolean>>> objRefToRelationMap =
				Tuple2KeyHashMap
						.<Class<?>, String, IMap<String, Tuple2KeyHashMap<Class<?>, String, Boolean>>>create(
								objRefs.size());

		ObjRef tempObjRef = new ObjRef();
		tempObjRef.setIdNameIndex(ObjRef.PRIMARY_KEY_INDEX);

		ArrayList<IAuditedEntity> realAuditedEntities = new ArrayList<>(auditedEntities.size());

		try {
			for (int a = 0, size = auditedEntities.size(); a < size; a++) {
				IAuditedEntity auditedEntity = auditedEntities.get(a);
				IAuditedEntityRef ref = auditedEntity.getRef();
				Class<?> refEntityType = classCache.loadClass(ref.getEntityType());

				IEntityMetaData metaData = entityMetaDataProvider.getMetaData(refEntityType);
				Class<?> entityType = metaData.getEntityType();
				tempObjRef.setRealType(entityType);
				tempObjRef.setId(conversionHelper.convertValueToType(metaData.getIdMember().getRealType(),
						ref.getEntityId()));

				if (isTrailedVersionTooNew(metaData, tempObjRef, ref, objRefToEntityMap)) {
					continue;
				}
				realAuditedEntities.add(auditedEntity);
				RelationMember[] relationMembers = metaData.getRelationMembers();

				if (relationMembers.length > 0) {
					IMap<String, Tuple2KeyHashMap<Class<?>, String, Boolean>> relationsMap =
							objRefToRelationMap
									.get(refEntityType, ref.getEntityId());
					if (relationsMap == null) {
						relationsMap = HashMap.<String, Tuple2KeyHashMap<Class<?>, String, Boolean>>create(
								relationMembers.length);
						objRefToRelationMap.put(refEntityType, ref.getEntityId(), relationsMap);
					}
					List<? extends IAuditedEntityRelationProperty> relations = auditedEntity.getRelations();
					if (!relations.isEmpty()) {
						List<? extends IAuditedEntityRelationProperty> auditedEntityRelations = auditedEntity
								.getRelations();
						for (int b = auditedEntityRelations.size(); b-- > 0;) {
							IAuditedEntityRelationProperty relation = auditedEntityRelations.get(b);
							Tuple2KeyHashMap<Class<?>, String, Boolean> relationMap = relationsMap
									.get(relation.getName());
							if (relationMap == null) {
								relationMap = Tuple2KeyHashMap
										.<Class<?>, String, Boolean>create(relationMembers.length);
								relationsMap.put(relation.getName(), relationMap);
							}
							List<? extends IAuditedEntityRelationPropertyItem> items = relation.getItems();
							for (int c = items.size(); c-- > 0;) {
								IAuditedEntityRelationPropertyItem item = items.get(c);
								IAuditedEntityRef itemRef = item.getRef();
								Class<?> itemEntityType = classCache.loadClass(itemRef.getEntityType());
								switch (item.getChangeType()) {
									case ADD:
										relationMap.putIfNotExists(itemEntityType, itemRef.getEntityId(), Boolean.TRUE);
										break;
									case REMOVE:
										relationMap.removeIfValue(itemEntityType, itemRef.getEntityId(), Boolean.TRUE);
										break;
									default:
										throw RuntimeExceptionUtil
												.createEnumNotSupportedException(item.getChangeType());
								}
							}
						}
						auditedEntitiesToVerify.add(auditedEntity);
					}
				}
			}
			Tuple2KeyHashMap<Class<?>, Object, IMap<String, IAuditedEntity>> objRefToPreviousRefVersionMap =
					Tuple2KeyHashMap
							.create(objRefs.size());
			Tuple2KeyHashMap<Class<?>, Object, Object> objRefToMaxVersionOfAuditTrailMap =
					new Tuple2KeyHashMap<>();

			// audit entries are ordered by timestamp DESC. So the newest auditEntries are last
			// so we do a reverse iteration because we are interested in the LAST/RECENT assigned value to
			// primitives
			for (int a = realAuditedEntities.size(); a-- > 0;) {
				IAuditedEntity auditedEntity = realAuditedEntities.get(a);
				IAuditedEntityRef ref = auditedEntity.getRef();
				Class<?> refEntityType = classCache.loadClass(ref.getEntityType());
				IEntityMetaData metaData = entityMetaDataProvider.getMetaData(refEntityType);

				tempObjRef.setRealType(metaData.getEntityType());
				tempObjRef.setId(conversionHelper.convertValueToType(metaData.getIdMember().getRealType(),
						ref.getEntityId()));

				IMap<String, IAuditedEntity> previousRefVersionToEntityMap = objRefToPreviousRefVersionMap
						.get(tempObjRef.getRealType(), tempObjRef.getId());
				if (previousRefVersionToEntityMap == null) {
					previousRefVersionToEntityMap = new HashMap<>();
					objRefToPreviousRefVersionMap.put(tempObjRef.getRealType(), tempObjRef.getId(),
							previousRefVersionToEntityMap);
				}
				String refPreviousVersion = auditedEntity.getRefPreviousVersion();
				if (refPreviousVersion == null) {
					if (auditedEntity.getChangeType() != AuditedEntityChangeType.INSERT) {
						throw new IllegalStateException("Illegal null string for '"
								+ IAuditedEntity.RefPreviousVersion + "' of a non-INSERT " + auditedEntity);
					}
					refPreviousVersion = "";
				}
				else if (refPreviousVersion.isEmpty()) {
					throw new IllegalStateException("Illegal empty string for '"
							+ IAuditedEntity.RefPreviousVersion + "' of " + auditedEntity);
				}
				else if (auditedEntity.getChangeType() == AuditedEntityChangeType.INSERT) {
					throw new IllegalStateException("Illegal non-null string for '"
							+ IAuditedEntity.RefPreviousVersion + "' of a INSERT " + auditedEntity);
				}
				if (!previousRefVersionToEntityMap.putIfNotExists(refPreviousVersion, auditedEntity)) {
					throw new IllegalStateException("Must never happen");
				}

				// this method stores the "first hit" of an AuditedEntity for any given entity which
				// corresponds to the max (newest) version of the entity
				mapMaxEntityVersionOfAuditTrail(metaData, tempObjRef, ref,
						objRefToMaxVersionOfAuditTrailMap);

				ISet<String> remainingPropertyMapOfEntity = objRefToPrimitiveMap.get(tempObjRef);
				if (remainingPropertyMapOfEntity == null) {
					// this auditedEntity is not relevant for any of the remaining primitive mappings
					continue;
				}
				IObjRefContainer entity = objRefToEntityMap.get(tempObjRef);
				List<? extends IAuditedEntityPrimitiveProperty> primitives = auditedEntity.getPrimitives();
				for (int c = primitives.size(); c-- > 0;) {
					IAuditedEntityPrimitiveProperty primitive = primitives.get(c);
					String memberName = primitive.getName();
					if (!remainingPropertyMapOfEntity.remove(memberName)) {
						continue;
					}
					if (entity != null) {
						Member member = metaData.getMemberByName(memberName);
						String entityValue = auditInfoController
								.createAuditedValueOfEntityPrimitive(member.getValue(entity, true));
						HashMap<String, Boolean> validPropertyMap = objRefToValidPropertyMap.get(tempObjRef);

						String newValue = primitive.getNewValue();
						boolean valid = EqualsUtil.equals(newValue, entityValue);
						validPropertyMap.put(memberName, Boolean.valueOf(valid));
					}
					auditedEntitiesToVerify.add(auditedEntity);
				}
				if (!remainingPropertyMapOfEntity.isEmpty()) {
					continue;
				}
				// all auditEntries covering the last state-change of each primitive property of the current
				// entity have been resolved
				objRefToPrimitiveMap.remove(tempObjRef);

				if (objRefToPrimitiveMap.isEmpty()) {
					break;
				}
			}
			for (int a = objRefs.size(); a-- > 0;) {
				IObjRef objRef = objRefs.get(a);
				IObjRefContainer entity = objRefToEntityMap.get(objRef);

				IEntityMetaData metaData = entity.get__EntityMetaData();
				if (isTrailedVersionTooOld(metaData, objRef, entity, objRefToMaxVersionOfAuditTrailMap)) {
					entitiesDataInvalid[a] = true;
					continue;
				}
				tempObjRef.setRealType(metaData.getEntityType());
				tempObjRef.setId(conversionHelper.convertValueToType(metaData.getIdMember().getRealType(),
						objRef.getId()));

				IMap<String, Tuple2KeyHashMap<Class<?>, String, Boolean>> relationsMap = objRefToRelationMap
						.get(metaData.getEntityType(),
								conversionHelper.convertValueToType(String.class, objRef.getId()));

				HashMap<String, Boolean> validPropertyMap = objRefToValidPropertyMap.get(objRef);

				boolean atleastOnePropertyInvalid = false;

				RelationMember[] relationMembers = metaData.getRelationMembers();
				for (int relationIndex = relationMembers.length; relationIndex-- > 0;) {
					RelationMember relationMember = relationMembers[relationIndex];
					IList<IObjRef> relationsOfMember = objRefHelper
							.extractObjRefList(relationMember.getValue(entity), null);
					Tuple2KeyHashMap<Class<?>, String, Boolean> relationMap = relationsMap != null
							? relationsMap.get(relationMember.getName())
							: null;
					boolean valid;
					if (relationMap == null) {
						// no audit entry did note a change to this member. So it has to be still empty for the
						// whole lifetime of the entity
						valid = relationsOfMember.isEmpty();
					}
					else if (relationMap.size() != relationsOfMember.size()) {
						valid = false;
					}
					else {
						for (int b = relationsOfMember.size(); b-- > 0;) {
							IObjRef relationOfMember = relationsOfMember.get(b);
							if (relationOfMember.getIdNameIndex() != ObjRef.PRIMARY_KEY_INDEX) {
								throw new IllegalStateException(
										"ObjRef of member '" + relationMember.getName() + "' of entity instance '"
												+ entity + "' must hold a primary identifier: " + relationMember);
							}
							relationMap.remove(relationOfMember.getRealType(),
									conversionHelper.convertValueToType(String.class, relationOfMember.getId()));
						}
						valid = relationMap.isEmpty();
					}
					validPropertyMap.put(relationMember.getName(), Boolean.valueOf(valid));
					atleastOnePropertyInvalid |= !valid;
				}
				for (PrimitiveMember member : metaData.getPrimitiveMembers()) {
					Boolean valid = validPropertyMap.get(member.getName());
					if (valid == null) {
						continue;
					}
					atleastOnePropertyInvalid |= !valid.booleanValue();
				}
				if (!atleastOnePropertyInvalid) {
					// this is the major logic now to be safe against removal, addition or replacement of
					// AuditedEntities for a given ObjRef where the AuditedEntities by itself might all be
					// correctly verified but the overall CHAIN has been compromised (e.g. a specific
					// AuditedEntity
					IMap<String, IAuditedEntity> previousRefVersionToEntityMap = objRefToPreviousRefVersionMap
							.get(tempObjRef.getRealType(), tempObjRef.getId());
					if (previousRefVersionToEntityMap == null) {
						atleastOnePropertyInvalid = true;
					}
					else {
						for (Entry<String, IAuditedEntity> entry : previousRefVersionToEntityMap) {
							IAuditedEntityRef ref = entry.getValue().getRef();
							String refVersion = ref.getEntityVersion();
							IAuditedEntity followingAuditEntry = previousRefVersionToEntityMap.get(refVersion);
							if (followingAuditEntry == null) {
								// this is only allowed for the LAST audited entity in the chain so we check whether
								// this is the case here
								Object maxVersion = objRefToMaxVersionOfAuditTrailMap.get(tempObjRef.getRealType(),
										tempObjRef.getId());
								Object convertedRefVersion = conversionHelper
										.convertValueToType(metaData.getVersionMember().getRealType(), refVersion);
								if (!convertedRefVersion.equals(maxVersion)) {
									atleastOnePropertyInvalid = true;
									break;
								}
							}
						}
					}
				}
				entitiesDataInvalid[a] = atleastOnePropertyInvalid;
			}
		}
		catch (ClassNotFoundException e) {
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@SuppressWarnings("rawtypes")
	private void mapMaxEntityVersionOfAuditTrail(IEntityMetaData metaData, ObjRef tempObjRef,
			IAuditedEntityRef ref,
			Tuple2KeyHashMap<Class<?>, Object, Object> objRefToMaxVersionOfAuditTrailMap) {
		Comparable existingMaxVersion = (Comparable) objRefToMaxVersionOfAuditTrailMap
				.get(tempObjRef.getRealType(), tempObjRef.getId());
		if (existingMaxVersion == null) {
			Class<?> versionType = metaData.getVersionMember().getRealType();
			Comparable currentVersion = (Comparable) conversionHelper.convertValueToType(versionType,
					ref.getEntityVersion());
			objRefToMaxVersionOfAuditTrailMap.put(tempObjRef.getRealType(), tempObjRef.getId(),
					currentVersion);
		}
	}

	protected IMap<IObjRef, ISet<String>> fillNameToValueMap(IMap<String, Object> nameToValueMap,
			ILinkedMap<IEntityMetaData, IList<IObjRef>> bucketSortObjRefs) {
		HashMap<IObjRef, ISet<String>> remainingPropertyMap = filterAuditedEntities
				? new HashMap<IObjRef, ISet<String>>()
				: null;

		for (Entry<IEntityMetaData, IList<IObjRef>> entry : bucketSortObjRefs) {
			IEntityMetaData metaData = entry.getKey();
			IList<IObjRef> objRefsOfEntityType = entry.getValue();

			nameToValueMap.put("param" + nameToValueMap.size(), metaData.getEntityType());

			Object[] ids = new Object[objRefsOfEntityType.size()];
			for (int a = objRefsOfEntityType.size(); a-- > 0;) {
				ids[a] = objRefsOfEntityType.get(a).getId();
			}
			nameToValueMap.put("param" + nameToValueMap.size(), ids);

			if (!filterAuditedEntities) {
				continue;
			}
			PrimitiveMember[] primitiveMembers = metaData.getPrimitiveMembers();
			ISet<String> primitiveMembersSet = primitiveMembers.length > 0
					? HashSet.<String>create(primitiveMembers.length)
					: EmptySet.<String>emptySet();
			for (PrimitiveMember primitiveMember : primitiveMembers) {
				if (metaData.getUpdatedByMember() == primitiveMember
						|| metaData.getUpdatedOnMember() == primitiveMember
						|| metaData.getCreatedByMember() == primitiveMember
						|| metaData.getCreatedOnMember() == primitiveMember) {
					continue;
				}
				primitiveMembersSet.add(primitiveMember.getName());
			}
			for (int a = objRefsOfEntityType.size(); a-- > 0;) {
				IObjRef objRef = objRefsOfEntityType.get(a);
				remainingPropertyMap.put(objRef, primitiveMembersSet.isEmpty() ? primitiveMembersSet
						: new HashSet<>(primitiveMembersSet));
			}
		}
		return remainingPropertyMap;
	}

	@Override
	public void queueVerifyEntitiesOnLoad(IList<ILoadContainer> loadedEntities) {
		ArrayList<IObjRef> objRefsToVerify = objRefsToVerifyTL.get();
		if (objRefsToVerify == null) {
			return;
		}
		for (int a = 0, size = loadedEntities.size(); a < size; a++) {
			IObjRef objRef = loadedEntities.get(a).getReference();
			IEntityMetaData metaData = entityMetaDataProvider.getMetaData(objRef.getRealType());

			IAuditConfiguration auditConfiguration = auditConfigurationProvider
					.getAuditConfiguration(metaData.getEntityType());
			if (!auditConfiguration.isAuditActive()) {
				continue;
			}
			objRefsToVerify.add(objRefFactory.dup(objRef));
		}
	}

	@Override
	public <R> R verifyEntitiesOnLoad(final IResultingBackgroundWorkerDelegate<R> runnable)
			throws Exception {
		if (VerifyOnLoadMode.NONE.equals(verifyOnLoadMode)) {
			return runnable.invoke();
		}
		{
			ArrayList<IObjRef> objRefsToVerify = objRefsToVerifyTL.get();
			if (objRefsToVerify != null) {
				return runnable.invoke();
			}
		}

		final ArrayList<IObjRef> objRefsToVerify = new ArrayList<>();
		objRefsToVerifyTL.set(objRefsToVerify);
		try {
			R result = runnable.invoke();
			if (objRefsToVerify.isEmpty()) {
				return result;
			}
			IBackgroundWorkerDelegate verifyRunnable = new IBackgroundWorkerDelegate() {
				@Override
				public void invoke() throws Exception {
					Boolean verifyOnStack = verifyOnStackTL.get();
					if (verifyOnStack != null) {
						// if we are already in a verification chain we verify always synchronous to keep the
						// current transaction open
						auditVerifyOnLoadTask.verifyEntitiesSync(objRefsToVerify);
						return;
					}
					switch (verifyOnLoadMode) {
						case VERIFY_ASYNC: {
							auditVerifyOnLoadTask.verifyEntitiesAsync(objRefsToVerify);
							break;
						}
						case VERIFY_SYNC: {
							auditVerifyOnLoadTask.verifyEntitiesSync(objRefsToVerify);
							break;
						}
						case NONE: {
							// intended blank
							break;
						}
						default:
							throw RuntimeExceptionUtil.createEnumNotSupportedException(verifyOnLoadMode);
					}
				}
			};
			if (!transaction.isActive() || verifyOnStackTL.get() != null) {
				verifyRunnable.invoke();
			}
			else {
				transaction.runOnTransactionPreCommit(verifyRunnable);
			}
			return result;
		}
		finally {
			objRefsToVerifyTL.set(null);
		}
	}

	@Override
	public boolean verifyEntities(List<?> entities) {
		Boolean verifyOnStack = verifyOnStackTL.get();
		if (verifyOnStack != null) {
			return verifyEntitiesIntern(entities);
		}
		verifyOnStackTL.set(Boolean.TRUE);
		try {
			return verifyEntitiesIntern(entities);
		}
		finally {
			verifyOnStackTL.set(null);
		}
	}

	protected boolean verifyEntitiesIntern(List<?> entities) {
		long start = System.currentTimeMillis();
		if (entities.isEmpty()) {
			return true;
		}
		ILinkedMap<IEntityMetaData, IList<IObjRef>> bucketSortObjRefs = bucketSortObjRefs(entities,
				true);
		if (bucketSortObjRefs.isEmpty()) {
			return true;
		}
		IStateRollback rollback = securityActivation
				.pushWithoutSecurity(IStateRollback.EMPTY_ROLLBACKS);
		try {
			LinkedHashMap<String, Object> nameToValueMap = new LinkedHashMap<>();
			IMap<IObjRef, ISet<String>> remainingPropertyMap = fillNameToValueMap(nameToValueMap,
					bucketSortObjRefs);

			int count = 0;
			for (Entry<IEntityMetaData, IList<IObjRef>> entry : bucketSortObjRefs) {
				count += entry.getValue().size();
			}

			if (log.isInfoEnabled()) {
				IThreadLocalObjectCollector objectCollector = this.objectCollector.getCurrent();
				StringBuilder sb = objectCollector.create(StringBuilder.class);
				sb.append("Verifying audit entries covering the following " + count + " entities: ");

				ArrayList<IObjRef> allObjRefs = new ArrayList<>(count);
				for (Entry<IEntityMetaData, IList<IObjRef>> entry : bucketSortObjRefs) {
					allObjRefs.addAll(entry.getValue());
				}
				for (Entry<IEntityMetaData, IList<IObjRef>> entry : bucketSortObjRefs) {
					sb.append("\n\t").append(entry.getValue().size()).append("x ")
							.append(entry.getKey().getEntityType().getName());
				}
				if (log.isDebugEnabled()) {
					sb.append("\n\t");
					debugToLoad(allObjRefs, sb);
					log.debug(sb);
				}
				else if (log.isInfoEnabled()) {
					log.info(sb);
				}
				objectCollector.dispose(sb);
			}
			IList<IObjRef> objRefsToAudit = remainingPropertyMap.keyList();
			boolean[] entitiesDataInvalid = new boolean[objRefsToAudit.size()];

			IdentityHashSet<IAuditedEntity> auditedEntitiesToVerify = new IdentityHashSet<>();
			{
				IQuery<IAuditedEntity> query = resolveQuery(bucketSortObjRefs);
				for (Entry<String, Object> entry : nameToValueMap) {
					query = query.param(entry.getKey(), entry.getValue());
				}
				IList<IAuditedEntity> auditedEntities = query.retrieve();

				filterAuditedEntities(auditedEntities, remainingPropertyMap, objRefsToAudit,
						entitiesDataInvalid, auditedEntitiesToVerify);
			}

			IList<IAuditedEntity> auditedEntitiesToVerifyList = auditedEntitiesToVerify.toList();
			boolean[] auditedEntitiesValid = verifyAuditedEntities(auditedEntitiesToVerifyList);
			ArrayList<IObjRef> invalidEntities = new ArrayList<>();
			ArrayList<IAuditedEntity> invalidAuditedEntities = new ArrayList<>();
			for (int a = auditedEntitiesValid.length; a-- > 0;) {
				if (auditedEntitiesValid[a]) {
					continue;
				}
				invalidAuditedEntities.add(auditedEntitiesToVerifyList.get(a));
			}
			for (int a = entitiesDataInvalid.length; a-- > 0;) {
				if (!entitiesDataInvalid[a]) {
					continue;
				}
				invalidEntities.add(objRefsToAudit.get(a));
			}
			long end = System.currentTimeMillis();
			if (!invalidEntities.isEmpty() || !invalidAuditedEntities.isEmpty()) {
				if (log.isDebugEnabled()) {
					IThreadLocalObjectCollector objectCollector = this.objectCollector.getCurrent();
					StringBuilder sb = objectCollector.create(StringBuilder.class);
					sb.append("Verification failed: ").append(invalidEntities.size()).append(" OF ")
							.append(count).append(" ENTITIES INVALID (").append(end - start).append(" ms):");
					for (IObjRef objRef : invalidEntities) {
						sb.append("\n\t\t").append(objRef.toString());
					}
					sb.append("\n\t").append(invalidAuditedEntities.size()).append(" OF ")
							.append(auditedEntitiesToVerify.size()).append(" AUDIT ENTRIES INVALID:");
					for (IAuditedEntity auditedEntity : invalidAuditedEntities) {
						IAuditedEntityRef ref = auditedEntity.getRef();
						Class<?> refEntityType = classCache.loadClass(ref.getEntityType());
						sb.append("\n\t\t").append(new ObjRef(refEntityType, ObjRef.PRIMARY_KEY_INDEX,
								ref.getEntityId(), ref.getEntityVersion()));
					}
					log.debug(sb);
					objectCollector.dispose(sb);
				}
				return false;
			}
			if (log.isDebugEnabled()) {
				log.debug(
						"Verification successful: ALL " + count + " ENTITIES VALID (" + (end - start) + " ms)");
			}
			return true;
		}
		catch (

		ClassNotFoundException e) {
			throw RuntimeExceptionUtil.mask(e);
		}
		finally {
			rollback.rollback();
		}
	}

	protected java.security.Signature getOrCreateVerifyHandle(ISignature signatureOfUser,
			IMap<ISignature, java.security.Signature> signatureToSignatureHandleMap) {
		try {
			java.security.Signature signatureHandle = signatureToSignatureHandleMap.get(signatureOfUser);
			if (signatureHandle == null) {
				signatureHandle = signatureUtil.createVerifyHandle(signatureOfUser.getSignAndVerify(),
						Base64.decode(signatureOfUser.getPublicKey()));
				signatureToSignatureHandleMap.put(signatureOfUser, signatureHandle);
			}
			return signatureHandle;
		}
		catch (Exception e) {
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Override
	public boolean[] verifyAuditEntries(List<? extends IAuditEntry> auditEntries) {
		if (auditEntries.isEmpty()) {
			return EMPTY_VALIDATION_RESULT;
		}
		IStateRollback rollback = securityActivation
				.pushWithoutSecurity(IStateRollback.EMPTY_ROLLBACKS);
		try {
			auditEntries = getFromCache(auditEntries);
			return verifyAuditEntriesIntern(auditEntries);
		}
		finally {
			rollback.rollback();
		}
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	private <V> List<? extends V> getFromCache(List<? extends V> auditEntries) {
		IList<IObjRef> objRefs = objRefHelper.extractObjRefList(auditEntries, null);
		IList<Object> objects = cache.getObjects(objRefs, CacheDirective.none());
		if (objects.size() != auditEntries.size()) {
			throw new IllegalStateException(
					"At least one " + IAuditEntry.class.getSimpleName() + " could not be resolved");
		}
		return (List) objects;
	}

	private boolean[] verifyAuditEntriesIntern(List<? extends IAuditEntry> auditEntries) {
		@SuppressWarnings("unused")
		IPrefetchState prefetch2 = getPref_SignaturesOfUser().prefetch(auditEntries);

		boolean[] result = new boolean[auditEntries.size()];
		Arrays.fill(result, true);
		ArrayList<IAuditEntry> auditEntriesToVerify = new ArrayList<>(auditEntries.size());
		HashMap<ISignature, java.security.Signature> signatureToSignatureHandleMap = new HashMap<>();

		for (IAuditEntry auditEntry : auditEntries) {
			fillEntriesToVerify(auditEntry, auditEntry.getSignatureOfUser(), auditEntry.getSignedValue(),
					auditEntriesToVerify);
		}
		@SuppressWarnings("unused")
		IPrefetchState prefetch = getPref_verifyAuditEntries().prefetch(auditEntriesToVerify);

		for (int a = 0, size = auditEntriesToVerify.size(); a < size; a++) {
			IAuditEntry auditEntry = auditEntriesToVerify.get(a);
			if (auditEntry == null) {
				continue;
			}
			ISignature signatureOfUser = auditEntry.getSignatureOfUser();
			char[] signature = auditEntry.getSignedValue();
			try {
				final Signature signatureHandle = getOrCreateVerifyHandle(signatureOfUser,
						signatureToSignatureHandleMap);
				byte[] digest = auditEntryToSignature.createVerifyDigest(auditEntry,
						new BiFunction<IAuditedEntity, byte[], Boolean>() {
							@Override
							public Boolean apply(IAuditedEntity auditedEntity, byte[] digest) {
								try {
									signatureHandle.update(digest);
									return Boolean
											.valueOf(
													signatureHandle.verify(Base64.decode(auditedEntity.getSignedValue())));
								}
								catch (SignatureException | IOException e) {
									throw RuntimeExceptionUtil.mask(e);
								}
							}
						});
				if (digest == null) {
					result[a] = false;
					continue;
				}
				signatureHandle.update(digest);
				result[a] = signatureHandle.verify(Base64.decode(signature));
			}
			catch (Exception e) {
				throw RuntimeExceptionUtil.mask(e);
			}
		}
		return result;
	}

	@Override
	public boolean[] verifyAuditedEntities(List<? extends IAuditedEntity> auditedEntities) {
		if (auditedEntities.isEmpty()) {
			return EMPTY_VALIDATION_RESULT;
		}
		IStateRollback rollback = securityActivation
				.pushWithoutSecurity(IStateRollback.EMPTY_ROLLBACKS);
		try {
			auditedEntities = getFromCache(auditedEntities);
			return verifyAuditedEntitiesIntern(auditedEntities);
		}
		finally {
			rollback.rollback();
		}
	}

	private boolean[] verifyAuditedEntitiesIntern(List<? extends IAuditedEntity> auditedEntities) {
		@SuppressWarnings("unused")
		IPrefetchState prefetch2 = getPref_SignaturesOfUserFromAuditedEntity()
				.prefetch(auditedEntities);

		boolean[] result = new boolean[auditedEntities.size()];
		Arrays.fill(result, true);
		ArrayList<IAuditedEntity> auditedEntitiesToVerify = new ArrayList<>(auditedEntities.size());
		HashMap<ISignature, java.security.Signature> signatureToSignatureHandleMap = new HashMap<>();

		for (IAuditedEntity auditedEntity : auditedEntities) {
			fillEntriesToVerify(auditedEntity, auditedEntity.getEntry().getSignatureOfUser(),
					auditedEntity.getSignedValue(), auditedEntitiesToVerify);
		}
		@SuppressWarnings("unused")
		IPrefetchState prefetch = getPref_verifyAuditEntriesFromAuditedEntity()
				.prefetch(auditedEntitiesToVerify);

		for (int a = 0, size = auditedEntitiesToVerify.size(); a < size; a++) {
			IAuditedEntity auditedEntity = auditedEntitiesToVerify.get(a);
			if (auditedEntity == null) {
				continue;
			}
			ISignature signatureOfUser = auditedEntity.getEntry().getSignatureOfUser();
			char[] signature = auditedEntity.getSignedValue();
			try {
				Signature signatureHandle = getOrCreateVerifyHandle(signatureOfUser,
						signatureToSignatureHandleMap);
				byte[] digest = auditEntryToSignature.createVerifyDigest(auditedEntity);
				if (digest == null) {
					result[a] = false;
					continue;
				}
				signatureHandle.update(digest);
				result[a] = signatureHandle.verify(Base64.decode(signature));
			}
			catch (Exception e) {
				throw RuntimeExceptionUtil.mask(e);
			}
		}
		return result;
	}

	protected <V> void fillEntriesToVerify(V value, ISignature signatureOfUser, char[] signature,
			List<? super V> entriesToVerify) {
		if (signatureOfUser == null) {
			if (signatureActive) {
				throw new IllegalArgumentException(
						((IEntityMetaDataHolder) value).get__EntityMetaData().getEntityType().getSimpleName()
								+ " has no relation to a user signature: " + value);
			}
			// audit entries without a signature instance can not be verified but are intentionally
			// treated as "valid"
			entriesToVerify.add(null);
			return;
		}
		if (signature == null) {
			if (signatureActive) {
				throw new IllegalArgumentException(
						((IEntityMetaDataHolder) value).get__EntityMetaData().getEntityType().getSimpleName()
								+ " has no valid signature: " + value);
			}
			// audit entries without a signed value can not be verified but are intentionally treated as
			// "valid"
			entriesToVerify.add(null);
			return;
		}
		entriesToVerify.add(value);
	}

	protected ILinkedMap<IEntityMetaData, IList<IObjRef>> bucketSortObjRefs(List<?> entities,
			boolean checkConfiguration) {
		final IdentityLinkedMap<IEntityMetaData, ISet<IObjRef>> metaDataToObjRefsDict =
				new IdentityLinkedMap<>();
		boolean hasAtLeastOneNonPrimaryObjRef = false;
		for (int i = entities.size(); i-- > 0;) {
			Object entity = entities.get(i);
			IObjRef objRef = null;
			IEntityMetaData metaData;
			if (entity instanceof IObjRef) {
				objRef = (IObjRef) entity;
				metaData = entityMetaDataProvider.getMetaData(objRef.getRealType());
			}
			else if (entity instanceof IEntityMetaDataHolder) {
				metaData = ((IEntityMetaDataHolder) entity).get__EntityMetaData();
			}
			else {
				throw new IllegalArgumentException("All objects must be either a valid entity or an "
						+ IObjRef.class.getSimpleName() + " instance");
			}

			if (checkConfiguration) {
				IAuditConfiguration auditConfiguration = auditConfigurationProvider
						.getAuditConfiguration(metaData.getEntityType());
				if (!auditConfiguration.isAuditActive()) {
					continue;
				}
			}
			if (objRef == null) {
				// this is the case above because we create the objRef only if the auditConfiguration tells
				// us to really audit the given entity type
				objRef = objRefHelper.entityToObjRef(entity);
			}
			if (objRef.getIdNameIndex() != ObjRef.PRIMARY_KEY_INDEX) {
				hasAtLeastOneNonPrimaryObjRef = true;
			}
			ISet<IObjRef> objRefs = metaDataToObjRefsDict.get(metaData);
			if (objRefs == null) {
				objRefs = new HashSet<>();
				metaDataToObjRefsDict.put(metaData, objRefs);
			}
			objRefs.add(objRef);
		}
		if (!hasAtLeastOneNonPrimaryObjRef) {
			return convertSetToList(metaDataToObjRefsDict);
		}
		transaction.runInTransaction(new IBackgroundWorkerDelegate() {
			@Override
			public void invoke() throws Exception {
				IDatabase database = AuditEntryVerifier.this.database.getCurrent();

				for (Entry<IEntityMetaData, ISet<IObjRef>> entry : metaDataToObjRefsDict) {
					ISet<IObjRef> objRefs = entry.getValue();
					HashMap<Byte, IList<IObjRef>> indexToObjRefsMap = null;
					Iterator<IObjRef> iter = objRefs.iterator();
					while (iter.hasNext()) {
						IObjRef objRef = iter.next();
						if (objRef.getIdNameIndex() == ObjRef.PRIMARY_KEY_INDEX) {
							continue;
						}
						if (indexToObjRefsMap == null) {
							indexToObjRefsMap = new HashMap<>();
						}
						IList<IObjRef> assignedObjRefs = indexToObjRefsMap.get(objRef.getIdNameIndex());
						if (assignedObjRefs == null) {
							assignedObjRefs = new ArrayList<>();
							indexToObjRefsMap.put(objRef.getIdNameIndex(), assignedObjRefs);
						}
						iter.remove();
						assignedObjRefs.add(objRef);
					}
					if (indexToObjRefsMap == null) {
						// nothing to do
						continue;
					}
					IEntityMetaData metaData = entry.getKey();
					Class<?> idRealType = metaData.getIdMember().getRealType();
					PrimitiveMember[] alternateIdMembers = metaData.getAlternateIdMembers();
					ITable table = database.getTableByType(metaData.getEntityType());
					for (Entry<Byte, IList<IObjRef>> indexEntry : indexToObjRefsMap) {
						int idIndex = indexEntry.getKey().intValue();
						PrimitiveMember alternateIdMember = alternateIdMembers[idIndex];
						Class<?> alternateIdRealType = alternateIdMember.getRealType();
						IList<IObjRef> alternateIdObjRefs = indexEntry.getValue();
						HashMap<Object, Object> alternateIdToVersionMap = HashMap
								.create(alternateIdObjRefs.size());
						for (int a = alternateIdObjRefs.size(); a-- > 0;) {
							IObjRef alternateIdObjRef = alternateIdObjRefs.get(a);
							alternateIdToVersionMap.put(alternateIdObjRef.getId(),
									alternateIdObjRef.getVersion());
						}
						IPreparedObjRefFactory preparedObjRefFactory = objRefFactory
								.prepareObjRefFactory(metaData.getEntityType(), ObjRef.PRIMARY_KEY_INDEX);
						IVersionCursor cursor = table.selectVersion(alternateIdMember.getName(),
								alternateIdObjRefs);
						try {
							while (cursor.moveNext()) {
								IVersionItem item = cursor.getCurrent();
								Object id = conversionHelper.convertValueToType(idRealType, item.getId());
								Object alternateId = conversionHelper.convertValueToType(alternateIdRealType,
										item.getId(idIndex));
								Object version = alternateIdToVersionMap.get(alternateId);

								IObjRef objRef = preparedObjRefFactory.createObjRef(id, version);
								objRefs.add(objRef);
							}
						}
						finally {
							cursor.dispose();
						}
					}
				}
			}
		});
		return convertSetToList(metaDataToObjRefsDict);
	}

	private ILinkedMap<IEntityMetaData, IList<IObjRef>> convertSetToList(
			IdentityLinkedMap<IEntityMetaData, ISet<IObjRef>> metaDataToObjRefsMap) {
		final IdentityLinkedMap<IEntityMetaData, IList<IObjRef>> targetMap = new IdentityLinkedMap<>();
		for (Entry<IEntityMetaData, ISet<IObjRef>> entry : metaDataToObjRefsMap) {
			targetMap.put(entry.getKey(), entry.getValue().toList());
		}
		return targetMap;
	}

	protected void debugToLoad(List<IObjRef> orisToLoad, StringBuilder sb) {
		int count = orisToLoad.size();
		sb.append("List<IObjRef> : ").append(count).append(" item");
		if (count != 1) {
			sb.append('s');
		}
		sb.append(" [");

		int printBorder = 3,
				skipped = count >= maxDebugItems ? Math.max(0, count - printBorder * 2) : 0;
		for (int a = count; a-- > 0;) {
			if (skipped > 1) {
				if (count - a > printBorder && a >= printBorder) {
					continue;
				}
				if (a == printBorder - 1) {
					sb.append("\r\n\t...skipped ").append(skipped).append(" items...");
				}
			}
			IObjRef oriToLoad = orisToLoad.get(a);
			if (count > 1) {
				sb.append("\r\n\t");
			}
			StringBuilderUtil.appendPrintable(sb, oriToLoad);
		}
		sb.append("]");
	}
}
