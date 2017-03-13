package com.koch.ambeth.audit.server;

import com.koch.ambeth.audit.model.IAuditEntry;
import com.koch.ambeth.merge.incremental.IIncrementalMergeState;
import com.koch.ambeth.merge.model.CreateOrUpdateContainerBuild;
import com.koch.ambeth.merge.transfer.DirectObjRef;
import com.koch.ambeth.service.merge.IEntityMetaDataProvider;
import com.koch.ambeth.util.collections.ArrayList;

public class AuditControllerState
{
	public final IIncrementalMergeState incrementalMergeState;

	public final IEntityMetaDataProvider entityMetaDataProvider;

	public final ArrayList<CreateOrUpdateContainerBuild> auditedChanges = new ArrayList<CreateOrUpdateContainerBuild>();

	public AuditControllerState(IIncrementalMergeState incrementalMergeState, IEntityMetaDataProvider entityMetaDataProvider)
	{
		this.incrementalMergeState = incrementalMergeState;
		this.entityMetaDataProvider = entityMetaDataProvider;
	}

	public CreateOrUpdateContainerBuild getAuditEntry()
	{
		if (auditedChanges.size() == 0)
		{
			return createEntity(IAuditEntry.class);
		}
		return auditedChanges.get(0);
	}

	public CreateOrUpdateContainerBuild createEntity(Class<?> entityType)
	{
		CreateOrUpdateContainerBuild entity = incrementalMergeState.newCreateContainer(entityType);
		entity.setReference(new DirectObjRef(entityMetaDataProvider.getMetaData(entityType).getEntityType(), entity));
		auditedChanges.add(entity);
		return entity;
	}
}