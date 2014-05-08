using De.Osthus.Ambeth.Ioc.Factory;
using De.Osthus.Ambeth.Util;

namespace De.Osthus.Ambeth.Ioc
{
    public class BootstrapScannerModule : IInitializingModule
    {
	    public void AfterPropertiesSet(IBeanContextFactory beanContextFactory)
	    {
		    beanContextFactory.RegisterBean<ClasspathScanner>("classpathScanner").Autowireable<IClasspathScanner>();

		    beanContextFactory.RegisterBean<ModuleScanner>("moduleScanner");
	    }
    }
}