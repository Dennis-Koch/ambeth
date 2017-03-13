package com.koch.ambeth.security.server.privilege;

import com.koch.ambeth.util.collections.IList;

public interface IEntityTypePermissionRuleProvider
{
	IList<IEntityTypePermissionRule> getEntityTypePermissionRules(Class<?> entityType);
}