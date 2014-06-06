using System;

namespace De.Osthus.Ambeth.Privilege
{
    public interface IPrivilegeProviderExtensionExtendable
    {
	    void RegisterPrivilegeProviderExtension(IPrivilegeProviderExtension privilegeProviderExtension, Type entityType);

	    void UnregisterPrivilegeProviderExtension(IPrivilegeProviderExtension privilegeProviderExtension, Type entityType);
    }
}
