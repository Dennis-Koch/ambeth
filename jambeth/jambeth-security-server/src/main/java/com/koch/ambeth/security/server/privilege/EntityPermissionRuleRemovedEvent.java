package com.koch.ambeth.security.server.privilege;


public class EntityPermissionRuleRemovedEvent
{
	private final IEntityPermissionRule<?> entityPermissionRule;

	private final Class<?> entityType;

	public EntityPermissionRuleRemovedEvent(IEntityPermissionRule<?> entityPermissionRule, Class<?> entityType)
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
