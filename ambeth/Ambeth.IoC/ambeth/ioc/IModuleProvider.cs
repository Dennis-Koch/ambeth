using System;

namespace De.Osthus.Ambeth.Ioc
{
    public interface IModuleProvider
    {
	    Type[] GetFrameworkModules();

	    Type[] GetBootstrapModules();
    }
}