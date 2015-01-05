package de.osthus.ambeth.privilege;

import de.osthus.ambeth.collections.IList;

public interface IEntityTypePermissionRuleProvider
{
	IList<IEntityTypePermissionRule> getEntityTypePermissionRules(Class<?> entityType);
}