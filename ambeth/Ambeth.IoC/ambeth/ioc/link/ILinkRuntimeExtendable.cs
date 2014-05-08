using System;
using De.Osthus.Ambeth.Ioc.Config;

namespace De.Osthus.Ambeth.Ioc.Link
{
    public interface ILinkRuntimeExtendable
    {
        [Obsolete]
        void Link(String listenerBeanName, Type autowiredRegistryClass);

        [Obsolete]
        void Link(String listenerBeanName, Type autowiredRegistryClass, params Object[] arguments);

        [Obsolete]
        void Link(IBeanConfiguration listenerBean, Type autowiredRegistryClass);

        [Obsolete]
        void Link(IBeanConfiguration listenerBean, Type autowiredRegistryClass, params Object[] arguments);

        [Obsolete]
        void Link<R>(String listenerBeanName);

        [Obsolete]
        void Link<R>(String listenerBeanName, params Object[] arguments);

        [Obsolete]
        void Link<R>(IBeanConfiguration listenerBean);

        [Obsolete]
        void Link<R>(IBeanConfiguration listenerBean, params Object[] arguments);

        [Obsolete]
        void LinkToNamed(String registryBeanName, String listenerBeanName, Type registryClass);

        [Obsolete]
        void LinkToNamed(String registryBeanName, String listenerBeanName, Type registryClass, params Object[] arguments);

        [Obsolete]
        void LinkToNamed<R>(String registryBeanName, String listenerBeanName);

        [Obsolete]
        void LinkToNamed<R>(String registryBeanName, String listenerBeanName, params Object[] arguments);

        [Obsolete]
        void LinkToEvent<D>(String eventProviderBeanName, IEventDelegate<D> eventName, String listenerBeanName, String methodName);

        [Obsolete]
        void LinkToEvent<D>(String eventProviderBeanName, IEventDelegate<D> eventName, String handlerDelegateBeanName);

        [Obsolete]
        void LinkToEvent<D>(String eventProviderBeanName, IEventDelegate<D> eventName, D handlerDelegate);

        [Obsolete]
        void LinkToEvent<R>(String eventProviderBeanName, String listenerBeanName, String methodName);

        [Obsolete]
        void LinkToEvent<R>(String eventProviderBeanName, String handlerDelegateBeanName);

        [Obsolete]
        void LinkToEvent<R>(String eventProviderBeanName, Delegate handlerDelegate);

        ILinkRegistryNeededRuntime Link(String listenerBeanName);

        ILinkRegistryNeededRuntime Link(IBeanConfiguration listenerBean);

        ILinkRegistryNeededRuntime<D> Link<D>(D listener);
    }
}