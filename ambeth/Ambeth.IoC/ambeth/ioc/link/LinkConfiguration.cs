using System;
using De.Osthus.Ambeth.Ioc.Config;
using De.Osthus.Ambeth.Proxy;
using De.Osthus.Ambeth.Config;

namespace De.Osthus.Ambeth.Ioc.Link
{
    public class LinkConfiguration<D> : BeanConfiguration, ILinkRegistryNeededConfiguration<D>, ILinkConfigWithOptional, ILinkConfigOptional
    {
        public LinkConfiguration(Type beanType, IProxyFactory proxyFactory, IProperties props)
            : base(beanType, null, proxyFactory, props)
        {
            // Intended blank
        }

        public ILinkConfigOptional With(params Object[] arguments)
        {
            PropertyValue(LinkContainer.PROPERTY_ARGUMENTS, arguments);
            return this;
        }

        public void Optional()
        {
            PropertyValue(LinkContainer.PROPERTY_OPTIONAL, true);
        }

        public ILinkConfigWithOptional To(String registryBeanName, Type registryClass)
        {
            PropertyValue(LinkContainer.PROPERTY_REGISTRY_NAME, registryBeanName);
            PropertyValue(LinkContainer.PROPERTY_REGISTRY_TYPE, registryClass);
            return this;
        }

        public ILinkConfigWithOptional To(String registryBeanName, String propertyName)
        {
            PropertyValue(LinkContainer.PROPERTY_REGISTRY_NAME, registryBeanName);
            PropertyValue(LinkContainer.PROPERTY_REGISTRY_PROPERTY_NAME, propertyName);
            return this;
        }

        public ILinkConfigWithOptional To(String registryBeanName, IEventDelegate<D> eventDelegate)
        {
            return To(registryBeanName, eventDelegate.EventName);
        }

        public ILinkConfigWithOptional To(Object registry, IEventDelegate<D> eventDelegate)
        {
            return To(registry, eventDelegate.EventName);
        }

        public ILinkConfigWithOptional To(String registryBeanName, IEventDelegate eventDelegate)
        {
            return To(registryBeanName, eventDelegate.EventName);
        }

        public ILinkConfigWithOptional To(Type autowiredRegistryClass)
        {
            PropertyValue(LinkContainer.PROPERTY_REGISTRY_TYPE, autowiredRegistryClass);
            return this;
        }

        public ILinkConfigWithOptional To<T>()
        {
            return To(typeof(T));
        }

        public ILinkConfigWithOptional To<T>(String registryBeanName)
        {
            return To(registryBeanName, typeof(T));
        }

        public ILinkConfigWithOptional To<T>(Object registry)
        {
            return To(registry, typeof(T));
        }

        public ILinkConfigWithOptional To(Object registry, Type registryClass)
        {
            PropertyValue(AbstractLinkContainer.PROPERTY_REGISTRY, registry);
            PropertyValue(AbstractLinkContainer.PROPERTY_REGISTRY_TYPE, registryClass);
            return this;
        }

        public ILinkConfigWithOptional To(Object registry, String propertyName)
        {
            PropertyValue(AbstractLinkContainer.PROPERTY_REGISTRY, registry);
            PropertyValue(AbstractLinkContainer.PROPERTY_REGISTRY_PROPERTY_NAME, propertyName);
            return this;
        }

        public ILinkConfigWithOptional To(Object registry, IEventDelegate eventDelegate)
        {
            return To(registry, eventDelegate.EventName);
        }
    }
}
