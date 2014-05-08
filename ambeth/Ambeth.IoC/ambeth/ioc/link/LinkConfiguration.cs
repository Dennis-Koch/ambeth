using System;
using De.Osthus.Ambeth.Ioc.Config;
using De.Osthus.Ambeth.Proxy;
using De.Osthus.Ambeth.Config;

namespace De.Osthus.Ambeth.Ioc.Link
{
    public class LinkConfiguration<D> : BeanConfiguration, ILinkConfiguration, ILinkRegistryNeededConfiguration<D>
    {
        protected bool f_optional;

        public LinkConfiguration(Type beanType, IProxyFactory proxyFactory, IProperties props)
            : base(beanType, null, proxyFactory, props)
        {
            // Intended blank
        }

        public ILinkConfiguration With(params Object[] arguments)
        {
            PropertyValue(LinkContainer.PROPERTY_ARGUMENTS, arguments);
            return this;
        }

        public ILinkConfiguration Optional()
        {
            if (f_optional)
            {
                // Already configured as optional
                return this;
            }
            PropertyValue(LinkContainer.PROPERTY_OPTIONAL, true);
            return this;
        }

        public ILinkConfiguration To(String registryBeanName, Type registryClass)
        {
            PropertyValue(LinkContainer.PROPERTY_REGISTRY_NAME, registryBeanName);
            PropertyValue(LinkContainer.PROPERTY_REGISTRY_TYPE, registryClass);
            return this;
        }

        public ILinkConfiguration To(String registryBeanName, String propertyName)
        {
            PropertyValue(LinkContainer.PROPERTY_REGISTRY_NAME, registryBeanName);
            PropertyValue(LinkContainer.PROPERTY_REGISTRY_PROPERTY_NAME, propertyName);
            return this;
        }

        public ILinkConfiguration To(String registryBeanName, IEventDelegate<D> eventDelegate)
        {
            return To(registryBeanName, eventDelegate.EventName);
        }

        public ILinkConfiguration To(Object registry, IEventDelegate<D> eventDelegate)
        {
            return To(registry, eventDelegate.EventName);
        }

        public ILinkConfiguration To(String registryBeanName, IEventDelegate eventDelegate)
        {
            return To(registryBeanName, eventDelegate.EventName);
        }

        public ILinkConfiguration To(Type autowiredRegistryClass)
        {
            PropertyValue(LinkContainer.PROPERTY_REGISTRY_TYPE, autowiredRegistryClass);
            return this;
        }

        public ILinkConfiguration To<T>()
        {
            return To(typeof(T));
        }
        
        public ILinkConfiguration To<T>(String registryBeanName)
        {
            return To(registryBeanName, typeof(T));
        }
        
        public ILinkConfiguration To<T>(Object registry)
        {
            return To(registry, typeof(T));
        }
        
        public ILinkConfiguration To(Object registry, Type registryClass)
	    {
		    PropertyValue(AbstractLinkContainer.PROPERTY_REGISTRY, registry);
		    PropertyValue(AbstractLinkContainer.PROPERTY_REGISTRY_TYPE, registryClass);
		    return this;
	    }

	    public ILinkConfiguration To(Object registry, String propertyName)
	    {
		    PropertyValue(AbstractLinkContainer.PROPERTY_REGISTRY, registry);
		    PropertyValue(AbstractLinkContainer.PROPERTY_REGISTRY_PROPERTY_NAME, propertyName);
		    return this;
	    }

        public ILinkConfiguration To(Object registry, IEventDelegate eventDelegate)
        {
            return To(registry, eventDelegate.EventName);
        }
    }
}
