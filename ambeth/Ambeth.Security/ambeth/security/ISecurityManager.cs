using System;
using System.Collections.Generic;
using System.Reflection;

namespace De.Osthus.Ambeth.Security
{
    public interface ISecurityManager
    {
        void CheckMethodAccess(MethodInfo method, Object[] arguments, IUserHandle userHandle);

        T FilterValue<T>(T value);
    }
}
