package de.osthus.ambeth.audit;

import de.osthus.ambeth.audit.model.IAuditEntry;
import de.osthus.ambeth.audit.model.IAuditedEntity;
import de.osthus.ambeth.audit.model.IAuditedEntityPrimitiveProperty;
import de.osthus.ambeth.audit.model.IAuditedEntityRelationProperty;
import de.osthus.ambeth.audit.model.IAuditedEntityRelationPropertyItem;
import de.osthus.ambeth.audit.model.IAuditedService;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.IEntityFactory;
import de.osthus.ambeth.security.model.ISignature;

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

	@Override
	public IAuditedEntity createAuditedEntity()
	{
		return entityFactory.createEntity(AuditedEntity.class);
	}

	@Override
	public IAuditedEntityPrimitiveProperty createAuditedEntityPrimitiveProperty()
	{
		return entityFactory.createEntity(AuditedEntityPrimitiveProperty.class);
	}

	@Override
	public IAuditedEntityRelationProperty createAuditedEntityRelationProperty()
	{
		return entityFactory.createEntity(AuditedEntityRelationProperty.class);
	}

	@Override
	public IAuditedEntityRelationPropertyItem createAuditedEntityRelationPropertyItem()
	{
		return entityFactory.createEntity(AuditedEntityRelationPropertyItem.class);
	}

	@Override
	public IAuditedService createAuditedService()
	{
		return entityFactory.createEntity(AuditedService.class);
	}

	@Override
	public ISignature createSignature()
	{
		return entityFactory.createEntity(Signature.class);
	}
}
