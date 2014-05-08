package de.osthus.ambeth.privilege;

public interface IPrivilegeProviderExtendable
{
	void registerPrivilegeProvider(IPrivilegeProvider privilegeProvider, Class<?> entityType);

	void unregisterPrivilegeProvider(IPrivilegeProvider privilegeProvider, Class<?> entityType);
}