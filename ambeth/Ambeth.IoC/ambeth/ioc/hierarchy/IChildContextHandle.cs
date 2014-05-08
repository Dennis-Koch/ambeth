using System;
using De.Osthus.Ambeth.Ioc.Link;
using System.Collections.Generic;

namespace De.Osthus.Ambeth.Ioc.Hierarchy
{
    public interface IContextHandle
    {
        IServiceContext Start();

        IServiceContext Start(IDictionary<String, Object> namedBeans);

        IServiceContext Start(RegisterPhaseDelegate registerPhaseDelegate);

        void Stop();
    }
}
