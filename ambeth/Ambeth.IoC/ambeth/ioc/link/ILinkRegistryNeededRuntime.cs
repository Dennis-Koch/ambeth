using System;
using De.Osthus.Ambeth.Ioc.Config;

namespace De.Osthus.Ambeth.Ioc.Link
{
    public interface ILinkRegistryNeededRuntime<D> : ILinkRegistryNeededRuntime
    {
        ILinkRuntime To(String registryBeanName, IEventDelegate<D> eventDelegate);

        ILinkRuntime To(Object registry, IEventDelegate<D> eventDelegate);
    }

    public interface ILinkRegistryNeededRuntime
    {
        ILinkRuntime To<R>(String registryBeanName);

        ILinkRuntime To(String registryBeanName, Type registryClass);

        ILinkRuntime To(String registryBeanName, String propertyName);

        ILinkRuntime To<R>();

        ILinkRuntime To(Type autowiredRegistryClass);

        ILinkRuntime To<R>(Object registry);

        ILinkRuntime To(Object registry, Type registryClass);

	    ILinkRuntime To(Object registry, String propertyName);
    }
}