package de.osthus.ambeth.privilege;

import de.osthus.ambeth.collections.ILinkedMap;
import de.osthus.ambeth.collections.IList;

public interface IEntityPermissionRuleProvider
{
	ILinkedMap<Class<?>, IList<IEntityPermissionRule<?>>> getAllEntityPermissionRules();

	IList<IEntityPermissionRule<?>> getEntityPermissionRules(Class<?> entityType);
}