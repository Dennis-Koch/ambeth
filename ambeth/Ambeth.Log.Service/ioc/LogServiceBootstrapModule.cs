using De.Osthus.Ambeth.Ioc.Annotation;
using De.Osthus.Ambeth.Ioc.Factory;

namespace De.Osthus.Ambeth.Ioc
{
    [FrameworkModule]
    public class LogServiceBootstrapModule : IInitializingBootstrapModule
    {        
        public void AfterPropertiesSet(IBeanContextFactory beanContextFactory)
        {
            // Intended blank
        }
    }
}
