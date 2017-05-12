using De.Osthus.Ambeth.Ioc.Config;
using System;

namespace De.Osthus.Ambeth.Ioc.Link
{

    public interface ILinkController
    {
        [Obsolete]
        IBeanConfiguration createLinkConfiguration(String registryBeanName, String listenerBeanName, Type registryClass);

        [Obsolete]
        IBeanConfiguration createLinkConfiguration(String registryBeanName, String listenerBeanName, Type registryClass, params Object[] arguments);

        [Obsolete]
        IBeanConfiguration createLinkConfiguration(String listenerBeanName, Type autowiredRegistryClass);

        [Obsolete]
        IBeanConfiguration createLinkConfiguration(String listenerBeanName, Type autowiredRegistryClass, params Object[] arguments);

        [Obsolete]
        void Link(IServiceContext serviceContext, String registryBeanName, String listenerBeanName, Type registryClass);

        [Obsolete]
        void Link(IServiceContext serviceContext, String registryBeanName, String listenerBeanName, Type registryClass, params Object[] arguments);

        [Obsolete]
        void Link(IServiceContext serviceContext, IBeanConfiguration listenerBean, Type autowiredRegistryClass);

        [Obsolete]
        void Link(IServiceContext serviceContext, IBeanConfiguration listenerBean, Type autowiredRegistryClass, params Object[] arguments);

        [Obsolete]
        void Link(IServiceContext serviceContext, String listenerBeanName, Type autowiredRegistryClass);

        [Obsolete]
        void Link(IServiceContext serviceContext, String listenerBeanName, Type autowiredRegistryClass, params Object[] arguments);

        [Obsolete]
        void Link<R>(IServiceContext serviceContext, String registryBeanName, String listenerBeanName);

        [Obsolete]
        void Link<R>(IServiceContext serviceContext, String registryBeanName, String listenerBeanName, params Object[] arguments);

        [Obsolete]
        void Link<R>(IServiceContext serviceContext, IBeanConfiguration listenerBean);

        [Obsolete]
        void Link<R>(IServiceContext serviceContext, IBeanConfiguration listenerBean, params Object[] arguments);

        [Obsolete]
        void Link<R>(IServiceContext serviceContext, String listenerBeanName);

        [Obsolete]
        void Link<R>(IServiceContext serviceContext, String listenerBeanName, params Object[] arguments);

        [Obsolete]
        IBeanConfiguration createEventLinkConfiguration(String eventProviderBeanName, Type eventInterface, String listenerBeanName, String methodName);

        [Obsolete]
        IBeanConfiguration createEventLinkConfiguration(String eventProviderBeanName, Type eventInterface, String handlerDelegateBeanName);

        [Obsolete]
        IBeanConfiguration createEventLinkConfiguration(String eventProviderBeanName, Type eventInterface, Delegate handlerDelegate);

        //[Obsolete]
        //IBeanConfiguration createEventLinkConfiguration(Type autowiredEventProviderType, String eventName, String handlerDelegateBeanName);

        [Obsolete]
        IBeanConfiguration createEventLinkConfiguration<D>(String eventProviderBeanName, IEventDelegate<D> eventName, String listenerBeanName, String methodName);

        [Obsolete]
        IBeanConfiguration createEventLinkConfiguration<D>(String eventProviderBeanName, IEventDelegate<D> eventName, String handlerDelegateBeanName);

        [Obsolete]
        IBeanConfiguration createEventLinkConfiguration<D>(String eventProviderBeanName, IEventDelegate<D> eventName, D handlerDelegate);

        //[Obsolete]
        //IBeanConfiguration createEventLinkConfiguration(Type autowiredEventProviderType, String eventName, Delegate handlerDelegate);

        [Obsolete]
        void LinkToEvent<D>(IServiceContext serviceContext, String eventProviderBeanName, IEventDelegate<D> eventName, String listenerBeanName, String methodName);

        [Obsolete]
        void LinkToEvent<D>(IServiceContext serviceContext, String eventProviderBeanName, IEventDelegate<D> eventName, String handlerDelegateBeanName);

        [Obsolete]
        void LinkToEvent<D>(IServiceContext serviceContext, String eventProviderBeanName, IEventDelegate<D> eventName, D handlerDelegate);

        [Obsolete]
        void LinkToEvent<R>(IServiceContext serviceContext, String eventProviderBeanName, String listenerBeanName, String methodName);

        [Obsolete]
        void LinkToEvent<R>(IServiceContext serviceContext, String eventProviderBeanName, String handlerDelegateBeanName);

        [Obsolete]
        void LinkToEvent<R>(IServiceContext serviceContext, String eventProviderBeanName, Delegate handlerDelegate);

        ILinkRegistryNeededRuntime Link(IServiceContext serviceContext, String listenerBeanName);

        ILinkRegistryNeededRuntime Link(IServiceContext serviceContext, String listenerBeanName, String methodName);

        ILinkRegistryNeededRuntime Link(IServiceContext serviceContext, IBeanConfiguration listenerBean);

        ILinkRegistryNeededRuntime Link(IServiceContext serviceContext, IBeanConfiguration listenerBean, String methodName);

        ILinkRegistryNeededRuntime Link(IServiceContext serviceContext, Object listener, String methodName);

        ILinkRegistryNeededRuntime<D> Link<D>(IServiceContext serviceContext, D listener);

        LinkConfiguration<Object> CreateLinkConfiguration(String listenerBeanName, String methodName);

        LinkConfiguration<Object> CreateLinkConfiguration(IBeanConfiguration listenerBean, String methodName);

        LinkConfiguration<Object> CreateLinkConfiguration(Object listener, String methodName);

        LinkConfiguration<D> CreateLinkConfiguration<D>(D listener);
    }
}
