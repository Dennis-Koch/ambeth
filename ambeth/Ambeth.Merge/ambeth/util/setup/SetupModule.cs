using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Ioc.Factory;
using De.Osthus.Ambeth.Ioc.Config;

namespace De.Osthus.Ambeth.Util.Setup
{
    public class SetupModule : IInitializingModule
    {
	    [LogInstance]
        public ILogger Log { private get; set; }

        public void AfterPropertiesSet(IBeanContextFactory beanContextFactory)
        {
            beanContextFactory.RegisterAnonymousBean<DataSetup>().Autowireable(typeof(IDataSetup), typeof(IDatasetBuilderExtensionExtendable));
        }

	    public static void RegisterDataSetBuilder(IBeanContextFactory beanContextFactory, Type type)
	    {
		    IBeanConfiguration builder = beanContextFactory.RegisterAnonymousBean(type).Autowireable(type);
		    beanContextFactory.Link(builder).To(typeof(IDatasetBuilderExtensionExtendable));
	    }
    }
}
