package de.osthus.ambeth.audit;

import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.locks.Lock;

import de.osthus.ambeth.IAuditEntryVerifier;
import de.osthus.ambeth.audit.model.IAuditEntry;
import de.osthus.ambeth.audit.model.IAuditedEntity;
import de.osthus.ambeth.audit.model.IAuditedEntityPrimitiveProperty;
import de.osthus.ambeth.audit.model.IAuditedEntityRef;
import de.osthus.ambeth.audit.model.IAuditedEntityRelationProperty;
import de.osthus.ambeth.cache.ClearAllCachesEvent;
import de.osthus.ambeth.codec.Base64;
import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.collections.HashSet;
import de.osthus.ambeth.collections.ILinkedMap;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.collections.IdentityHashSet;
import de.osthus.ambeth.collections.IdentityLinkedMap;
import de.osthus.ambeth.collections.LinkedHashMap;
import de.osthus.ambeth.collections.SmartCopyMap;
import de.osthus.ambeth.config.AuditConfigurationConstants;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.IStartingBean;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.IEntityMetaDataProvider;
import de.osthus.ambeth.merge.model.IEntityMetaData;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.merge.transfer.ObjRef;
import de.osthus.ambeth.metadata.PrimitiveMember;
import de.osthus.ambeth.metadata.RelationMember;
import de.osthus.ambeth.objectcollector.IThreadLocalObjectCollector;
import de.osthus.ambeth.query.IOperand;
import de.osthus.ambeth.query.IOperator;
import de.osthus.ambeth.query.IQuery;
import de.osthus.ambeth.query.IQueryBuilder;
import de.osthus.ambeth.query.IQueryBuilderFactory;
import de.osthus.ambeth.query.OrderByType;
import de.osthus.ambeth.security.ISignatureUtil;
import de.osthus.ambeth.security.model.ISignature;
import de.osthus.ambeth.util.IPrefetchHandle;
import de.osthus.ambeth.util.IPrefetchHelper;
import de.osthus.ambeth.util.IPrefetchState;

public class AuditEntryVerifier implements IAuditEntryVerifier, IStartingBean
{
	public static final String HANDLE_CLEAR_ALL_CACHES_EVENT = "handleClearAllCachesEvent";

	@LogInstance
	private ILogger log;

	@Autowired
	protected IAuditConfigurationProvider auditConfigurationProvider;

	@Autowired
	protected IAuditEntryToSignature auditEntryToSignature;

	@Autowired
	protected IEntityMetaDataProvider entityMetaDataProvider;

	@Autowired
	protected IThreadLocalObjectCollector objectCollector;

	@Autowired
	protected IPrefetchHelper prefetchHelper;

	@Autowired
	protected IQueryBuilderFactory queryBuilderFactory;

	@Autowired
	protected ISignatureUtil signatureUtil;

	@Property(name = AuditConfigurationConstants.AuditVerifyExpectSignature, defaultValue = "true")
	protected boolean expectSignatureOnVerify;

	protected final SmartCopyMap<Integer, IQuery<IAuditEntry>> entityTypeCountToQuery = new SmartCopyMap<Integer, IQuery<IAuditEntry>>();

	// this feature is still alpha status: optimize audit entry verification scope (do only set it to TRUE if you know what you do)
	protected boolean filterAuditEntries = false;

	protected IPrefetchHandle pref_filterAuditEntries, pref_verifyAuditEntries, prefetchSignaturesOfUser;

	@Override
	public void afterStarted() throws Throwable
	{
		pref_filterAuditEntries = prefetchHelper.createPrefetch()//
				.add(IAuditEntry.class, IAuditEntry.Entities)//
				.add(IAuditedEntity.class, IAuditedEntity.Ref, IAuditedEntity.Primitives, IAuditedEntity.Relations)//
				.add(IAuditedEntityRelationProperty.class, IAuditedEntityRelationProperty.Items)//
				.build();

		pref_verifyAuditEntries = prefetchHelper.createPrefetch()//
				.add(IAuditEntry.class, IAuditEntry.Services, IAuditEntry.Entities)//
				.add(IAuditedEntity.class, IAuditedEntity.Ref, IAuditedEntity.Primitives, IAuditedEntity.Relations)//
				.add(IAuditedEntityRelationProperty.class, IAuditedEntityRelationProperty.Items)//
				.build();

		prefetchSignaturesOfUser = prefetchHelper.createPrefetch()//
				.add(IAuditEntry.class, IAuditEntry.SignatureOfUser)//
				.build();
	}

	public void handleClearAllCachesEvent(ClearAllCachesEvent evnt)
	{
		Lock writeLock = entityTypeCountToQuery.getWriteLock();
		writeLock.lock();
		try
		{
			for (IQuery<IAuditEntry> query : entityTypeCountToQuery.values())
			{
				query.dispose();
			}
			entityTypeCountToQuery.clear();
		}
		finally
		{
			writeLock.unlock();
		}
	}

	protected IQuery<IAuditEntry> resolveQuery(ILinkedMap<IEntityMetaData, IList<IObjRef>> bucketSortObjRefs)
	{
		IQuery<IAuditEntry> query = entityTypeCountToQuery.get(Integer.valueOf(bucketSortObjRefs.size()));
		if (query != null)
		{
			return query;
		}
		IQueryBuilder<IAuditEntry> qb = queryBuilderFactory.create(IAuditEntry.class);

		String refPath = IAuditEntry.Entities + "." + IAuditedEntity.Ref;
		IOperand entityTypeProp = qb.property(refPath + "." + IAuditedEntityRef.EntityType);
		IOperand entityIdProp = qb.property(refPath + "." + IAuditedEntityRef.EntityId);
		int index = 0;
		IOperator op = null;
		for (int a = bucketSortObjRefs.size(); a-- > 0;)
		{
			IOperator typeMatchOp = qb.isEqualTo(entityTypeProp, qb.valueName("param" + index++));
			IOperator idMatchOp = qb.isIn(entityIdProp, qb.valueName("param" + index++));
			IOperator matchOp = qb.and(typeMatchOp, idMatchOp);

			if (op == null)
			{
				op = matchOp;
			}
			else
			{
				op = qb.or(op, matchOp);
			}
		}
		query = qb.orderBy(qb.property(IAuditEntry.Timestamp), OrderByType.ASC).build(op);

		entityTypeCountToQuery.put(Integer.valueOf(bucketSortObjRefs.size()), query);
		return query;
	}

	protected IList<IAuditEntry> filterAuditEntries(IList<IAuditEntry> auditEntries, IMap<IObjRef, HashSet<String>> remainingPropertyMap)
	{
		if (!filterAuditEntries)
		{
			return auditEntries;
		}
		@SuppressWarnings("unused")
		IPrefetchState prefetch = pref_filterAuditEntries.prefetch(auditEntries);

		if (log.isDebugEnabled())
		{
			log.debug("Filtering audit entries which cover the last state-change of each property of each entity...");
		}
		IdentityHashSet<IAuditEntry> auditEntriesToVerify = IdentityHashSet.<IAuditEntry> create(auditEntries.size());

		ObjRef tempObjRef = new ObjRef();
		tempObjRef.setIdNameIndex(ObjRef.PRIMARY_KEY_INDEX);

		// audit entries are ordered by timestamp DESC. So the newest auditEntries are last
		// we iterate from back to front and break if we have found enough audit entries to cover all relevant properties
		for (int a = auditEntries.size(); a-- > 0;)
		{
			if (remainingPropertyMap.size() == 0)
			{
				// no audit entries to consider any more
				break;
			}
			IAuditEntry auditEntry = auditEntries.get(a);
			List<? extends IAuditedEntity> entities = auditEntry.getEntities();
			for (int b = entities.size(); b-- > 0;)
			{
				IAuditedEntity auditedEntity = entities.get(b);
				IAuditedEntityRef ref = auditedEntity.getRef();

				tempObjRef.setRealType(ref.getEntityType());
				tempObjRef.setId(ref.getEntityId());

				HashSet<String> remainingPropertyMapOfEntity = remainingPropertyMap.get(tempObjRef);
				if (remainingPropertyMapOfEntity == null)
				{
					// this auditedEntity is not relevant for any of the remaining mappings
					continue;
				}
				List<? extends IAuditedEntityPrimitiveProperty> primitives = auditedEntity.getPrimitives();
				for (int c = primitives.size(); c-- > 0;)
				{
					if (remainingPropertyMapOfEntity.remove(primitives.get(c).getName()))
					{
						auditEntriesToVerify.add(auditEntry);
					}
				}
				List<? extends IAuditedEntityRelationProperty> relations = auditedEntity.getRelations();
				for (int c = relations.size(); c-- > 0;)
				{
					if (remainingPropertyMapOfEntity.remove(relations.get(c).getName()))
					{
						auditEntriesToVerify.add(auditEntry);
					}
				}
				if (remainingPropertyMapOfEntity.size() == 0)
				{
					// all auditEntries covering the last state-change of each property of the current entity have been resolved
					remainingPropertyMap.remove(tempObjRef);

					if (remainingPropertyMap.size() == 0)
					{
						// no audit entries to consider any more
						break;
					}
				}
			}
		}
		if (log.isDebugEnabled())
		{
			IThreadLocalObjectCollector objectCollector = this.objectCollector.getCurrent();
			StringBuilder sb = objectCollector.create(StringBuilder.class);
			sb.append(auditEntriesToVerify.size()).append(" of ").append(auditEntries.size())
					.append(" needed to cover the last state-change of each property of each entity (")
					.append(auditEntriesToVerify.size() * 100 / auditEntries.size()).append(" %)");
			log.debug(sb);
			objectCollector.dispose(sb);
		}
		return auditEntriesToVerify.toList();
	}

	protected IMap<IObjRef, HashSet<String>> fillNameToValueMap(IMap<String, Object> nameToValueMap,
			ILinkedMap<IEntityMetaData, IList<IObjRef>> bucketSortObjRefs)
	{
		HashMap<IObjRef, HashSet<String>> remainingPropertyMap = filterAuditEntries ? new HashMap<IObjRef, HashSet<String>>() : null;

		for (Entry<IEntityMetaData, IList<IObjRef>> entry : bucketSortObjRefs)
		{
			IEntityMetaData metaData = entry.getKey();
			IList<IObjRef> objRefsOfEntityType = entry.getValue();

			nameToValueMap.put("param" + nameToValueMap.size(), metaData.getEntityType());

			Object[] ids = new Object[objRefsOfEntityType.size()];
			for (int a = objRefsOfEntityType.size(); a-- > 0;)
			{
				ids[a] = objRefsOfEntityType.get(a).getId();
			}
			nameToValueMap.put("param" + nameToValueMap.size(), ids);

			if (!filterAuditEntries)
			{
				continue;
			}
			HashSet<String> allMembersSet = new HashSet<String>();
			for (PrimitiveMember primitiveMember : metaData.getPrimitiveMembers())
			{
				allMembersSet.add(primitiveMember.getName());
			}
			for (RelationMember relationMember : metaData.getRelationMembers())
			{
				allMembersSet.add(relationMember.getName());
			}
			if (metaData.getVersionMember() != null)
			{
				allMembersSet.add(metaData.getVersionMember().getName());
			}
			for (int a = objRefsOfEntityType.size(); a-- > 0;)
			{
				IObjRef objRef = objRefsOfEntityType.get(a);
				remainingPropertyMap.put(objRef, new HashSet<String>(allMembersSet));
			}
		}
		return remainingPropertyMap;
	}

	@Override
	public boolean verifyEntities(List<? extends IObjRef> objRefs)
	{
		if (objRefs.size() == 0)
		{
			return true;
		}
		ILinkedMap<IEntityMetaData, IList<IObjRef>> bucketSortObjRefs = bucketSortObjRefs(objRefs);
		if (bucketSortObjRefs.size() == 0)
		{
			return true;
		}
		LinkedHashMap<String, Object> nameToValueMap = new LinkedHashMap<String, Object>();
		IMap<IObjRef, HashSet<String>> remainingPropertyMap = fillNameToValueMap(nameToValueMap, bucketSortObjRefs);

		if (log.isDebugEnabled())
		{
			IThreadLocalObjectCollector objectCollector = this.objectCollector.getCurrent();
			StringBuilder sb = objectCollector.create(StringBuilder.class);
			sb.append("Searching for all audit entries covering the following " + objRefs.size() + " entities: ");

			for (Entry<IEntityMetaData, IList<IObjRef>> entry : bucketSortObjRefs)
			{
				sb.append("\n\r").append(entry.getValue().size()).append("x ").append(entry.getKey().getEntityType().getName());
			}
			log.debug(sb);
			objectCollector.dispose(sb);
		}
		IQuery<IAuditEntry> query = resolveQuery(bucketSortObjRefs);
		for (Entry<String, Object> entry : nameToValueMap)
		{
			query = query.param(entry.getKey(), entry.getValue());
		}
		IList<IAuditEntry> auditEntries = query.retrieve();
		IList<IAuditEntry> auditEntriesToVerify = filterAuditEntries(auditEntries, remainingPropertyMap);
		return verifyAuditEntries(auditEntriesToVerify);
	}

	@Override
	public boolean verifyAuditEntries(List<? extends IAuditEntry> auditEntries)
	{
		@SuppressWarnings("unused")
		IPrefetchState prefetch2 = prefetchSignaturesOfUser.prefetch(auditEntries);

		ArrayList<IAuditEntry> auditEntriesToVerify = new ArrayList<IAuditEntry>(auditEntries.size());
		HashMap<ISignature, java.security.Signature> signatureToSignatureHandleMap = new HashMap<ISignature, java.security.Signature>();
		for (IAuditEntry auditEntry : auditEntries)
		{
			ISignature signatureOfUser = auditEntry.getSignatureOfUser();
			char[] signature = auditEntry.getSignature();
			if (signature == null && expectSignatureOnVerify)
			{
				return false;
			}
			if (signatureOfUser == null)
			{
				if (signature == null)
				{
					// audit entries without a signature can not be verified but are intentionally treated as "valid"
					continue;
				}
				throw new IllegalArgumentException(IAuditEntry.class.getSimpleName() + " has no relation to a user signature: " + auditEntry);
			}
			auditEntriesToVerify.add(auditEntry);
		}
		@SuppressWarnings("unused")
		IPrefetchState prefetch = pref_verifyAuditEntries.prefetch(auditEntriesToVerify);

		for (IAuditEntry auditEntry : auditEntriesToVerify)
		{
			ISignature signatureOfUser = auditEntry.getSignatureOfUser();
			char[] signature = auditEntry.getSignature();
			try
			{
				java.security.Signature signatureHandle = signatureToSignatureHandleMap.get(signatureOfUser);
				if (signatureHandle == null)
				{
					signatureHandle = signatureUtil.createVerifyHandle(signatureOfUser.getSignAndVerify(), Base64.decode(signatureOfUser.getPublicKey()));
					signatureToSignatureHandleMap.put(signatureOfUser, signatureHandle);
				}
				auditEntryToSignature.writeToSignatureHandle(signatureHandle, auditEntry, null);
				if (!signatureHandle.verify(Base64.decode(signature)))
				{
					return false;
				}
			}
			catch (Throwable e)
			{
				throw RuntimeExceptionUtil.mask(e);
			}
		}
		return true;
	}

	protected ILinkedMap<IEntityMetaData, IList<IObjRef>> bucketSortObjRefs(List<? extends IObjRef> orisToLoad)
	{
		IdentityLinkedMap<IEntityMetaData, IList<IObjRef>> serviceToAssignedObjRefsDict = new IdentityLinkedMap<IEntityMetaData, IList<IObjRef>>();

		for (int i = orisToLoad.size(); i-- > 0;)
		{
			IObjRef objRef = orisToLoad.get(i);
			IEntityMetaData metaData = entityMetaDataProvider.getMetaData(objRef.getRealType());

			IAuditConfiguration auditConfiguration = auditConfigurationProvider.getAuditConfiguration(metaData.getEntityType());
			if (!auditConfiguration.isAuditActive())
			{
				continue;
			}
			IList<IObjRef> assignedObjRefs = serviceToAssignedObjRefsDict.get(metaData);
			if (assignedObjRefs == null)
			{
				assignedObjRefs = new ArrayList<IObjRef>();
				serviceToAssignedObjRefsDict.put(metaData, assignedObjRefs);
			}
			assignedObjRefs.add(objRef);
		}
		return serviceToAssignedObjRefsDict;
	}

}
