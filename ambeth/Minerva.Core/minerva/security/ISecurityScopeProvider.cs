using System;
using De.Osthus.Ambeth.Model;

namespace De.Osthus.Minerva.Security
{
    public interface ISecurityScopeProvider
    {
        ISecurityScope[] CurrentSecurityScope { get; }
    }
}
