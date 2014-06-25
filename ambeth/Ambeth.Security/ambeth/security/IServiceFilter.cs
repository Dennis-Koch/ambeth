using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Reflection;
using De.Osthus.Ambeth.Model;

namespace De.Osthus.Ambeth.Security
{
    public interface IServiceFilter
    {
        CallPermission CheckCallPermissionOnService(MethodInfo serviceMethod, Object[] arguments, IAuthorization authorization, ISecurityScope[] securityScopes);
    }
}
