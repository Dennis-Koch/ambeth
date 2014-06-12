package de.osthus.ambeth.privilege;

public interface IPrivilegeProviderExtensionExtendable
{
	<T> void registerPrivilegeProviderExtension(IPrivilegeProviderExtension<? super T> privilegeProviderExtension, Class<T> entityType);

	<T> void unregisterPrivilegeProviderExtension(IPrivilegeProviderExtension<? super T> privilegeProviderExtension, Class<T> entityType);
}