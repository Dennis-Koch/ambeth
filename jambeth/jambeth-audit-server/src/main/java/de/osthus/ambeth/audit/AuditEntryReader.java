package de.osthus.ambeth.audit;

import java.util.List;

import de.osthus.ambeth.audit.model.IAuditEntry;
import de.osthus.ambeth.audit.model.IAuditedEntity;
import de.osthus.ambeth.audit.model.IAuditedEntityRef;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.ioc.IStartingBean;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.IEntityMetaDataProvider;
import de.osthus.ambeth.merge.model.IEntityMetaData;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.metadata.PrimitiveMember;
import de.osthus.ambeth.query.IOperand;
import de.osthus.ambeth.query.IQuery;
import de.osthus.ambeth.query.IQueryBuilder;
import de.osthus.ambeth.query.IQueryBuilderFactory;
import de.osthus.ambeth.query.OrderByType;

public class AuditEntryReader implements IAuditEntryReader, IStartingBean
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IEntityMetaDataProvider entityMetaDataProvider;

	@Autowired
	protected IQueryBuilderFactory queryBuilderFactory;

	private IQuery<IAuditedEntity> q_auditedEntity_withVersion, q_auditedEntity_noVersion;

	private IQuery<IAuditEntry> q_auditEntry_withVersion, q_auditEntry_noVersion;

	@Override
	public void afterStarted() throws Throwable
	{
		{
			IQueryBuilder<IAuditedEntity> qb = queryBuilderFactory.create(IAuditedEntity.class);

			IOperand entityType = qb.property(IAuditedEntity.Ref + "." + IAuditedEntityRef.EntityType);
			IOperand entityId = qb.property(IAuditedEntity.Ref + "." + IAuditedEntityRef.EntityId);
			IOperand entityVersion = qb.property(IAuditedEntity.Ref + "." + IAuditedEntityRef.EntityVersion);

			qb.orderBy(qb.property(IAuditedEntity.Entry + "." + IAuditEntry.Timestamp), OrderByType.ASC);

			q_auditedEntity_withVersion = qb.build(qb.and(
					//
					qb.isEqualTo(entityType, qb.valueName(IAuditedEntityRef.EntityType)),//
					qb.isEqualTo(entityId, qb.valueName(IAuditedEntityRef.EntityId)),
					qb.isLessThanOrEqualTo(entityVersion, qb.valueName(IAuditedEntityRef.EntityVersion))));
		}
		{
			IQueryBuilder<IAuditedEntity> qb = queryBuilderFactory.create(IAuditedEntity.class);
			IOperand entityType = qb.property(IAuditedEntity.Ref + "." + IAuditedEntityRef.EntityType);
			IOperand entityId = qb.property(IAuditedEntity.Ref + "." + IAuditedEntityRef.EntityId);

			qb.orderBy(qb.property(IAuditedEntity.Entry + "." + IAuditEntry.Timestamp), OrderByType.ASC);

			q_auditedEntity_noVersion = qb.build(qb.and(//
					qb.isEqualTo(entityType, qb.valueName(IAuditedEntityRef.EntityType)),//
					qb.isEqualTo(entityId, qb.valueName(IAuditedEntityRef.EntityId))));
		}
		{
			IQueryBuilder<IAuditEntry> qb = queryBuilderFactory.create(IAuditEntry.class);

			IOperand entityType = qb.property(IAuditEntry.Entities + "." + IAuditedEntity.Ref + "." + IAuditedEntityRef.EntityType);
			IOperand entityId = qb.property(IAuditEntry.Entities + "." + IAuditedEntity.Ref + "." + IAuditedEntityRef.EntityId);
			IOperand entityVersion = qb.property(IAuditEntry.Entities + "." + IAuditedEntity.Ref + "." + IAuditedEntityRef.EntityVersion);

			qb.orderBy(qb.property(IAuditEntry.Timestamp), OrderByType.ASC);

			q_auditEntry_withVersion = qb.build(qb.and(
					//
					qb.isEqualTo(entityType, qb.valueName(IAuditedEntityRef.EntityType)),//
					qb.isEqualTo(entityId, qb.valueName(IAuditedEntityRef.EntityId)),
					qb.isLessThanOrEqualTo(entityVersion, qb.valueName(IAuditedEntityRef.EntityVersion))));
		}
		{
			IQueryBuilder<IAuditEntry> qb = queryBuilderFactory.create(IAuditEntry.class);
			IOperand entityType = qb.property(IAuditEntry.Entities + "." + IAuditedEntity.Ref + "." + IAuditedEntityRef.EntityType);
			IOperand entityId = qb.property(IAuditEntry.Entities + "." + IAuditedEntity.Ref + "." + IAuditedEntityRef.EntityId);

			qb.orderBy(qb.property(IAuditEntry.Timestamp), OrderByType.ASC);

			q_auditEntry_noVersion = qb.build(qb.and(//
					qb.isEqualTo(entityType, qb.valueName(IAuditedEntityRef.EntityType)),//
					qb.isEqualTo(entityId, qb.valueName(IAuditedEntityRef.EntityId))));
		}
	}

	protected <V> IList<V> getByObjRef(IQuery<V> q_NoVersion, IQuery<V> q_WithVersion, IObjRef objRef)
	{
		IEntityMetaData metaData = entityMetaDataProvider.getMetaData(objRef.getRealType());

		if (objRef.getVersion() == null)
		{
			return q_NoVersion.param(IAuditedEntityRef.EntityType, metaData.getEntityType())//
					.param(IAuditedEntityRef.EntityId, objRef.getId())//
					.retrieve();
		}
		return q_WithVersion.param(IAuditedEntityRef.EntityType, metaData.getEntityType())//
				.param(IAuditedEntityRef.EntityId, objRef.getId())//
				.param(IAuditedEntityRef.EntityVersion, objRef.getVersion())//
				.retrieve();
	}

	protected <V> List<V> getByEntity(IQuery<V> q_NoVersion, IQuery<V> q_WithVersion, Object entity)
	{
		if (entity instanceof IObjRef)
		{
			return getByObjRef(q_NoVersion, q_WithVersion, (IObjRef) entity);
		}
		IEntityMetaData metaData = entityMetaDataProvider.getMetaData(entity.getClass());

		PrimitiveMember versionMember = metaData.getVersionMember();
		Object version = null;
		if (versionMember != null)
		{
			version = versionMember.getValue(entity);
		}
		Object id = metaData.getIdMember().getValue(entity);
		if (version == null)
		{
			return q_NoVersion.param(IAuditedEntityRef.EntityType, metaData.getEntityType())//
					.param(IAuditedEntityRef.EntityId, id)//
					.retrieve();
		}
		return q_WithVersion.param(IAuditedEntityRef.EntityType, metaData.getEntityType())//
				.param(IAuditedEntityRef.EntityId, id)//
				.param(IAuditedEntityRef.EntityVersion, version)//
				.retrieve();
	}

	@Override
	public List<IAuditedEntity> getAllAuditedEntitiesOfEntity(IObjRef objRef)
	{
		return getByObjRef(q_auditedEntity_noVersion, q_auditedEntity_withVersion, objRef);
	}

	@Override
	public List<IAuditedEntity> getAllAuditedEntitiesOfEntity(Object entity)
	{
		return getByEntity(q_auditedEntity_noVersion, q_auditedEntity_withVersion, entity);
	}

	@Override
	public List<IAuditEntry> getAllAuditEntriesOfEntity(IObjRef objRef)
	{
		return getByObjRef(q_auditEntry_noVersion, q_auditEntry_withVersion, objRef);
	}

	@Override
	public List<IAuditEntry> getAllAuditEntriesOfEntity(Object entity)
	{
		return getByEntity(q_auditEntry_noVersion, q_auditEntry_withVersion, entity);
	}
}
