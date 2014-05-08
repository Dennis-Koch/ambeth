using System;
using System.Collections.Generic;
using De.Osthus.Ambeth.Event;
using De.Osthus.Ambeth.Datachange.Model;
using De.Osthus.Ambeth.Ioc.Factory;
using De.Osthus.Ambeth.Ioc.Annotation;

namespace De.Osthus.Ambeth.Ioc
{
    [FrameworkModule]
    public class DataChangeBootstrapModule : IInitializingBootstrapModule
    {
        public void AfterPropertiesSet(IBeanContextFactory beanContextFactory)
        {
            // Intended blank
        }
    }
}
