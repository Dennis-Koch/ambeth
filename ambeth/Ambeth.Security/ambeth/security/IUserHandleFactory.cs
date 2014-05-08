using System;
using System.Collections.Generic;
using De.Osthus.Ambeth.Model;

namespace De.Osthus.Ambeth.Security
{
    public interface IUserHandleFactory
    {
        IUserHandle CreateUserHandle(String sid, ISecurityScope[] securityScopes);
    }
}
