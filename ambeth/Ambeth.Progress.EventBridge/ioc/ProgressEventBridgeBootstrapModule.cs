using De.Osthus.Ambeth.Datachange;
using De.Osthus.Ambeth.Ioc.Factory;
using De.Osthus.Ambeth.Progress;

namespace De.Osthus.Ambeth.Ioc
{
    public class ProgressEventBridgeBootstrapModule : IInitializingBootstrapModule
    {
        public void AfterPropertiesSet(IBeanContextFactory beanContextFactory)
        {
            beanContextFactory.RegisterBean<ProgressToEventDispatcher>("progressToEventDispatcher").Autowireable<IProgressListener>();
        }
    }
}
