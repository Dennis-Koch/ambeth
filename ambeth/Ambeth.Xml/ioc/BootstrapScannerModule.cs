using De.Osthus.Ambeth.Ioc.Factory;
using De.Osthus.Ambeth.Util;

namespace De.Osthus.Ambeth.Ioc
{
    public class BootstrapScannerModule : IInitializingModule
    {
	    public void AfterPropertiesSet(IBeanContextFactory beanContextFactory)
	    {
            beanContextFactory.RegisterAnonymousBean<ClasspathScanner>().Autowireable<IClasspathScanner>();

		    beanContextFactory.RegisterAnonymousBean<ModuleScanner>();
	    }
    }
}