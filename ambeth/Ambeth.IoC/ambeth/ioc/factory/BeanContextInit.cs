using De.Osthus.Ambeth.Config;
using System;
using De.Osthus.Ambeth.Ioc.Config;
using De.Osthus.Ambeth.Collections;

namespace De.Osthus.Ambeth.Ioc.Factory
{

    public class BeanContextInit
    {
        public Properties properties;

        public ServiceContext beanContext;

        public BeanContextFactory beanContextFactory;

        public IdentityLinkedMap<Object, IBeanConfiguration> objectToBeanConfigurationMap;

        public IISet<Object> allLifeCycledBeansSet;

        public System.Collections.Generic.IList<Object> initializedOrdering;
    }
}
