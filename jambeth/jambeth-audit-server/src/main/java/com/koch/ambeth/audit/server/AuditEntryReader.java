package com.koch.ambeth.audit.server;

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

import java.util.Date;
import java.util.List;

import com.koch.ambeth.audit.model.IAuditEntry;
import com.koch.ambeth.audit.model.IAuditedEntity;
import com.koch.ambeth.audit.model.IAuditedEntityRef;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.query.IOperand;
import com.koch.ambeth.query.IQuery;
import com.koch.ambeth.query.IQueryBuilder;
import com.koch.ambeth.query.IQueryBuilderFactory;
import com.koch.ambeth.query.OrderByType;
import com.koch.ambeth.security.model.IUser;
import com.koch.ambeth.security.server.IUserIdentifierProvider;
import com.koch.ambeth.service.merge.IEntityMetaDataProvider;
import com.koch.ambeth.service.merge.model.IEntityMetaData;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.service.metadata.PrimitiveMember;
import com.koch.ambeth.util.collections.IList;

public class AuditEntryReader implements IAuditEntryReader {
	private static final String VALUE_NAME_START = "auditEntryStart";
	private static final String VALUE_NAME_END = "auditEntryEnd";

	@Autowired
	protected IEntityMetaDataProvider entityMetaDataProvider;

	@Autowired
	protected IUserIdentifierProvider userIdentifierProvider;

	@Autowired
	protected IQueryBuilderFactory queryBuilderFactory;

	private IQuery<IAuditedEntity> q_auditedEntity_withVersion, q_auditedEntity_noVersion;

	private IQuery<IAuditEntry> q_auditEntry_withVersion, q_auditEntry_noVersion,
			q_auditEntry_entityType, q_auditEntry_entityType_InTimeSlot;

	private IQuery<IAuditEntry> q_allUserActions;

	protected IQuery<IAuditedEntity> getQuery_AuditedEntity_WithVersion() {
		if (q_auditedEntity_withVersion != null) {
			return q_auditedEntity_withVersion;
		}
		IQueryBuilder<IAuditedEntity> qb = queryBuilderFactory.create(IAuditedEntity.class);

		IOperand entityType = qb.property(IAuditedEntity.Ref + "." + IAuditedEntityRef.EntityType);
		IOperand entityId = qb.property(IAuditedEntity.Ref + "." + IAuditedEntityRef.EntityId);
		IOperand entityVersion = qb
				.property(IAuditedEntity.Ref + "." + IAuditedEntityRef.EntityVersion);

		qb.orderBy(qb.property(IAuditedEntity.Entry + "." + IAuditEntry.Timestamp), OrderByType.ASC);

		q_auditedEntity_withVersion = qb.build(qb.and(
				//
				qb.isEqualTo(entityType, qb.valueName(IAuditedEntityRef.EntityType)), //
				qb.isEqualTo(entityId, qb.valueName(IAuditedEntityRef.EntityId)),
				qb.isLessThanOrEqualTo(entityVersion, qb.valueName(IAuditedEntityRef.EntityVersion))));
		return q_auditedEntity_withVersion;
	}

	protected IQuery<IAuditedEntity> getQuery_AuditedEntity_NoVersion() {
		if (q_auditedEntity_noVersion != null) {
			return q_auditedEntity_noVersion;
		}
		IQueryBuilder<IAuditedEntity> qb = queryBuilderFactory.create(IAuditedEntity.class);
		IOperand entityType = qb.property(IAuditedEntity.Ref + "." + IAuditedEntityRef.EntityType);
		IOperand entityId = qb.property(IAuditedEntity.Ref + "." + IAuditedEntityRef.EntityId);

		qb.orderBy(qb.property(IAuditedEntity.Entry + "." + IAuditEntry.Timestamp), OrderByType.ASC);

		q_auditedEntity_noVersion = qb.build(qb.and(//
				qb.isEqualTo(entityType, qb.valueName(IAuditedEntityRef.EntityType)), //
				qb.isEqualTo(entityId, qb.valueName(IAuditedEntityRef.EntityId))));
		return q_auditedEntity_noVersion;
	}

	protected IQuery<IAuditEntry> getQuery_AuditEntry_WithVersion() {
		if (q_auditEntry_withVersion != null) {
			return q_auditEntry_withVersion;
		}
		IQueryBuilder<IAuditEntry> qb = queryBuilderFactory.create(IAuditEntry.class);

		IOperand entityType = qb.property(
				IAuditEntry.Entities + "." + IAuditedEntity.Ref + "." + IAuditedEntityRef.EntityType);
		IOperand entityId = qb.property(
				IAuditEntry.Entities + "." + IAuditedEntity.Ref + "." + IAuditedEntityRef.EntityId);
		IOperand entityVersion = qb.property(
				IAuditEntry.Entities + "." + IAuditedEntity.Ref + "." + IAuditedEntityRef.EntityVersion);

		qb.orderBy(qb.property(IAuditEntry.Timestamp), OrderByType.ASC);

		q_auditEntry_withVersion = qb.build(qb.and(
				//
				qb.isEqualTo(entityType, qb.valueName(IAuditedEntityRef.EntityType)), //
				qb.isEqualTo(entityId, qb.valueName(IAuditedEntityRef.EntityId)),
				qb.isLessThanOrEqualTo(entityVersion, qb.valueName(IAuditedEntityRef.EntityVersion))));
		return q_auditEntry_withVersion;
	}

	protected IQuery<IAuditEntry> getQuery_AuditEntry_NoVersion() {
		if (q_auditEntry_noVersion != null) {
			return q_auditEntry_noVersion;
		}
		IQueryBuilder<IAuditEntry> qb = queryBuilderFactory.create(IAuditEntry.class);
		IOperand entityType = qb.property(
				IAuditEntry.Entities + "." + IAuditedEntity.Ref + "." + IAuditedEntityRef.EntityType);
		IOperand entityId = qb.property(
				IAuditEntry.Entities + "." + IAuditedEntity.Ref + "." + IAuditedEntityRef.EntityId);

		qb.orderBy(qb.property(IAuditEntry.Timestamp), OrderByType.ASC);

		q_auditEntry_noVersion = qb.build(qb.and(//
				qb.isEqualTo(entityType, qb.valueName(IAuditedEntityRef.EntityType)), //
				qb.isEqualTo(entityId, qb.valueName(IAuditedEntityRef.EntityId))));
		return q_auditEntry_noVersion;
	}

	protected IQuery<IAuditEntry> getQuery_AllUserActions() {
		if (q_allUserActions != null) {
			return q_allUserActions;
		}
		IQueryBuilder<IAuditEntry> qb = queryBuilderFactory.create(IAuditEntry.class);
		IOperand userIdentifier = qb.property(IAuditEntry.UserIdentifier);

		qb.orderBy(qb.property(IAuditEntry.Timestamp), OrderByType.ASC);

		q_allUserActions = qb.build(qb.and(//
				qb.isEqualTo(userIdentifier, qb.valueName(IAuditEntry.UserIdentifier))));
		return q_allUserActions;
	}

	protected IQuery<IAuditEntry> getQuery_AuditEntry_EntityType() {
		if (q_auditEntry_entityType != null) {
			return q_auditEntry_entityType;
		}
		IQueryBuilder<IAuditEntry> qb = queryBuilderFactory.create(IAuditEntry.class);
		IOperand entityType = qb.property(
				IAuditEntry.Entities + "." + IAuditedEntity.Ref + "." + IAuditedEntityRef.EntityType);

		qb.orderBy(qb.property(IAuditEntry.Timestamp), OrderByType.ASC);

		q_auditEntry_entityType = qb.build(//
				qb.isEqualTo(entityType, qb.valueName(IAuditedEntityRef.EntityType)));//
		return q_auditEntry_entityType;
	}

	protected IQuery<IAuditEntry> getQuery_AuditEntry_EntityType_InTimeSlot() {
		if (q_auditEntry_entityType_InTimeSlot != null) {
			return q_auditEntry_entityType_InTimeSlot;
		}
		IQueryBuilder<IAuditEntry> qb = queryBuilderFactory.create(IAuditEntry.class);
		IOperand entityType = qb.property(
				IAuditEntry.Entities + "." + IAuditedEntity.Ref + "." + IAuditedEntityRef.EntityType);
		IOperand auditEntryStart = qb.property(IAuditEntry.Timestamp);
		IOperand auditEntryEnd = qb.property(IAuditEntry.Timestamp);

		qb.orderBy(qb.property(IAuditEntry.Timestamp), OrderByType.ASC);

		q_auditEntry_entityType_InTimeSlot = qb.build(qb.and(
				//
				qb.isEqualTo(entityType, qb.valueName(IAuditedEntityRef.EntityType)),
				qb.isGreaterThanOrEqualTo(auditEntryStart, qb.valueName(VALUE_NAME_START)),
				qb.isLessThanOrEqualTo(auditEntryEnd, qb.valueName(VALUE_NAME_END))));
		return q_auditEntry_entityType_InTimeSlot;
	}

	protected <V> IList<V> getByObjRef(IQuery<V> q_NoVersion, IQuery<V> q_WithVersion,
			IObjRef objRef) {
		IEntityMetaData metaData = entityMetaDataProvider.getMetaData(objRef.getRealType());

		if (objRef.getVersion() == null) {
			return q_NoVersion.param(IAuditedEntityRef.EntityType, metaData.getEntityType())//
					.param(IAuditedEntityRef.EntityId, objRef.getId())//
					.retrieve();
		}
		return q_WithVersion.param(IAuditedEntityRef.EntityType, metaData.getEntityType())//
				.param(IAuditedEntityRef.EntityId, objRef.getId())//
				.param(IAuditedEntityRef.EntityVersion, objRef.getVersion())//
				.retrieve();
	}

	protected <V> IList<V> getByEntityType(IQuery<V> q_entityType, Class<?> entityType) {
		IEntityMetaData metaData = entityMetaDataProvider.getMetaData(entityType);

		return q_entityType.param(IAuditedEntityRef.EntityType, metaData.getEntityType())//
				.retrieve();
	}

	protected <V> List<V> get(IQuery<V> q_NoVersion, IQuery<V> q_WithVersion, Object entity) {
		if (entity instanceof IObjRef) {
			return getByObjRef(q_NoVersion, q_WithVersion, (IObjRef) entity);
		}
		IEntityMetaData metaData = entityMetaDataProvider.getMetaData(entity.getClass());

		PrimitiveMember versionMember = metaData.getVersionMember();
		Object version = null;
		if (versionMember != null) {
			version = versionMember.getValue(entity);
		}
		Object id = metaData.getIdMember().getValue(entity);
		if (version == null) {
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
	public List<IAuditedEntity> getAllAuditedEntitiesOfEntity(Object entity) {
		return get(getQuery_AuditedEntity_NoVersion(), getQuery_AuditedEntity_WithVersion(), entity);
	}

	@Override
	public List<IAuditEntry> getAllAuditEntriesOfEntity(Object entity) {
		return get(getQuery_AuditEntry_NoVersion(), getQuery_AuditEntry_WithVersion(), entity);
	}

	@Override
	public List<IAuditEntry> getAllAuditEntriesOfUser(IUser user) {
		return getQuery_AllUserActions()
				.param(IAuditEntry.UserIdentifier, userIdentifierProvider.getSID(user)).retrieve();
	}

	@Override
	public List<IAuditEntry> getAllAuditEntriesOfEntityType(Class<?> entityType) {
		return getByEntityType(getQuery_AuditEntry_EntityType(), entityType);
	}

	@Override
	public List<IAuditEntry> getAllAuditEntriesOfEntityTypeInTimeSlot(Class<?> entityType, Date start,
			Date end) {
		return getQuery_AuditEntry_EntityType_InTimeSlot().param(VALUE_NAME_START, start)//
				.param(VALUE_NAME_END, end).param(IAuditedEntityRef.EntityType, entityType)//
				.retrieve();
	}

}
