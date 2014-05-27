package de.osthus.ambeth.privilege;

public interface IPrivilegeRegistry
{
	IPrivilegeProvider getExtension(Class<?> entityType);
}