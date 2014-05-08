using System;
using System.Collections.Generic;

namespace De.Osthus.Ambeth.Ioc.Hierarchy
{
    public interface IContextFactory
    {
        IServiceContext CreateChildContext(RegisterPhaseDelegate registerPhaseDelegate);
    }
}
