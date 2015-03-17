package de.osthus.ambeth.audit;

import de.osthus.ambeth.audit.model.IAuditEntry;
import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.merge.IEntityMetaDataProvider;
import de.osthus.ambeth.merge.incremental.IIncrementalMergeState;
import de.osthus.ambeth.merge.model.CreateOrUpdateContainerBuild;
import de.osthus.ambeth.merge.transfer.DirectObjRef;

public class AuditControllerState
{
	public final IIncrementalMergeState incrementalMergeState;

	public final IEntityMetaDataProvider entityMetaDataProvider;

	public IAuditEntry auditEntry;

	public final ArrayList<CreateOrUpdateContainerBuild> auditedChanges = new ArrayList<CreateOrUpdateContainerBuild>();

	public AuditControllerState(IIncrementalMergeState incrementalMergeState, IEntityMetaDataProvider entityMetaDataProvider)
	{
		this.incrementalMergeState = incrementalMergeState;
		this.entityMetaDataProvider = entityMetaDataProvider;
	}

	public CreateOrUpdateContainerBuild createEntity(Class<?> entityType)
	{
		CreateOrUpdateContainerBuild entity = incrementalMergeState.newCreateContainer(entityType);
		entity.setReference(new DirectObjRef(entityMetaDataProvider.getMetaData(entityType).getEntityType(), entity));
		auditedChanges.add(entity);
		return entity;
	}
}