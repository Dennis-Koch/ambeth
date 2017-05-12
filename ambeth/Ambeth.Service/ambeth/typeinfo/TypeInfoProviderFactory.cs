using De.Osthus.Ambeth.Ioc;
using System;
using De.Osthus.Ambeth.Util;

namespace De.Osthus.Ambeth.Typeinfo
{
    public class TypeInfoProviderFactory : ITypeInfoProviderFactory, IInitializingBean
    {
        public IServiceContext ServiceContext { protected get; set; }

	    public Type TypeInfoProviderType { protected get; set; }

	    public virtual void AfterPropertiesSet()
	    {
		    ParamChecker.AssertNotNull(ServiceContext, "ServiceContext");
		    ParamChecker.AssertNotNull(TypeInfoProviderType, "TypeInfoProviderType");
	    }

        public virtual ITypeInfoProvider CreateTypeInfoProvider()
	    {
            return ServiceContext.RegisterBean<ITypeInfoProvider>(TypeInfoProviderType).PropertyValue("Synchronized", false).Finish();
	    }
    }
}