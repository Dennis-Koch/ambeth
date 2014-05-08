using De.Osthus.Ambeth.Ioc.Factory;
using System;
using System.Collections.Generic;
using De.Osthus.Ambeth.Ioc.Config;

namespace De.Osthus.Ambeth.Ioc
{
    public interface IBeanPostProcessor
    {
        Object PostProcessBean(IBeanContextFactory beanContextFactory, IServiceContext beanContext, IBeanConfiguration beanConfiguration, Type beanType, Object targetBean, ISet<Type> requestedTypes);
    }
}
