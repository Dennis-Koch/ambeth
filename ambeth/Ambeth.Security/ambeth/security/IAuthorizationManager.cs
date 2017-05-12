using System;
using System.Collections.Generic;
using De.Osthus.Ambeth.Model;

namespace De.Osthus.Ambeth.Security
{
    public interface IAuthorizationManager
    {
        IAuthorization Authorize(String sid, ISecurityScope[] securityScopes);
    }
}
