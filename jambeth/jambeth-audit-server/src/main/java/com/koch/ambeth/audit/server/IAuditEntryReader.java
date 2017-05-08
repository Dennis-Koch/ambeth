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
import com.koch.ambeth.security.model.IUser;
import com.koch.ambeth.service.merge.model.IObjRef;

public interface IAuditEntryReader {
	List<IAuditedEntity> getAllAuditedEntitiesOfEntity(Object entity);

	List<IAuditedEntity> getAllAuditedEntitiesOfEntity(IObjRef objRef);

	List<IAuditEntry> getAllAuditEntriesOfEntity(Object entity);

	List<IAuditEntry> getAllAuditEntriesOfEntity(IObjRef objRef);

	List<IAuditEntry> getAllAuditEntriesOfUser(IUser user);

	List<IAuditEntry> getAllAuditEntriesOfEntityType(Class<?> entityType);

	List<IAuditEntry> getAllAuditEntriesOfEntityTypeInTimeSlot(Class<?> entityType, Date start,
			Date end);
}
