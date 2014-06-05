package de.osthus.ambeth.privilege;

public interface IPrivilegeProviderExtensionExtendable
{
	void registerPrivilegeProviderExtension(IPrivilegeProviderExtension privilegeProviderExtension, Class<?> entityType);

	void unregisterPrivilegeProviderExtension(IPrivilegeProviderExtension privilegeProviderExtension, Class<?> entityType);
}