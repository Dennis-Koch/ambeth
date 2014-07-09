package de.osthus.ambeth.privilege;

public interface IEntityPermissionRuleExtendable
{
	<T> void registerEntityPermissionRule(IEntityPermissionRule<? super T> entityPermissionRule, Class<T> entityType);

	<T> void unregisterEntityPermissionRule(IEntityPermissionRule<? super T> entityPermissionRule, Class<T> entityType);
}