using System;
using System.Collections.Generic;
using De.Osthus.Ambeth.Ioc.Config;

namespace De.Osthus.Ambeth.Ioc.Factory
{
    public interface IBeanContextInitializer
    {
        void InitializeBeanContext(ServiceContext beanContext, BeanContextFactory beanContextFactory);

        Object InitializeBean(ServiceContext beanContext, BeanContextFactory beanContextFactory, IBeanConfiguration beanConfiguration, Object bean,
                IList<IBeanConfiguration> beanConfHierarchy, bool joinLifecycle);

        IList<IBeanConfiguration> FillParentHierarchyIfValid(ServiceContext beanContext, BeanContextFactory beanContextFactory,
                IBeanConfiguration beanConfiguration);

        Type ResolveTypeInHierarchy(IList<IBeanConfiguration> beanConfigurations);
    }
}
