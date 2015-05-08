package de.osthus.ambeth.audit;

import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.locks.Lock;

import de.osthus.ambeth.audit.model.IAuditEntry;
import de.osthus.ambeth.audit.model.IAuditedEntity;
import de.osthus.ambeth.audit.model.IAuditedEntityPrimitiveProperty;
import de.osthus.ambeth.audit.model.IAuditedEntityRef;
import de.osthus.ambeth.audit.model.IAuditedEntityRelationProperty;
import de.osthus.ambeth.audit.model.IAuditedEntityRelationPropertyItem;
import de.osthus.ambeth.cache.ClearAllCachesEvent;
import de.osthus.ambeth.codec.Base64;
import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.collections.HashSet;
import de.osthus.ambeth.collections.ILinkedMap;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.collections.IdentityLinkedMap;
import de.osthus.ambeth.collections.IdentityLinkedSet;
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
import de.osthus.ambeth.util.IConversionHelper;
import de.osthus.ambeth.util.IPrefetchHandle;
import de.osthus.ambeth.util.IPrefetchHelper;
import de.osthus.ambeth.util.IPrefetchState;
import de.osthus.ambeth.util.StringBuilderUtil;

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
	protected IConversionHelper conversionHelper;

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

	@Property(name = AuditConfigurationConstants.VerifyEntitiesOnLoadActive, defaultValue = "false")
	protected boolean verifyEntitiesOnLoadActive;

	protected final SmartCopyMap<Integer, IQuery<IAuditedEntity>> entityTypeCountToQuery = new SmartCopyMap<Integer, IQuery<IAuditedEntity>>();

	// this feature is still alpha status: optimize audit entry verification scope (do only set it to TRUE if you know what you do)
	protected boolean filterAuditedEntities = false;

	protected IPrefetchHandle pref_filterAuditedEntities, pref_verifyAuditEntries, prefetchSignaturesOfUser, prefetchSignaturesOfUserFromAuditedEntity,
			pref_verifyAuditEntriesFromAuditedEntity;

	protected int maxDebugItems = 50;

	@Override
	public void afterStarted() throws Throwable
	{
		pref_filterAuditedEntities = prefetchHelper.createPrefetch()//
				.add(IAuditedEntity.class, IAuditedEntity.Entry, IAuditedEntity.Ref, IAuditedEntity.Primitives, IAuditedEntity.Relations)//
				.add(IAuditedEntityRelationProperty.class, IAuditedEntityRelationProperty.Items)//
				.add(IAuditedEntityRelationPropertyItem.class, IAuditedEntityRelationPropertyItem.Ref)//
				.build();

		pref_verifyAuditEntries = prefetchHelper.createPrefetch()//
				.add(IAuditEntry.class, IAuditEntry.Services, IAuditEntry.Entities)//
				.add(IAuditedEntity.class, IAuditedEntity.Ref, IAuditedEntity.Primitives, IAuditedEntity.Relations)//
				.add(IAuditedEntityRelationProperty.class, IAuditedEntityRelationProperty.Items)//
				.add(IAuditedEntityRelationPropertyItem.class, IAuditedEntityRelationPropertyItem.Ref)//
				.build();

		prefetchSignaturesOfUser = prefetchHelper.createPrefetch()//
				.add(IAuditEntry.class, IAuditEntry.SignatureOfUser)//
				.build();

		prefetchSignaturesOfUserFromAuditedEntity = prefetchHelper.createPrefetch()//
				.add(IAuditedEntity.class, IAuditedEntity.Entry + "." + IAuditEntry.SignatureOfUser)//
				.build();

		pref_verifyAuditEntriesFromAuditedEntity = prefetchHelper.createPrefetch()//
				.add(IAuditedEntity.class, IAuditedEntity.Ref, IAuditedEntity.Primitives, IAuditedEntity.Relations)//
				.add(IAuditedEntityRelationProperty.class, IAuditedEntityRelationProperty.Items)//
				.add(IAuditedEntityRelationPropertyItem.class, IAuditedEntityRelationPropertyItem.Ref)//
				.build();
	}

	public void handleClearAllCachesEvent(ClearAllCachesEvent evnt)
	{
		Lock writeLock = entityTypeCountToQuery.getWriteLock();
		writeLock.lock();
		try
		{
			for (IQuery<?> query : entityTypeCountToQuery.values())
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

	protected IQuery<IAuditedEntity> resolveQuery(ILinkedMap<IEntityMetaData, IList<IObjRef>> bucketSortObjRefs)
	{
		IQuery<IAuditedEntity> query = entityTypeCountToQuery.get(Integer.valueOf(bucketSortObjRefs.size()));
		if (query != null)
		{
			return query;
		}
		IQueryBuilder<IAuditedEntity> qb = queryBuilderFactory.create(IAuditedEntity.class);

		IOperand entityTypeProp = qb.property(IAuditedEntity.Ref + "." + IAuditedEntityRef.EntityType);
		IOperand entityIdProp = qb.property(IAuditedEntity.Ref + "." + IAuditedEntityRef.EntityId);
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
		query = qb.orderBy(qb.property(IAuditedEntity.Entry + "." + IAuditEntry.Timestamp), OrderByType.ASC).build(op);

		entityTypeCountToQuery.put(Integer.valueOf(bucketSortObjRefs.size()), query);
		return query;
	}

	protected IList<IAuditedEntity> filterAuditedEntities(IList<IAuditedEntity> auditedEntities, IMap<IObjRef, HashSet<String>> remainingPropertyMap)
	{
		if (!filterAuditedEntities || auditedEntities.size() == 0)
		{
			return auditedEntities;
		}
		@SuppressWarnings("unused")
		IPrefetchState prefetch = pref_filterAuditedEntities.prefetch(auditedEntities);

		if (log.isDebugEnabled())
		{
			log.debug("Filtering audit entries which cover the last state-change of each property of each entity...");
		}
		IdentityLinkedSet<IAuditedEntity> auditEntitiesToVerify = IdentityLinkedSet.<IAuditedEntity> create(auditedEntities.size());

		ObjRef tempObjRef = new ObjRef();
		tempObjRef.setIdNameIndex(ObjRef.PRIMARY_KEY_INDEX);

		// audit entries are ordered by timestamp DESC. So the newest auditEntries are last
		// we iterate from back to front and break if we have found enough audit entries to cover all relevant properties
		for (int a = 0, size = auditedEntities.size(); a < size; a++)
		{
			if (remainingPropertyMap.size() == 0)
			{
				// no audit entries to consider any more
				break;
			}
			IAuditedEntity auditedEntity = auditedEntities.get(a);
			IAuditedEntityRef ref = auditedEntity.getRef();

			IEntityMetaData metaData = entityMetaDataProvider.getMetaData(ref.getEntityType());

			tempObjRef.setRealType(metaData.getEntityType());
			tempObjRef.setId(conversionHelper.convertValueToType(metaData.getIdMember().getRealType(), ref.getEntityId()));

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
					auditEntitiesToVerify.add(auditedEntity);
				}
			}
			List<? extends IAuditedEntityRelationProperty> relations = auditedEntity.getRelations();
			for (int c = relations.size(); c-- > 0;)
			{
				if (remainingPropertyMapOfEntity.remove(relations.get(c).getName()))
				{
					auditEntitiesToVerify.add(auditedEntity);
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
		if (log.isDebugEnabled())
		{
			IThreadLocalObjectCollector objectCollector = this.objectCollector.getCurrent();
			StringBuilder sb = objectCollector.create(StringBuilder.class);
			sb.append(auditEntitiesToVerify.size()).append(" of ").append(auditedEntities.size())
					.append(" needed to cover the last state-change of each property of each entity (")
					.append(auditEntitiesToVerify.size() * 100 / auditedEntities.size()).append(" %)");
			log.debug(sb);
			objectCollector.dispose(sb);
		}
		return auditEntitiesToVerify.toList();
	}

	protected IMap<IObjRef, HashSet<String>> fillNameToValueMap(IMap<String, Object> nameToValueMap,
			ILinkedMap<IEntityMetaData, IList<IObjRef>> bucketSortObjRefs)
	{
		HashMap<IObjRef, HashSet<String>> remainingPropertyMap = filterAuditedEntities ? new HashMap<IObjRef, HashSet<String>>() : null;

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

			if (!filterAuditedEntities)
			{
				continue;
			}
			HashSet<String> allMembersSet = new HashSet<String>();
			for (PrimitiveMember primitiveMember : metaData.getPrimitiveMembers())
			{
				if (metaData.getUpdatedByMember() == primitiveMember || metaData.getUpdatedOnMember() == primitiveMember
						|| metaData.getCreatedByMember() == primitiveMember || metaData.getCreatedOnMember() == primitiveMember)
				{
					continue;
				}
				allMembersSet.add(primitiveMember.getName());
			}
			for (RelationMember relationMember : metaData.getRelationMembers())
			{
				allMembersSet.add(relationMember.getName());
			}
			// if (metaData.getVersionMember() != null)
			// {
			// allMembersSet.add(metaData.getVersionMember().getName());
			// }
			for (int a = objRefsOfEntityType.size(); a-- > 0;)
			{
				IObjRef objRef = objRefsOfEntityType.get(a);
				remainingPropertyMap.put(objRef, new HashSet<String>(allMembersSet));
			}
		}
		return remainingPropertyMap;
	}

	@Override
	public void verifyEntitiesOnLoad(List<? extends IObjRef> objRefs)
	{
		if (!verifyEntitiesOnLoadActive)
		{
			return;
		}
		if (!verifyEntities(objRefs))
		{
			log.error("Audit entry verification failed: " + Arrays.toString(objRefs.toArray()));
		}
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

		int count = 0;
		for (Entry<IEntityMetaData, IList<IObjRef>> entry : bucketSortObjRefs)
		{
			count += entry.getValue().size();
		}

		if (log.isDebugEnabled())
		{
			IThreadLocalObjectCollector objectCollector = this.objectCollector.getCurrent();
			StringBuilder sb = objectCollector.create(StringBuilder.class);
			sb.append("Verifying audit entries covering the following " + count + " entities: ");

			ArrayList<IObjRef> allObjRefs = new ArrayList<IObjRef>(count);
			for (Entry<IEntityMetaData, IList<IObjRef>> entry : bucketSortObjRefs)
			{
				allObjRefs.addAll(entry.getValue());
			}
			for (Entry<IEntityMetaData, IList<IObjRef>> entry : bucketSortObjRefs)
			{
				sb.append("\n\t").append(entry.getValue().size()).append("x ").append(entry.getKey().getEntityType().getName());
			}
			if (log.isDebugEnabled())
			{
				sb.append("\n\t");
				debugToLoad(allObjRefs, sb);
				log.debug(sb);
			}
			else if (log.isInfoEnabled())
			{
				log.info(sb);
			}
			objectCollector.dispose(sb);
		}
		IQuery<IAuditedEntity> query = resolveQuery(bucketSortObjRefs);
		for (Entry<String, Object> entry : nameToValueMap)
		{
			query = query.param(entry.getKey(), entry.getValue());
		}
		IList<IAuditedEntity> auditedEntities = query.retrieve();
		IList<IAuditedEntity> auditedEntitiesToVerify = filterAuditedEntities(auditedEntities, remainingPropertyMap);
		boolean[] verifyAuditEntries = verifyAuditedEntities(auditedEntitiesToVerify);
		for (boolean result : verifyAuditEntries)
		{
			if (!result)
			{
				return false;
			}
		}
		if (log.isInfoEnabled())
		{
			log.info("Verification successful: ALL " + count + " ENTITIES VALID");
		}
		return true;
	}

	@Override
	public boolean[] verifyAuditEntries(List<? extends IAuditEntry> auditEntries)
	{
		if (auditEntries.size() == 0)
		{
			return new boolean[0];
		}
		@SuppressWarnings("unused")
		IPrefetchState prefetch2 = prefetchSignaturesOfUser.prefetch(auditEntries);

		boolean[] result = new boolean[auditEntries.size()];
		ArrayList<IAuditEntry> auditEntriesToVerify = new ArrayList<IAuditEntry>(auditEntries.size());
		HashMap<ISignature, java.security.Signature> signatureToSignatureHandleMap = new HashMap<ISignature, java.security.Signature>();
		for (IAuditEntry auditEntry : auditEntries)
		{
			ISignature signatureOfUser = auditEntry.getSignatureOfUser();
			char[] signature = auditEntry.getSignature();
			if (signature == null && expectSignatureOnVerify)
			{
				continue;
			}
			if (signatureOfUser == null)
			{
				if (signature == null)
				{
					auditEntriesToVerify.add(null);
					// audit entries without a signature can not be verified but are intentionally treated as "valid"
					continue;
				}
				throw new IllegalArgumentException(IAuditEntry.class.getSimpleName() + " has no relation to a user signature: " + auditEntry);
			}
			auditEntriesToVerify.add(auditEntry);
		}
		@SuppressWarnings("unused")
		IPrefetchState prefetch = pref_verifyAuditEntries.prefetch(auditEntriesToVerify);

		for (int a = 0, size = auditEntriesToVerify.size(); a < size; a++)
		{
			IAuditEntry auditEntry = auditEntriesToVerify.get(a);
			if (auditEntry == null)
			{
				result[a] = true;
				continue;
			}
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
				byte[] digest = auditEntryToSignature.createVerifyDigest(auditEntry, signatureHandle);
				if (digest == null)
				{
					result[a] = false;
					continue;
				}
				signatureHandle.update(digest);
				result[a] = signatureHandle.verify(Base64.decode(signature));
			}
			catch (Throwable e)
			{
				throw RuntimeExceptionUtil.mask(e);
			}
		}
		return result;
	}

	@Override
	public boolean[] verifyAuditedEntities(List<? extends IAuditedEntity> auditedEntities)
	{
		if (auditedEntities.size() == 0)
		{
			return new boolean[0];
		}
		@SuppressWarnings("unused")
		IPrefetchState prefetch2 = prefetchSignaturesOfUserFromAuditedEntity.prefetch(auditedEntities);

		boolean[] result = new boolean[auditedEntities.size()];
		ArrayList<IAuditedEntity> auditEntriesToVerify = new ArrayList<IAuditedEntity>(auditedEntities.size());
		HashMap<ISignature, java.security.Signature> signatureToSignatureHandleMap = new HashMap<ISignature, java.security.Signature>();
		for (IAuditedEntity auditedEntity : auditedEntities)
		{
			ISignature signatureOfUser = auditedEntity.getEntry().getSignatureOfUser();
			char[] signature = auditedEntity.getSignature();
			if (signature == null && expectSignatureOnVerify)
			{
				continue;
			}
			if (signatureOfUser == null)
			{
				if (signature == null)
				{
					auditEntriesToVerify.add(null);
					// audit entries without a signature can not be verified but are intentionally treated as "valid"
					continue;
				}
				throw new IllegalArgumentException(IAuditedEntity.class.getSimpleName() + " has no relation to a user signature: " + auditedEntity);
			}
			auditEntriesToVerify.add(auditedEntity);
		}
		@SuppressWarnings("unused")
		IPrefetchState prefetch = pref_verifyAuditEntriesFromAuditedEntity.prefetch(auditEntriesToVerify);

		for (int a = 0, size = auditEntriesToVerify.size(); a < size; a++)
		{
			IAuditedEntity auditEntry = auditEntriesToVerify.get(a);
			if (auditEntry == null)
			{
				result[a] = true;
				continue;
			}
			ISignature signatureOfUser = auditEntry.getEntry().getSignatureOfUser();
			char[] signature = auditEntry.getSignature();
			try
			{
				java.security.Signature signatureHandle = signatureToSignatureHandleMap.get(signatureOfUser);
				if (signatureHandle == null)
				{
					signatureHandle = signatureUtil.createVerifyHandle(signatureOfUser.getSignAndVerify(), Base64.decode(signatureOfUser.getPublicKey()));
					signatureToSignatureHandleMap.put(signatureOfUser, signatureHandle);
				}
				byte[] digest = auditEntryToSignature.createVerifyDigest(auditEntry);
				signatureHandle.update(digest);
				result[a] = signatureHandle.verify(Base64.decode(signature));
			}
			catch (Throwable e)
			{
				throw RuntimeExceptionUtil.mask(e);
			}
		}
		return result;
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

	protected void debugToLoad(List<IObjRef> orisToLoad, StringBuilder sb)
	{
		int count = orisToLoad.size();
		sb.append("List<IObjRef> : ").append(count).append(" item");
		if (count != 1)
		{
			sb.append('s');
		}
		sb.append(" [");

		int printBorder = 3, skipped = count >= maxDebugItems ? Math.max(0, count - printBorder * 2) : 0;
		for (int a = count; a-- > 0;)
		{
			if (skipped > 1)
			{
				if (count - a > printBorder && a >= printBorder)
				{
					continue;
				}
				if (a == printBorder - 1)
				{
					sb.append("\r\n\t...skipped ").append(skipped).append(" items...");
				}
			}
			IObjRef oriToLoad = orisToLoad.get(a);
			if (count > 1)
			{
				sb.append("\r\n\t");
			}
			StringBuilderUtil.appendPrintable(sb, oriToLoad);
		}
		sb.append("]");
	}
}
