using System;

namespace De.Osthus.Ambeth.Ioc.Link
{
    public interface ILinkRegistryNeededConfiguration<D> : ILinkRegistryNeededConfiguration
    {
        ILinkConfiguration To(String registryBeanName, IEventDelegate<D> eventDelegate);

        ILinkConfiguration To(Object registry, IEventDelegate<D> eventDelegate);
    }

    public interface ILinkRegistryNeededConfiguration
    {
        ILinkConfiguration To<R>(String registryBeanName);

        ILinkConfiguration To(String registryBeanName, Type registryClass);

        ILinkConfiguration To(String registryBeanName, String propertyName);

        ILinkConfiguration To(String registryBeanName, IEventDelegate eventDelegate);

        ILinkConfiguration To<R>();

        ILinkConfiguration To(Type autowiredRegistryClass);

        ILinkConfiguration To<R>(Object registry);
        
	    ILinkConfiguration To(Object registry, Type registryClass);

	    ILinkConfiguration To(Object registry, String propertyName);

        ILinkConfiguration To(Object registry, IEventDelegate eventDelegate);
    }
}