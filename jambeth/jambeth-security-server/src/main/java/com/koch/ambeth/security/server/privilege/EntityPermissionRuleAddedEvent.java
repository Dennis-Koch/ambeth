package com.koch.ambeth.security.server.privilege;

public class EntityPermissionRuleAddedEvent
{
	private final IEntityPermissionRule<?> entityPermissionRule;

	private final Class<?> entityType;

	public EntityPermissionRuleAddedEvent(IEntityPermissionRule<?> entityPermissionRule, Class<?> entityType)
	{
		this.entityPermissionRule = entityPermissionRule;
		this.entityType = entityType;
	}

	public IEntityPermissionRule<?> getEntityPermissionRule()
	{
		return entityPermissionRule;
	}

	public Class<?> getEntityType()
	{
		return entityType;
	}
}
