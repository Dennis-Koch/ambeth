using De.Osthus.Ambeth.Config;
using System;
using De.Osthus.Ambeth.Ioc.Config;
using De.Osthus.Ambeth.Collections;
using System.Collections.Generic;

namespace De.Osthus.Ambeth.Ioc.Factory
{

    public class BeanContextInit
    {
        public Properties properties;

        public ServiceContext beanContext;

        public BeanContextFactory beanContextFactory;

        public IdentityLinkedMap<Object, IBeanConfiguration> objectToBeanConfigurationMap;

        public IdentityHashMap<Object, IBeanConfiguration> objectToHandledBeanConfigurationMap;

        public IISet<Object> allLifeCycledBeansSet;

        public IList<Object> initializedOrdering;

        public List<IDisposableBean> toDestroyOnError = new List<IDisposableBean>();
    }
}
