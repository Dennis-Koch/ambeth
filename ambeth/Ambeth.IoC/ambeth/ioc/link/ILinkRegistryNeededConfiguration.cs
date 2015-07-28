using System;

namespace De.Osthus.Ambeth.Ioc.Link
{
    public interface ILinkRegistryNeededConfiguration<D> : ILinkRegistryNeededConfiguration
    {
        ILinkConfigWithOptional To(String registryBeanName, IEventDelegate<D> eventDelegate);

        ILinkConfigWithOptional To(Object registry, IEventDelegate<D> eventDelegate);
    }

    public interface ILinkRegistryNeededConfiguration
    {
        ILinkConfigWithOptional To<R>(String registryBeanName);

        ILinkConfigWithOptional To(String registryBeanName, Type registryClass);

        ILinkConfigWithOptional To(String registryBeanName, String propertyName);

        ILinkConfigWithOptional To(String registryBeanName, IEventDelegate eventDelegate);

        ILinkConfigWithOptional To<R>();

        ILinkConfigWithOptional To(Type autowiredRegistryClass);

        ILinkConfigWithOptional To<R>(Object registry);

        ILinkConfigWithOptional To(Object registry, Type registryClass);

        ILinkConfigWithOptional To(Object registry, String propertyName);

        ILinkConfigWithOptional To(Object registry, IEventDelegate eventDelegate);
    }
}