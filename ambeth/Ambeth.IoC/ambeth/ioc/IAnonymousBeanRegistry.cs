using System;
using De.Osthus.Ambeth.Ioc.Config;

namespace De.Osthus.Ambeth.Ioc
{
    public interface IAnonymousBeanRegistry
    {
        IBeanConfiguration RegisterWithLifecycle(Object obj);

        void RegisterDisposable(IDisposable disposable);

        void RegisterDisposable(IDisposableBean disposableBean);

        IBeanConfiguration RegisterExternalBean(Object externalBean);

        IBeanConfiguration RegisterAnonymousBean<T>();

        IBeanConfiguration RegisterAnonymousBean(Type beanType);

        IBeanConfiguration RegisterBean<T>();

        IBeanConfiguration RegisterBean(Type beanType);
    }
}
