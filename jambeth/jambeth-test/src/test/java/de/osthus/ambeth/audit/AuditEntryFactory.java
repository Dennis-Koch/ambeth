package de.osthus.ambeth.audit;

import de.osthus.ambeth.audit.model.IAuditEntry;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.IEntityFactory;

public class AuditEntryFactory implements IAuditEntryFactory
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IEntityFactory entityFactory;

	@Override
	public IAuditEntry createAuditEntry()
	{
		return entityFactory.createEntity(AuditEntry.class);
	}
}
