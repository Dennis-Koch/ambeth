package de.osthus.ambeth.privilege;

public interface IEntityTypePermissionRuleExtendable
{
	void registerEntityTypePermissionRule(IEntityTypePermissionRule entityTypePermissionRule, Class<?> entityType);

	void unregisterEntityTypePermissionRule(IEntityTypePermissionRule entityTypePermissionRule, Class<?> entityType);
}