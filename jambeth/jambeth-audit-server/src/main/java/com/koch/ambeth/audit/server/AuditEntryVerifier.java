package com.koch.ambeth.audit.server;

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
import com.koch.ambeth.merge.IObjRefHelper;
import com.koch.ambeth.merge.cache.CacheDirective;
import com.koch.ambeth.merge.cache.ICache;
import com.koch.ambeth.merge.cache.ICacheFactory;
import com.koch.ambeth.merge.compositeid.ICompositeIdFactory;
import com.koch.ambeth.merge.metadata.IObjRefFactory;
import com.koch.ambeth.merge.proxy.IEntityMetaDataHolder;
import com.koch.ambeth.merge.proxy.IObjRefContainer;
import com.koch.ambeth.merge.security.ISecurityActivation;
import com.koch.ambeth.merge.transfer.ObjRef;
import com.koch.ambeth.merge.util.DirectValueHolderRef;
import com.koch.ambeth.merge.util.IPrefetchHandle;
import com.koch.ambeth.merge.util.IPrefetchHelper;
import com.koch.ambeth.merge.util.IPrefetchState;
import com.koch.ambeth.persistence.api.IDatabase;
import com.koch.ambeth.query.IOperand;
import com.koch.ambeth.query.IOperator;
import com.koch.ambeth.query.IQuery;
import com.koch.ambeth.query.IQueryBuilder;
import com.koch.ambeth.query.IQueryBuilderFactory;
import com.koch.ambeth.query.OrderByType;
import com.koch.ambeth.security.model.ISignature;
import com.koch.ambeth.security.server.ISignatureUtil;
import com.koch.ambeth.security.server.config.SecurityServerConfigurationConstants;
import com.koch.ambeth.service.cache.ClearAllCachesEvent;
import com.koch.ambeth.service.cache.model.ILoadContainer;
import com.koch.ambeth.service.merge.IEntityMetaDataProvider;
import com.koch.ambeth.service.merge.model.IEntityMetaData;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.service.metadata.Member;
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
import com.koch.ambeth.util.function.CheckedRunnable;
import com.koch.ambeth.util.function.CheckedSupplier;
import com.koch.ambeth.util.objectcollector.IThreadLocalObjectCollector;
import com.koch.ambeth.util.transaction.ILightweightTransaction;
import lombok.SneakyThrows;

import java.io.IOException;
import java.security.SignatureException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.locks.Lock;

public class AuditEntryVerifier implements IAuditEntryVerifier, IVerifyOnLoad, IThreadLocalCleanupBean {
    public static final boolean[] EMPTY_VALIDATION_RESULT = new boolean[0];
    public static final String HANDLE_CLEAR_ALL_CACHES_EVENT = "handleClearAllCachesEvent";
    protected final SmartCopyMap<Integer, IQuery<IAuditedEntity>> entityTypeCountToQuery = new SmartCopyMap<>();
    @Forkable(processor = ForkableProcessor.class)
    protected final ThreadLocal<ArrayList<IObjRef>> objRefsToVerifyTL = new ThreadLocal<>();
    protected final ThreadLocal<Boolean> verifyOnStackTL = new ThreadLocal<>();
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
    protected ICacheFactory cacheFactory;
    @Autowired
    protected IClassCache classCache;
    @Autowired
    protected ICompositeIdFactory compositeIdFactory;
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
    // this feature is still alpha status: optimize audit entry verification scope (do only set it to
    // TRUE if you know what you do)
    protected boolean filterAuditedEntities = true;

    protected IPrefetchHandle pref_filterAuditedEntities, pref_verifyAuditEntries, prefetchSignaturesOfUser, prefetchSignaturesOfUserFromAuditedEntity, pref_verifyAuditEntriesFromAuditedEntity;

    protected int maxDebugItems = 50;
    @LogInstance
    private ILogger log;

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
                                                   .add(IAuditedEntity.class, IAuditedEntity.Entry, IAuditedEntity.Ref, IAuditedEntity.Primitives, IAuditedEntity.Relations)//
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
                                                .add(IAuditedEntity.class, IAuditedEntity.Ref, IAuditedEntity.Primitives, IAuditedEntity.Relations)//
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
                                                                 .add(IAuditedEntity.class, IAuditedEntity.Ref, IAuditedEntity.Primitives, IAuditedEntity.Relations)//
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
        } finally {
            writeLock.unlock();
        }
    }

    protected IQuery<IAuditedEntity> resolveQuery(ILinkedMap<IEntityMetaData, IList<IObjRef>> bucketSortObjRefs) {
        IQuery<IAuditedEntity> query = entityTypeCountToQuery.get(Integer.valueOf(bucketSortObjRefs.size()));
        if (query != null) {
            return query;
        }
        IQueryBuilder<IAuditedEntity> qb = queryBuilderFactory.create(IAuditedEntity.class);

        IOperand entityTypeProp = qb.property(IAuditedEntity.Ref + "." + IAuditedEntityRef.EntityType);
        IOperand entityIdProp = qb.property(IAuditedEntity.Ref + "." + IAuditedEntityRef.EntityId);
        int index = 0;
        IOperator op = null;
        for (int a = bucketSortObjRefs.size(); a-- > 0; ) {
            IOperator typeMatchOp = qb.let(entityTypeProp).isEqualTo(qb.valueName("param" + index++));
            IOperator idMatchOp = qb.let(entityIdProp).isIn(qb.valueName("param" + index++));
            IOperator matchOp = qb.and(typeMatchOp, idMatchOp);

            if (op == null) {
                op = matchOp;
            } else {
                op = qb.or(op, matchOp);
            }
        }
        query = qb.orderBy(qb.property(IAuditedEntity.Entry + "." + IAuditEntry.Timestamp), OrderByType.ASC).build(op);

        entityTypeCountToQuery.put(Integer.valueOf(bucketSortObjRefs.size()), query);
        return query;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected boolean isTrailedVersionTooNew(IEntityMetaData metaData, IObjRef tempObjRef, IAuditedEntityRef ref, IMap<IObjRef, IObjRefContainer> objRefToEntityMap) {
        IObjRefContainer entity = objRefToEntityMap.get(tempObjRef);

        Comparable versionOfEntity = (Comparable) metaData.getVersionMember().getValue(entity);
        Comparable versionOfAuditedEntityRef = conversionHelper.convertValueToType(versionOfEntity.getClass(), ref.getEntityVersion());

        // versionOfEntity is smaller than the audited version. This means we do not want to check the
        // WHOLE audit trail for this entity
        // but only to a specific time-point
        return (versionOfEntity.compareTo(versionOfAuditedEntityRef) < 0);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected boolean isTrailedVersionTooOld(IEntityMetaData metaData, IObjRef tempObjRef, Object entity, Tuple2KeyHashMap<Class<?>, Object, Object> objRefToMaxVersionOfAuditTrailMap) {
        Comparable versionOfEntity = (Comparable) metaData.getVersionMember().getValue(entity);
        Comparable maxVersionOfAuditTrail = (Comparable) objRefToMaxVersionOfAuditTrailMap.get(tempObjRef.getRealType(), tempObjRef.getId());

        // maxVersionOfAuditTrail is smaller than the entity from cache. our audit trail is incomplete.
        // we need to re-search for AuditedEntity and reverify this
        // entity in another iteration
        return (maxVersionOfAuditTrail.compareTo(versionOfEntity) < 0);
    }

    protected void filterAuditedEntities(IList<IAuditedEntity> auditedEntities, IMap<IObjRef, ISet<String>> objRefToPrimitiveMap, IList<IObjRef> objRefs, boolean[] entitiesDataInvalid,
            ISet<IAuditedEntity> auditedEntitiesToVerify) {
        if (!filterAuditedEntities || auditedEntities.isEmpty()) {
            auditedEntitiesToVerify.addAll(auditedEntities);
            return;
        }
        @SuppressWarnings("unused") var prefetch = getPref_filterAuditedEntities().prefetch(auditedEntities);

        var conversionHelper = this.conversionHelper;
        var entities = cache.getObjects(objRefs, CacheDirective.returnMisses());
        var objRefToEntityMap = HashMap.<IObjRef, IObjRefContainer>create(objRefs.size());
        var objRefToValidPropertyMap = HashMap.<IObjRef, HashMap<String, Boolean>>create(objRefs.size());
        var valueHoldersToPrefetch = new ArrayList<DirectValueHolderRef>(objRefs.size());
        for (int a = objRefs.size(); a-- > 0; ) {
            var objRef = objRefs.get(a);
            var entity = (IObjRefContainer) entities.get(a);
            objRefToEntityMap.put(objRef, entity);
            var metaData = entity.get__EntityMetaData();
            var invalidPropertyMap = HashMap.<String, Boolean>create(objRefToPrimitiveMap.get(objRef).size() + metaData.getRelationMembers().length);
            objRefToValidPropertyMap.put(objRef, invalidPropertyMap);

            for (var relationMember : metaData.getRelationMembers()) {
                valueHoldersToPrefetch.add(new DirectValueHolderRef(entity, relationMember));
            }
        }
        prefetchHelper.prefetch(valueHoldersToPrefetch);

        var objRefToRelationMap = Tuple2KeyHashMap.<Class<?>, String, IMap<String, Tuple2KeyHashMap<Class<?>, String, Boolean>>>create(objRefs.size());

        var tempObjRef = new ObjRef();
        tempObjRef.setIdNameIndex(ObjRef.PRIMARY_KEY_INDEX);

        var realAuditedEntities = new ArrayList<IAuditedEntity>(auditedEntities.size());

        try {
            for (int a = 0, size = auditedEntities.size(); a < size; a++) {
                var auditedEntity = auditedEntities.get(a);
                var ref = auditedEntity.getRef();
                var refEntityType = classCache.loadClass(ref.getEntityType());

                var metaData = entityMetaDataProvider.getMetaData(refEntityType);
                var entityType = metaData.getEntityType();
                tempObjRef.setRealType(entityType);
                tempObjRef.setId(conversionHelper.convertValueToType(metaData.getIdMember().getRealType(), ref.getEntityId()));

                if (isTrailedVersionTooNew(metaData, tempObjRef, ref, objRefToEntityMap)) {
                    continue;
                }
                realAuditedEntities.add(auditedEntity);
                var relationMembers = metaData.getRelationMembers();

                if (relationMembers.length > 0) {
                    var relationsMap = objRefToRelationMap.get(refEntityType, ref.getEntityId());
                    if (relationsMap == null) {
                        relationsMap = HashMap.<String, Tuple2KeyHashMap<Class<?>, String, Boolean>>create(relationMembers.length);
                        objRefToRelationMap.put(refEntityType, ref.getEntityId(), relationsMap);
                    }
                    var relations = auditedEntity.getRelations();
                    if (!relations.isEmpty()) {
                        var auditedEntityRelations = auditedEntity.getRelations();
                        for (int b = auditedEntityRelations.size(); b-- > 0; ) {
                            var relation = auditedEntityRelations.get(b);
                            var relationMap = relationsMap.get(relation.getName());
                            if (relationMap == null) {
                                relationMap = Tuple2KeyHashMap.<Class<?>, String, Boolean>create(relationMembers.length);
                                relationsMap.put(relation.getName(), relationMap);
                            }
                            List<? extends IAuditedEntityRelationPropertyItem> items = relation.getItems();
                            for (int c = items.size(); c-- > 0; ) {
                                var item = items.get(c);
                                var itemRef = item.getRef();
                                var itemEntityType = classCache.loadClass(itemRef.getEntityType());
                                switch (item.getChangeType()) {
                                    case ADD:
                                        relationMap.putIfNotExists(itemEntityType, itemRef.getEntityId(), Boolean.TRUE);
                                        break;
                                    case REMOVE:
                                        relationMap.removeIfValue(itemEntityType, itemRef.getEntityId(), Boolean.TRUE);
                                        break;
                                    default:
                                        throw RuntimeExceptionUtil.createEnumNotSupportedException(item.getChangeType());
                                }
                            }
                        }
                        auditedEntitiesToVerify.add(auditedEntity);
                    }
                }
            }
            var objRefToPreviousRefVersionMap = Tuple2KeyHashMap.<Class<?>, Object, IMap<String, IAuditedEntity>>create(objRefs.size());
            var objRefToMaxVersionOfAuditTrailMap = new Tuple2KeyHashMap<Class<?>, Object, Object>();

            // audit entries are ordered by timestamp DESC. So the newest auditEntries are last
            // so we do a reverse iteration because we are interested in the LAST/RECENT assigned value to
            // primitives
            for (int a = realAuditedEntities.size(); a-- > 0; ) {
                var auditedEntity = realAuditedEntities.get(a);
                var ref = auditedEntity.getRef();
                var refEntityType = classCache.loadClass(ref.getEntityType());
                var metaData = entityMetaDataProvider.getMetaData(refEntityType);

                tempObjRef.setRealType(metaData.getEntityType());
                tempObjRef.setId(conversionHelper.convertValueToType(metaData.getIdMember().getRealType(), ref.getEntityId()));

                var previousRefVersionToEntityMap = objRefToPreviousRefVersionMap.get(tempObjRef.getRealType(), tempObjRef.getId());
                if (previousRefVersionToEntityMap == null) {
                    previousRefVersionToEntityMap = new HashMap<>();
                    objRefToPreviousRefVersionMap.put(tempObjRef.getRealType(), tempObjRef.getId(), previousRefVersionToEntityMap);
                }
                var refPreviousVersion = auditedEntity.getRefPreviousVersion();
                if (refPreviousVersion == null) {
                    if (auditedEntity.getChangeType() != AuditedEntityChangeType.INSERT) {
                        throw new IllegalStateException("Illegal null string for '" + IAuditedEntity.RefPreviousVersion + "' of a non-INSERT " + auditedEntity);
                    }
                    refPreviousVersion = "";
                } else if (refPreviousVersion.isEmpty()) {
                    throw new IllegalStateException("Illegal empty string for '" + IAuditedEntity.RefPreviousVersion + "' of " + auditedEntity);
                } else if (auditedEntity.getChangeType() == AuditedEntityChangeType.INSERT) {
                    throw new IllegalStateException("Illegal non-null string for '" + IAuditedEntity.RefPreviousVersion + "' of a INSERT " + auditedEntity);
                }
                if (!previousRefVersionToEntityMap.putIfNotExists(refPreviousVersion, auditedEntity)) {
                    throw new IllegalStateException("Must never happen");
                }

                // this method stores the "first hit" of an AuditedEntity for any given entity which
                // corresponds to the max (newest) version of the entity
                mapMaxEntityVersionOfAuditTrail(metaData, tempObjRef, ref, objRefToMaxVersionOfAuditTrailMap);

                ISet<String> remainingPropertyMapOfEntity = objRefToPrimitiveMap.get(tempObjRef);
                if (remainingPropertyMapOfEntity == null) {
                    // this auditedEntity is not relevant for any of the remaining primitive mappings
                    continue;
                }
                IObjRefContainer entity = objRefToEntityMap.get(tempObjRef);
                List<? extends IAuditedEntityPrimitiveProperty> primitives = auditedEntity.getPrimitives();
                for (int c = primitives.size(); c-- > 0; ) {
                    IAuditedEntityPrimitiveProperty primitive = primitives.get(c);
                    String memberName = primitive.getName();
                    if (!remainingPropertyMapOfEntity.remove(memberName)) {
                        continue;
                    }
                    if (entity != null) {
                        Member member = metaData.getMemberByName(memberName);
                        String entityValue = auditInfoController.createAuditedValueOfEntityPrimitive(member.getValue(entity, true));
                        HashMap<String, Boolean> validPropertyMap = objRefToValidPropertyMap.get(tempObjRef);

                        String newValue = primitive.getNewValue();
                        boolean valid = Objects.equals(newValue, entityValue);
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
            for (int a = objRefs.size(); a-- > 0; ) {
                IObjRef objRef = objRefs.get(a);
                IObjRefContainer entity = objRefToEntityMap.get(objRef);

                IEntityMetaData metaData = entity.get__EntityMetaData();
                if (isTrailedVersionTooOld(metaData, objRef, entity, objRefToMaxVersionOfAuditTrailMap)) {
                    entitiesDataInvalid[a] = true;
                    continue;
                }
                tempObjRef.setRealType(metaData.getEntityType());
                tempObjRef.setId(conversionHelper.convertValueToType(metaData.getIdMember().getRealType(), objRef.getId()));

                IMap<String, Tuple2KeyHashMap<Class<?>, String, Boolean>> relationsMap =
                        objRefToRelationMap.get(metaData.getEntityType(), conversionHelper.convertValueToType(String.class, objRef.getId()));

                HashMap<String, Boolean> validPropertyMap = objRefToValidPropertyMap.get(objRef);

                boolean atleastOnePropertyInvalid = false;

                var relationMembers = metaData.getRelationMembers();
                for (int relationIndex = relationMembers.length; relationIndex-- > 0; ) {
                    var relationMember = relationMembers[relationIndex];
                    var relationsOfMember = objRefHelper.extractObjRefList(relationMember.getValue(entity), null);
                    var relationMap = relationsMap != null ? relationsMap.get(relationMember.getName()) : null;
                    boolean valid;
                    if (relationMap == null) {
                        // no audit entry did note a change to this member. So it has to be still empty for the
                        // whole lifetime of the entity
                        valid = relationsOfMember.isEmpty();
                    } else if (relationMap.size() != relationsOfMember.size()) {
                        valid = false;
                    } else {
                        for (int b = relationsOfMember.size(); b-- > 0; ) {
                            IObjRef relationOfMember = relationsOfMember.get(b);
                            if (relationOfMember.getIdNameIndex() != ObjRef.PRIMARY_KEY_INDEX) {
                                throw new IllegalStateException(
                                        "ObjRef of member '" + relationMember.getName() + "' of entity instance '" + entity + "' must hold a primary identifier: " + relationMember);
                            }
                            relationMap.remove(relationOfMember.getRealType(), conversionHelper.convertValueToType(String.class, relationOfMember.getId()));
                        }
                        valid = relationMap.isEmpty();
                    }
                    validPropertyMap.put(relationMember.getName(), Boolean.valueOf(valid));
                    atleastOnePropertyInvalid |= !valid;
                }
                for (var member : metaData.getPrimitiveMembers()) {
                    var valid = validPropertyMap.get(member.getName());
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
                    var previousRefVersionToEntityMap = objRefToPreviousRefVersionMap.get(tempObjRef.getRealType(), tempObjRef.getId());
                    if (previousRefVersionToEntityMap == null) {
                        atleastOnePropertyInvalid = true;
                    } else {
                        for (var entry : previousRefVersionToEntityMap) {
                            var ref = entry.getValue().getRef();
                            var refVersion = ref.getEntityVersion();
                            var followingAuditEntry = previousRefVersionToEntityMap.get(refVersion);
                            if (followingAuditEntry == null) {
                                // this is only allowed for the LAST audited entity in the chain so we check whether
                                // this is the case here
                                var maxVersion = objRefToMaxVersionOfAuditTrailMap.get(tempObjRef.getRealType(), tempObjRef.getId());
                                var convertedRefVersion = conversionHelper.convertValueToType(metaData.getVersionMember().getRealType(), refVersion);
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
        } catch (ClassNotFoundException e) {
            throw RuntimeExceptionUtil.mask(e);
        }
    }

    @SuppressWarnings("rawtypes")
    private void mapMaxEntityVersionOfAuditTrail(IEntityMetaData metaData, ObjRef tempObjRef, IAuditedEntityRef ref, Tuple2KeyHashMap<Class<?>, Object, Object> objRefToMaxVersionOfAuditTrailMap) {
        var existingMaxVersion = (Comparable) objRefToMaxVersionOfAuditTrailMap.get(tempObjRef.getRealType(), tempObjRef.getId());
        if (existingMaxVersion == null) {
            var versionType = metaData.getVersionMember().getRealType();
            var currentVersion = (Comparable) conversionHelper.convertValueToType(versionType, ref.getEntityVersion());
            objRefToMaxVersionOfAuditTrailMap.put(tempObjRef.getRealType(), tempObjRef.getId(), currentVersion);
        }
    }

    protected IMap<IObjRef, ISet<String>> fillNameToValueMap(IMap<String, Object> nameToValueMap, ILinkedMap<IEntityMetaData, IList<IObjRef>> bucketSortObjRefs) {
        var remainingPropertyMap = filterAuditedEntities ? new HashMap<IObjRef, ISet<String>>() : null;

        for (var entry : bucketSortObjRefs) {
            var metaData = entry.getKey();
            var objRefsOfEntityType = entry.getValue();

            nameToValueMap.put("param" + nameToValueMap.size(), metaData.getEntityType());

            var ids = new Object[objRefsOfEntityType.size()];
            for (int a = objRefsOfEntityType.size(); a-- > 0; ) {
                ids[a] = objRefsOfEntityType.get(a).getId();
            }
            nameToValueMap.put("param" + nameToValueMap.size(), ids);

            if (!filterAuditedEntities) {
                continue;
            }
            var primitiveMembers = metaData.getPrimitiveMembers();
            var primitiveMembersSet = primitiveMembers.length > 0 ? HashSet.<String>create(primitiveMembers.length) : EmptySet.<String>emptySet();
            for (var primitiveMember : primitiveMembers) {
                if (metaData.getUpdatedByMember() == primitiveMember || metaData.getUpdatedOnMember() == primitiveMember || metaData.getCreatedByMember() == primitiveMember ||
                        metaData.getCreatedOnMember() == primitiveMember) {
                    continue;
                }
                primitiveMembersSet.add(primitiveMember.getName());
            }
            for (int a = objRefsOfEntityType.size(); a-- > 0; ) {
                var objRef = objRefsOfEntityType.get(a);
                remainingPropertyMap.put(objRef, primitiveMembersSet.isEmpty() ? primitiveMembersSet : new HashSet<>(primitiveMembersSet));
            }
        }
        return remainingPropertyMap;
    }

    @Override
    public void queueVerifyEntitiesOnLoad(IList<ILoadContainer> loadedEntities) {
        var objRefsToVerify = objRefsToVerifyTL.get();
        if (objRefsToVerify == null) {
            return;
        }
        for (int a = 0, size = loadedEntities.size(); a < size; a++) {
            var objRef = loadedEntities.get(a).getReference();
            var metaData = entityMetaDataProvider.getMetaData(objRef.getRealType());

            var auditConfiguration = auditConfigurationProvider.getAuditConfiguration(metaData.getEntityType());
            if (!auditConfiguration.isAuditActive()) {
                continue;
            }
            objRefsToVerify.add(objRefFactory.dup(objRef));
        }
    }

    @SneakyThrows
    @Override
    public <R> R verifyEntitiesOnLoad(final CheckedSupplier<R> runnable) {
        if (VerifyOnLoadMode.NONE.equals(verifyOnLoadMode)) {
            return CheckedSupplier.invoke(runnable);
        }
        {
            var objRefsToVerify = objRefsToVerifyTL.get();
            if (objRefsToVerify != null) {
                return CheckedSupplier.invoke(runnable);
            }
        }

        var objRefsToVerify = new ArrayList<IObjRef>();
        objRefsToVerifyTL.set(objRefsToVerify);
        try {
            var result = CheckedSupplier.invoke(runnable);
            if (objRefsToVerify.isEmpty()) {
                return result;
            }
            CheckedRunnable verifyRunnable = () -> {
                var verifyOnStack = verifyOnStackTL.get();
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
                }
            };
            if (!transaction.isActive() || verifyOnStackTL.get() != null) {
                CheckedRunnable.invoke(verifyRunnable);
            } else {
                transaction.runOnTransactionPreCommit(verifyRunnable);
            }
            return result;
        } finally {
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
        } finally {
            verifyOnStackTL.set(null);
        }
    }

    @SneakyThrows
    protected boolean verifyEntitiesIntern(List<?> entities) {
        var start = System.currentTimeMillis();
        if (entities.isEmpty()) {
            return true;
        }
        var bucketSortObjRefs = bucketSortObjRefs(entities, true);
        if (bucketSortObjRefs.isEmpty()) {
            return true;
        }
        var rollback = securityActivation.pushWithoutSecurity();
        try {
            var nameToValueMap = new LinkedHashMap<String, Object>();
            var remainingPropertyMap = fillNameToValueMap(nameToValueMap, bucketSortObjRefs);

            var count = 0;
            for (var entry : bucketSortObjRefs) {
                count += entry.getValue().size();
            }

            if (log.isInfoEnabled()) {
                var objectCollector = this.objectCollector.getCurrent();
                var sb = objectCollector.create(StringBuilder.class);
                sb.append("Verifying audit entries covering the following " + count + " entities: ");

                var allObjRefs = new ArrayList<IObjRef>(count);
                for (var entry : bucketSortObjRefs) {
                    allObjRefs.addAll(entry.getValue());
                }
                for (var entry : bucketSortObjRefs) {
                    sb.append("\n\t").append(entry.getValue().size()).append("x ").append(entry.getKey().getEntityType().getName());
                }
                if (log.isDebugEnabled()) {
                    sb.append("\n\t");
                    debugToLoad(allObjRefs, sb);
                    log.debug(sb);
                } else if (log.isInfoEnabled()) {
                    log.info(sb);
                }
                objectCollector.dispose(sb);
            }
            var objRefsToAudit = remainingPropertyMap.keyList();
            var entitiesDataInvalid = new boolean[objRefsToAudit.size()];

            var auditedEntitiesToVerify = new IdentityHashSet<IAuditedEntity>();
            {
                var query = resolveQuery(bucketSortObjRefs);
                for (var entry : nameToValueMap) {
                    query = query.param(entry.getKey(), entry.getValue());
                }
                var auditedEntities = query.retrieve();

                filterAuditedEntities(auditedEntities, remainingPropertyMap, objRefsToAudit, entitiesDataInvalid, auditedEntitiesToVerify);
            }

            var auditedEntitiesToVerifyList = auditedEntitiesToVerify.toList();
            var auditedEntitiesValid = verifyAuditedEntities(auditedEntitiesToVerifyList);
            var invalidEntities = new ArrayList<IObjRef>();
            var invalidAuditedEntities = new ArrayList<IAuditedEntity>();
            for (int a = auditedEntitiesValid.length; a-- > 0; ) {
                if (auditedEntitiesValid[a]) {
                    continue;
                }
                invalidAuditedEntities.add(auditedEntitiesToVerifyList.get(a));
            }
            for (int a = entitiesDataInvalid.length; a-- > 0; ) {
                if (!entitiesDataInvalid[a]) {
                    continue;
                }
                invalidEntities.add(objRefsToAudit.get(a));
            }
            var end = System.currentTimeMillis();
            if (!invalidEntities.isEmpty() || !invalidAuditedEntities.isEmpty()) {
                if (log.isDebugEnabled()) {
                    var objectCollector = this.objectCollector.getCurrent();
                    var sb = objectCollector.create(StringBuilder.class);
                    sb.append("Verification failed: ").append(invalidEntities.size()).append(" OF ").append(count).append(" ENTITIES INVALID (").append(end - start).append(" ms):");
                    for (var objRef : invalidEntities) {
                        sb.append("\n\t\t").append(objRef.toString());
                    }
                    sb.append("\n\t").append(invalidAuditedEntities.size()).append(" OF ").append(auditedEntitiesToVerify.size()).append(" AUDIT ENTRIES INVALID:");
                    for (var auditedEntity : invalidAuditedEntities) {
                        var ref = auditedEntity.getRef();
                        var refEntityType = classCache.loadClass(ref.getEntityType());
                        sb.append("\n\t\t").append(new ObjRef(refEntityType, ObjRef.PRIMARY_KEY_INDEX, ref.getEntityId(), ref.getEntityVersion()));
                    }
                    log.debug(sb);
                    objectCollector.dispose(sb);
                }
                return false;
            }
            if (log.isDebugEnabled()) {
                log.debug("Verification successful: ALL " + count + " ENTITIES VALID (" + (end - start) + " ms)");
            }
            return true;
        } finally {
            rollback.rollback();
        }
    }

    protected java.security.Signature getOrCreateVerifyHandle(ISignature signatureOfUser, IMap<ISignature, java.security.Signature> signatureToSignatureHandleMap) {
        try {
            java.security.Signature signatureHandle = signatureToSignatureHandleMap.get(signatureOfUser);
            if (signatureHandle == null) {
                signatureHandle = signatureUtil.createVerifyHandle(signatureOfUser.getSignAndVerify(), Base64.decode(signatureOfUser.getPublicKey()));
                signatureToSignatureHandleMap.put(signatureOfUser, signatureHandle);
            }
            return signatureHandle;
        } catch (Exception e) {
            throw RuntimeExceptionUtil.mask(e);
        }
    }

    @Override
    public boolean[] verifyAuditEntries(List<? extends IAuditEntry> auditEntries) {
        if (auditEntries.isEmpty()) {
            return EMPTY_VALIDATION_RESULT;
        }
        var rollback = securityActivation.pushWithoutSecurity();
        try {
            auditEntries = getFromCache(auditEntries);
            return verifyAuditEntriesIntern(auditEntries);
        } finally {
            rollback.rollback();
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private <V> List<? extends V> getFromCache(List<? extends V> auditEntries) {
        var objRefs = objRefHelper.extractObjRefList(auditEntries, null);
        var objects = cache.getObjects(objRefs, CacheDirective.none());
        if (objects.size() != auditEntries.size()) {
            throw new IllegalStateException("At least one " + IAuditEntry.class.getSimpleName() + " could not be resolved");
        }
        return (List) objects;
    }

    @SneakyThrows
    private boolean[] verifyAuditEntriesIntern(List<? extends IAuditEntry> auditEntries) {
        @SuppressWarnings("unused") IPrefetchState prefetch2 = getPref_SignaturesOfUser().prefetch(auditEntries);

        var result = new boolean[auditEntries.size()];
        Arrays.fill(result, true);
        var auditEntriesToVerify = new ArrayList<IAuditEntry>(auditEntries.size());
        var signatureToSignatureHandleMap = new HashMap<ISignature, java.security.Signature>();

        for (var auditEntry : auditEntries) {
            fillEntriesToVerify(auditEntry, auditEntry.getSignatureOfUser(), auditEntry.getSignedValue(), auditEntriesToVerify);
        }
        @SuppressWarnings("unused") IPrefetchState prefetch = getPref_verifyAuditEntries().prefetch(auditEntriesToVerify);

        for (int a = 0, size = auditEntriesToVerify.size(); a < size; a++) {
            var auditEntry = auditEntriesToVerify.get(a);
            if (auditEntry == null) {
                continue;
            }
            var signatureOfUser = auditEntry.getSignatureOfUser();
            var signature = auditEntry.getSignedValue();
            var signatureHandle = getOrCreateVerifyHandle(signatureOfUser, signatureToSignatureHandleMap);
            var digest = auditEntryToSignature.createVerifyDigest(auditEntry, (auditedEntity, currDigest) -> {
                try {
                    signatureHandle.update(currDigest);
                    return Boolean.valueOf(signatureHandle.verify(Base64.decode(auditedEntity.getSignedValue())));
                } catch (SignatureException | IOException e) {
                    throw RuntimeExceptionUtil.mask(e);
                }
            });
            if (digest == null) {
                result[a] = false;
                continue;
            }
            signatureHandle.update(digest);
            result[a] = signatureHandle.verify(Base64.decode(signature));
        }
        return result;
    }

    @Override
    public boolean[] verifyAuditedEntities(List<? extends IAuditedEntity> auditedEntities) {
        if (auditedEntities.isEmpty()) {
            return EMPTY_VALIDATION_RESULT;
        }
        var rollback = securityActivation.pushWithoutSecurity();
        try {
            auditedEntities = getFromCache(auditedEntities);
            return verifyAuditedEntitiesIntern(auditedEntities);
        } finally {
            rollback.rollback();
        }
    }

    private boolean[] verifyAuditedEntitiesIntern(List<? extends IAuditedEntity> auditedEntities) {
        @SuppressWarnings("unused") var prefetch2 = getPref_SignaturesOfUserFromAuditedEntity().prefetch(auditedEntities);

        var result = new boolean[auditedEntities.size()];
        Arrays.fill(result, true);
        ArrayList<IAuditedEntity> auditedEntitiesToVerify = new ArrayList<>(auditedEntities.size());
        var signatureToSignatureHandleMap = new HashMap<ISignature, java.security.Signature>();

        for (var auditedEntity : auditedEntities) {
            fillEntriesToVerify(auditedEntity, auditedEntity.getEntry().getSignatureOfUser(), auditedEntity.getSignedValue(), auditedEntitiesToVerify);
        }
        @SuppressWarnings("unused") var prefetch = getPref_verifyAuditEntriesFromAuditedEntity().prefetch(auditedEntitiesToVerify);

        for (int a = 0, size = auditedEntitiesToVerify.size(); a < size; a++) {
            var auditedEntity = auditedEntitiesToVerify.get(a);
            if (auditedEntity == null) {
                continue;
            }
            var signatureOfUser = auditedEntity.getEntry().getSignatureOfUser();
            var signature = auditedEntity.getSignedValue();
            try {
                var signatureHandle = getOrCreateVerifyHandle(signatureOfUser, signatureToSignatureHandleMap);
                var digest = auditEntryToSignature.createVerifyDigest(auditedEntity);
                if (digest == null) {
                    result[a] = false;
                    continue;
                }
                signatureHandle.update(digest);
                result[a] = signatureHandle.verify(Base64.decode(signature));
            } catch (Exception e) {
                throw RuntimeExceptionUtil.mask(e);
            }
        }
        return result;
    }

    protected <V> void fillEntriesToVerify(V value, ISignature signatureOfUser, char[] signature, List<? super V> entriesToVerify) {
        if (signatureOfUser == null) {
            if (signatureActive) {
                throw new IllegalArgumentException(((IEntityMetaDataHolder) value).get__EntityMetaData().getEntityType().getSimpleName() + " has no relation to a user signature: " + value);
            }
            // audit entries without a signature instance can not be verified but are intentionally
            // treated as "valid"
            entriesToVerify.add(null);
            return;
        }
        if (signature == null) {
            if (signatureActive) {
                throw new IllegalArgumentException(((IEntityMetaDataHolder) value).get__EntityMetaData().getEntityType().getSimpleName() + " has no valid signature: " + value);
            }
            // audit entries without a signed value can not be verified but are intentionally treated as
            // "valid"
            entriesToVerify.add(null);
            return;
        }
        entriesToVerify.add(value);
    }

    protected ILinkedMap<IEntityMetaData, IList<IObjRef>> bucketSortObjRefs(List<?> entities, boolean checkConfiguration) {
        var metaDataToObjRefsDict = new IdentityLinkedMap<IEntityMetaData, ISet<IObjRef>>();
        var hasAtLeastOneNonPrimaryObjRef = false;
        for (int i = entities.size(); i-- > 0; ) {
            var entity = entities.get(i);
            IObjRef objRef = null;
            IEntityMetaData metaData;
            if (entity instanceof IObjRef) {
                objRef = (IObjRef) entity;
                metaData = entityMetaDataProvider.getMetaData(objRef.getRealType());
            } else if (entity instanceof IEntityMetaDataHolder) {
                metaData = ((IEntityMetaDataHolder) entity).get__EntityMetaData();
            } else {
                throw new IllegalArgumentException("All objects must be either a valid entity or an " + IObjRef.class.getSimpleName() + " instance");
            }

            if (checkConfiguration) {
                var auditConfiguration = auditConfigurationProvider.getAuditConfiguration(metaData.getEntityType());
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
            var objRefs = metaDataToObjRefsDict.get(metaData);
            if (objRefs == null) {
                objRefs = new HashSet<>();
                metaDataToObjRefsDict.put(metaData, objRefs);
            }
            objRefs.add(objRef);
        }
        if (!hasAtLeastOneNonPrimaryObjRef) {
            return convertSetToList(metaDataToObjRefsDict);
        }
        transaction.runInTransaction(() -> {
            var database = AuditEntryVerifier.this.database.getCurrent();

            for (var entry : metaDataToObjRefsDict) {
                var objRefs = entry.getValue();
                HashMap<Byte, IList<IObjRef>> indexToObjRefsMap = null;
                var iter = objRefs.iterator();
                while (iter.hasNext()) {
                    var objRef = iter.next();
                    if (objRef.getIdNameIndex() == ObjRef.PRIMARY_KEY_INDEX) {
                        continue;
                    }
                    if (indexToObjRefsMap == null) {
                        indexToObjRefsMap = new HashMap<>();
                    }
                    var assignedObjRefs = indexToObjRefsMap.get(objRef.getIdNameIndex());
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
                var metaData = entry.getKey();
                var table = database.getTableByType(metaData.getEntityType());
                var idConverter = compositeIdFactory.prepareCompositeIdFactory(metaData, metaData.getIdMember());
                var preparedObjRefFactory = objRefFactory.prepareObjRefFactory(metaData.getEntityType(), ObjRef.PRIMARY_KEY_INDEX);
                for (var indexEntry : indexToObjRefsMap) {
                    var idIndex = indexEntry.getKey().intValue();
                    var alternateIdConverter = compositeIdFactory.prepareCompositeIdFactory(metaData, metaData.getIdMemberByIdIndex(idIndex));
                    var alternateIdObjRefs = indexEntry.getValue();
                    var alternateIdToVersionMap = HashMap.create(alternateIdObjRefs.size());
                    for (int a = alternateIdObjRefs.size(); a-- > 0; ) {
                        var alternateIdObjRef = alternateIdObjRefs.get(a);
                        alternateIdToVersionMap.put(alternateIdObjRef.getId(), alternateIdObjRef.getVersion());
                    }
                    var cursor = table.selectVersion(idIndex, alternateIdObjRefs);
                    try {
                        for (var item : cursor) {
                            var id = idConverter.convertValue(item.getId(), null);
                            var alternateId = alternateIdConverter.convertValue(item.getId(idIndex), null);
                            var version = alternateIdToVersionMap.get(alternateId);

                            var objRef = preparedObjRefFactory.createObjRef(id, version);
                            objRefs.add(objRef);
                        }
                    } finally {
                        cursor.dispose();
                    }
                }
            }
        });
        return convertSetToList(metaDataToObjRefsDict);
    }

    private ILinkedMap<IEntityMetaData, IList<IObjRef>> convertSetToList(IdentityLinkedMap<IEntityMetaData, ISet<IObjRef>> metaDataToObjRefsMap) {
        var targetMap = IdentityLinkedMap.<IEntityMetaData, IList<IObjRef>>create(metaDataToObjRefsMap.size());
        for (var entry : metaDataToObjRefsMap) {
            targetMap.put(entry.getKey(), entry.getValue().toList());
        }
        return targetMap;
    }

    protected void debugToLoad(List<IObjRef> orisToLoad, StringBuilder sb) {
        var count = orisToLoad.size();
        sb.append("List<").append(IObjRef.class.getSimpleName()).append("> : ").append(count).append(" item");
        if (count != 1) {
            sb.append('s');
        }
        sb.append(" [");

        int printBorder = 3, skipped = count >= maxDebugItems ? Math.max(0, count - printBorder * 2) : 0;
        for (int a = count; a-- > 0; ) {
            if (skipped > 1) {
                if (count - a > printBorder && a >= printBorder) {
                    continue;
                }
                if (a == printBorder - 1) {
                    sb.append("\r\n\t...skipped ").append(skipped).append(" items...");
                }
            }
            var oriToLoad = orisToLoad.get(a);
            if (count > 1) {
                sb.append("\r\n\t");
            }
            StringBuilderUtil.appendPrintable(sb, oriToLoad);
        }
        sb.append("]");
    }

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
}
