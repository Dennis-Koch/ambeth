using De.Osthus.Ambeth.Ioc.Factory;
using De.Osthus.Ambeth.Threading;
using System;
using System.Collections.Generic;

namespace De.Osthus.Ambeth.Ioc.Hierarchy
{
    public interface IContextHandle
    {
        IServiceContext Start();

        IServiceContext Start(IDictionary<String, Object> namedBeans);

        IServiceContext Start(IBackgroundWorkerParamDelegate<IBeanContextFactory> registerPhaseDelegate);

        void Stop();
    }
}
