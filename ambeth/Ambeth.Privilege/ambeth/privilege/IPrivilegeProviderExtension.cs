using System;
using System.Net;
using De.Osthus.Ambeth.Model;
using System.Collections.Generic;

namespace De.Osthus.Ambeth.Privilege
{
    public interface IPrivilegeProviderExtension
    {
	    void BuildCascadeLoadPathsForEntityType(Type entityType, IList<String> cascadeLoadPaths);

	    bool IsReadEntityAllowed(Object entity, ISecurityScope[] securityScopes);

	    bool IsCreateEntityAllowed(Object entity, ISecurityScope[] securityScopes);

	    bool IsUpdateEntityAllowed(Object entity, ISecurityScope[] securityScopes);

	    bool IsDeleteEntityAllowed(Object entity, ISecurityScope[] securityScopes);
    }
}
