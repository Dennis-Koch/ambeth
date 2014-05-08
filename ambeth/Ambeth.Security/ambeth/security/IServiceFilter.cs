using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Reflection;

namespace De.Osthus.Ambeth.Security
{
    public interface IServiceFilter
    {
        CallPermission CheckCallPermissionOnService(MethodInfo serviceMethod, Object[] arguments, IUserHandle userHandle);
    }
}
