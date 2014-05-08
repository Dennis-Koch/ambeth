using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using De.Osthus.Ambeth.Model;

namespace De.Osthus.Ambeth.Security
{
    public class AmbethUserHandleFactory : IUserHandleFactory
    {
        public IUserHandle CreateUserHandle(String sid, ISecurityScope[] securityScopes)
        {
            return new AmbethUserHandle(sid, securityScopes, null);
        }
    }
}
