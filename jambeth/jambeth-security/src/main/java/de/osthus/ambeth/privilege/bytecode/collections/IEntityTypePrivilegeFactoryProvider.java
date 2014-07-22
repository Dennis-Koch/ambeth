package de.osthus.ambeth.privilege.bytecode.collections;

public interface IEntityTypePrivilegeFactoryProvider
{
	IEntityTypePrivilegeFactory getEntityTypePrivilegeFactory(Class<?> entityType, Boolean create, Boolean read, Boolean update, Boolean delete, Boolean execute);
}
