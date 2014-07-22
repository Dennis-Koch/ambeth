package de.osthus.ambeth.privilege;

public interface IEntityTypePermissionRuleExtendable
{
	<T> void registerEntityTypePermissionRule(IEntityTypePermissionRule<? super T> entityTypePermissionRule, Class<T> entityType);

	<T> void unregisterEntityTypePermissionRule(IEntityTypePermissionRule<? super T> entityTypePermissionRule, Class<T> entityType);
}