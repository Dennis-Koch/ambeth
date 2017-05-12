using De.Osthus.Ambeth.Ioc.Factory;
using De.Osthus.Ambeth.Progress;

namespace De.Osthus.Ambeth.Ioc
{
    public class ProgressBootstrapModule : IInitializingBootstrapModule
    {
        public void AfterPropertiesSet(IBeanContextFactory beanContextFactory)
        {
            beanContextFactory.RegisterBean<ProgressDispatcherTL>("progressDispatcher").Autowireable<IProgressDispatcher>();
        }
    }
}
