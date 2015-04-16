package de.osthus.ambeth.audit;

import de.osthus.ambeth.audit.model.IAuditEntry;
import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.merge.IEntityMetaDataProvider;
import de.osthus.ambeth.merge.incremental.IIncrementalMergeState;
import de.osthus.ambeth.merge.model.CreateOrUpdateContainerBuild;
import de.osthus.ambeth.merge.transfer.DirectObjRef;
import de.osthus.ambeth.security.model.ISignature;

public class AuditControllerState
{
	public final IIncrementalMergeState incrementalMergeState;

	public final IEntityMetaDataProvider entityMetaDataProvider;

	public final ArrayList<CreateOrUpdateContainerBuild> auditedChanges = new ArrayList<CreateOrUpdateContainerBuild>();

	private ISignature signatureOfUser;

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

	public ISignature getSignatureOfUser()
	{
		return signatureOfUser;
	}

	public void setSignatureOfUser(ISignature signatureOfUser)
	{
		this.signatureOfUser = signatureOfUser;
	}
}