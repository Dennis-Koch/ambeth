using De.Osthus.Ambeth.Ioc.Factory;
using De.Osthus.Ambeth.Threading;

namespace De.Osthus.Ambeth.Ioc.Hierarchy
{
    public interface IContextFactory
    {
        IServiceContext CreateChildContext(IBackgroundWorkerParamDelegate<IBeanContextFactory> registerPhaseDelegate);
    }
}
