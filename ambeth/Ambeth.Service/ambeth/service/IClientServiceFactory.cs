using System;
using De.Osthus.Ambeth.Ioc.Factory;

namespace De.Osthus.Ambeth.Service
{
    public interface IClientServiceFactory
    {
        Type GetTargetProviderType(Type clientInterface);

        Type GetSyncInterceptorType(Type clientInterface);

        String GetServiceName(Type clientInterface);

        void PostProcessTargetProviderBean(String targetProviderBeanName, IBeanContextFactory beanContextFactory);
    }
}
