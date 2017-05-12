using System;
using De.Osthus.Ambeth.Ioc.Config;

namespace De.Osthus.Ambeth.Ioc.Link
{
    public interface ILinkRegistryNeededRuntime<D> : ILinkRegistryNeededRuntime
    {
        ILinkRuntimeWithOptional To(String registryBeanName, IEventDelegate<D> eventDelegate);

        ILinkRuntimeWithOptional To(Object registry, IEventDelegate<D> eventDelegate);
    }

    public interface ILinkRegistryNeededRuntime
    {
        ILinkRuntimeWithOptional To<R>(String registryBeanName);

        ILinkRuntimeWithOptional To(String registryBeanName, Type registryClass);

        ILinkRuntimeWithOptional To(String registryBeanName, String propertyName);

        ILinkRuntimeWithOptional To<R>();

        ILinkRuntimeWithOptional To(Type autowiredRegistryClass);

        ILinkRuntimeWithOptional To<R>(Object registry);

        ILinkRuntimeWithOptional To(Object registry, Type registryClass);

        ILinkRuntimeWithOptional To(Object registry, String propertyName);
    }
}