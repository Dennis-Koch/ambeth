package de.osthus.ambeth.privilege.factory;

public interface IEntityPrivilegeFactoryProvider
{
	IEntityPrivilegeFactory getEntityPrivilegeFactory(Class<?> entityType, boolean create, boolean read, boolean update, boolean delete, boolean execute);
}
