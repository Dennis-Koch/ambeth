package com.koch.ambeth.security.server.privilege;

public interface IEntityTypePermissionRuleExtendable
{
	void registerEntityTypePermissionRule(IEntityTypePermissionRule entityTypePermissionRule, Class<?> entityType);

	void unregisterEntityTypePermissionRule(IEntityTypePermissionRule entityTypePermissionRule, Class<?> entityType);
}