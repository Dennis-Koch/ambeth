package de.osthus.ambeth.audit;

import java.util.List;

import de.osthus.ambeth.audit.model.IAuditEntry;
import de.osthus.ambeth.audit.model.IAuditedEntity;
import de.osthus.ambeth.audit.model.IAuditedEntityRef;
import de.osthus.ambeth.ioc.IStartingBean;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.IEntityMetaDataProvider;
import de.osthus.ambeth.merge.model.IEntityMetaData;
import de.osthus.ambeth.merge.model.IObjRef;
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

	private IQuery<IAuditedEntity> q_withVersion, q_noVersion;

	@Override
	public void afterStarted() throws Throwable
	{
		{
			IQueryBuilder<IAuditedEntity> qb = queryBuilderFactory.create(IAuditedEntity.class);

			IOperand entityType = qb.property(IAuditedEntity.Ref + "." + IAuditedEntityRef.EntityType);
			IOperand entityId = qb.property(IAuditedEntity.Ref + "." + IAuditedEntityRef.EntityId);
			IOperand entityVersion = qb.property(IAuditedEntity.Ref + "." + IAuditedEntityRef.EntityVersion);

			qb.orderBy(qb.property(IAuditedEntity.Entry + "." + IAuditEntry.Timestamp), OrderByType.ASC);

			q_withVersion = qb.build(qb.and(
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

			q_noVersion = qb.build(qb.and(//
					qb.isEqualTo(entityType, qb.valueName(IAuditedEntityRef.EntityType)),//
					qb.isEqualTo(entityId, qb.valueName(IAuditedEntityRef.EntityId))));
		}
	}

	@Override
	public List<IAuditedEntity> getAllAuditedEntitiesOfEntity(IObjRef objRef)
	{
		IEntityMetaData metaData = entityMetaDataProvider.getMetaData(objRef.getRealType());

		if (objRef.getVersion() == null)
		{
			return q_noVersion.param(IAuditedEntityRef.EntityType, metaData.getEntityType())//
					.param(IAuditedEntityRef.EntityId, objRef.getId())//
					.retrieve();
		}
		return q_withVersion.param(IAuditedEntityRef.EntityType, metaData.getEntityType())//
				.param(IAuditedEntityRef.EntityId, objRef.getId())//
				.param(IAuditedEntityRef.EntityVersion, objRef.getVersion())//
				.retrieve();
	}
}
