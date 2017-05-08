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

import com.koch.ambeth.audit.model.IAuditEntry;
import com.koch.ambeth.merge.incremental.IIncrementalMergeState;
import com.koch.ambeth.merge.model.CreateOrUpdateContainerBuild;
import com.koch.ambeth.merge.transfer.DirectObjRef;
import com.koch.ambeth.service.merge.IEntityMetaDataProvider;
import com.koch.ambeth.util.collections.ArrayList;

public class AuditControllerState {
	public final IIncrementalMergeState incrementalMergeState;

	public final IEntityMetaDataProvider entityMetaDataProvider;

	public final ArrayList<CreateOrUpdateContainerBuild> auditedChanges =
			new ArrayList<>();

	public AuditControllerState(IIncrementalMergeState incrementalMergeState,
			IEntityMetaDataProvider entityMetaDataProvider) {
		this.incrementalMergeState = incrementalMergeState;
		this.entityMetaDataProvider = entityMetaDataProvider;
	}

	public CreateOrUpdateContainerBuild getAuditEntry() {
		if (auditedChanges.size() == 0) {
			return createEntity(IAuditEntry.class);
		}
		return auditedChanges.get(0);
	}

	public CreateOrUpdateContainerBuild createEntity(Class<?> entityType) {
		CreateOrUpdateContainerBuild entity = incrementalMergeState.newCreateContainer(entityType);
		entity.setReference(
				new DirectObjRef(entityMetaDataProvider.getMetaData(entityType).getEntityType(), entity));
		auditedChanges.add(entity);
		return entity;
	}
}
