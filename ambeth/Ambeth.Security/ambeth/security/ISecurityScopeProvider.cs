using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using De.Osthus.Ambeth.Model;

namespace De.Osthus.Ambeth.Security
{
    public interface ISecurityScopeProvider
    {
        ISecurityScope[] SecurityScopes { get; set; }

        IUserHandle UserHandle { get; set; }
    }
}
