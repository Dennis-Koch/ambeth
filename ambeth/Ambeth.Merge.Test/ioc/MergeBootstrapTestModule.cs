using De.Osthus.Ambeth.Ioc.Factory;

namespace De.Osthus.Ambeth.Ioc
{
    public class MergeBootstrapTestModule : IInitializingBootstrapModule
    {
        public virtual void AfterPropertiesSet(IBeanContextFactory beanContextFactory)
        {
            // Intended blank
        }
    }
}
