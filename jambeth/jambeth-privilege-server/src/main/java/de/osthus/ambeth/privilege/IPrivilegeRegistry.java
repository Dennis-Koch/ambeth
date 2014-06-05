package de.osthus.ambeth.privilege;

public interface IPrivilegeRegistry
{
	IPrivilegeProviderExtension getExtension(Class<?> entityType);
}