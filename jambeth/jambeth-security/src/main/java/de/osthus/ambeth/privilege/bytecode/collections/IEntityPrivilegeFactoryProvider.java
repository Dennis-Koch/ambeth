package de.osthus.ambeth.privilege.bytecode.collections;

public interface IEntityPrivilegeFactoryProvider
{
	IEntityPrivilegeFactory getEntityPrivilegeFactory(Class<?> entityType, boolean create, boolean read, boolean update, boolean delete, boolean execute);
}
