package de.osthus.ambeth.audit;

import java.util.Date;
import java.util.List;

import de.osthus.ambeth.audit.model.IAuditEntry;
import de.osthus.ambeth.audit.model.IAuditedEntity;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.security.model.IUser;

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