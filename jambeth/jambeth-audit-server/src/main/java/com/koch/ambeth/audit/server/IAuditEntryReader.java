package com.koch.ambeth.audit.server;

import java.util.Date;
import java.util.List;

import com.koch.ambeth.audit.model.IAuditEntry;
import com.koch.ambeth.audit.model.IAuditedEntity;
import com.koch.ambeth.security.model.IUser;
import com.koch.ambeth.service.merge.model.IObjRef;

public interface IAuditEntryReader
{
	List<IAuditedEntity> getAllAuditedEntitiesOfEntity(Object entity);

	List<IAuditedEntity> getAllAuditedEntitiesOfEntity(IObjRef objRef);

	List<IAuditEntry> getAllAuditEntriesOfEntity(Object entity);

	List<IAuditEntry> getAllAuditEntriesOfEntity(IObjRef objRef);

	List<IAuditEntry> getAllAuditEntriesOfUser(IUser user);

	List<IAuditEntry> getAllAuditEntriesOfEntityType(Class<?> entityType);

	List<IAuditEntry> getAllAuditEntriesOfEntityTypeInTimeSlot(Class<?> entityType, Date start, Date end);
}