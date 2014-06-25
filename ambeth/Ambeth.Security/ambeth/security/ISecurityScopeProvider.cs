using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using De.Osthus.Ambeth.Model;
using De.Osthus.Ambeth.Threading;

namespace De.Osthus.Ambeth.Security
{
    public interface ISecurityScopeProvider
    {
        ISecurityScope[] SecurityScopes { get; set; }

        IAuthorization Authorization { get; set; }

        R ExecuteWithSecurityScopes<R>(IResultingBackgroundWorkerDelegate<R> runnable, params ISecurityScope[] securityScopes);
    }
}
