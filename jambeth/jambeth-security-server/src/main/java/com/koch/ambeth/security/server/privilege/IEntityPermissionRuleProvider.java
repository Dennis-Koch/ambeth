package com.koch.ambeth.security.server.privilege;

import com.koch.ambeth.util.collections.ILinkedMap;
import com.koch.ambeth.util.collections.IList;

public interface IEntityPermissionRuleProvider
{
	ILinkedMap<Class<?>, IList<IEntityPermissionRule<?>>> getAllEntityPermissionRules();

	IList<IEntityPermissionRule<?>> getEntityPermissionRules(Class<?> entityType);
}