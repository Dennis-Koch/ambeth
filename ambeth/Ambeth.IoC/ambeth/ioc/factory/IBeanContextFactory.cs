using De.Osthus.Ambeth.Ioc.Config;
using De.Osthus.Ambeth.Ioc.Link;
using System;

namespace De.Osthus.Ambeth.Ioc.Factory
{
    public interface IBeanContextFactory : ILinkExtendable, IAnonymousBeanRegistry
    {
        void RegisterAlias(String aliasBeanName, String beanNameToCreateAliasFor);

        IBeanConfiguration RegisterBean(String beanName, String parentBeanName);

        IBeanConfiguration RegisterBean<T>(String beanName);

        IBeanConfiguration RegisterBean(String beanName, Type beanType);

        IBeanConfiguration RegisterAutowireableBean<I, T>() where T : I;
        
        IBeanConfiguration RegisterAutowireableBean(Type beanType, Type typeToPublish);

        IBeanConfiguration RegisterWithLifecycle(String beanName, Object obj);

        IBeanConfiguration RegisterExternalBean(String beanName, Object externalBean);
    }
}
