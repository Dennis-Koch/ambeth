using De.Osthus.Ambeth.Ioc.Config;
using De.Osthus.Ambeth.Proxy;
using System;

namespace De.Osthus.Ambeth.Ioc.Link
{
    public class LinkRuntime<D> : BeanRuntime<ILinkContainer>, ILinkRegistryNeededRuntime<D>, ILinkRuntimeWithOptional, ILinkRuntimeOptional, ILinkRuntimeFinish
    {
        public LinkRuntime(ServiceContext serviceContext, Type beanType) : base(serviceContext, beanType, true)
        {
            // Intended blank
        }

        protected override BeanConfiguration CreateBeanConfiguration(Type beanType)
        {
            IProxyFactory proxyFactory = serviceContext.GetService<IProxyFactory>(false);
            return new LinkConfiguration<Object>(beanType, proxyFactory, null);
        }

        public ILinkRuntimeOptional With(params Object[] arguments)
        {
            PropertyValue(AbstractLinkContainer.PROPERTY_ARGUMENTS, arguments);
            return this;
        }

        public ILinkRuntimeFinish Optional()
        {
            PropertyValue(AbstractLinkContainer.PROPERTY_OPTIONAL, "true");
            return this;
        }

        public void FinishLink()
        {
            Finish();
        }

        public ILinkRegistryNeededRuntime Listener(IBeanConfiguration listenerBean)
        {
            PropertyValue(AbstractLinkContainer.PROPERTY_LISTENER_BEAN, listenerBean);
            return this;
        }

        public ILinkRegistryNeededRuntime Listener(String listenerBeanName)
        {
            PropertyValue(AbstractLinkContainer.PROPERTY_LISTENER_NAME, listenerBeanName);
            return this;
        }

        public ILinkRegistryNeededRuntime Listener(Object listener)
        {
            PropertyValue(AbstractLinkContainer.PROPERTY_LISTENER, listener);
            return this;
        }

        public ILinkRegistryNeededRuntime ListenerMethod(String methodName)
        {
            PropertyValue(AbstractLinkContainer.PROPERTY_LISTENER_METHOD_NAME, methodName);
            return this;
        }

        public ILinkRuntimeWithOptional To<R>(String registryBeanName)
        {
            return To(registryBeanName, typeof(R));
        }

        public ILinkRuntimeWithOptional To(String registryBeanName, Type registryClass)
        {
            PropertyValue(AbstractLinkContainer.PROPERTY_REGISTRY_NAME, registryBeanName);
            PropertyValue(AbstractLinkContainer.PROPERTY_REGISTRY_TYPE, registryClass);
            return this;
        }

        public ILinkRuntimeWithOptional To(String registryBeanName, String propertyName)
        {
            PropertyValue(AbstractLinkContainer.PROPERTY_REGISTRY_NAME, registryBeanName);
            PropertyValue(AbstractLinkContainer.PROPERTY_REGISTRY_PROPERTY_NAME, propertyName);
            return this;
        }

        public ILinkRuntimeWithOptional To(String registryBeanName, IEventDelegate<D> eventDelegate)
        {
            return To(registryBeanName, eventDelegate.EventName);
        }

        public ILinkRuntimeWithOptional To<R>()
        {
            return To(typeof(R));
        }

        public ILinkRuntimeWithOptional To(Type autowiredRegistryClass)
        {
            PropertyValue(AbstractLinkContainer.PROPERTY_REGISTRY_TYPE, autowiredRegistryClass);
            return this;
        }

        public ILinkRuntimeWithOptional To<T>(Object registry)
        {
            return To(registry, typeof(T));
        }

        public ILinkRuntimeWithOptional To(Object registry, String propertyName)
        {
            PropertyValue(AbstractLinkContainer.PROPERTY_REGISTRY, registry);
            PropertyValue(AbstractLinkContainer.PROPERTY_REGISTRY_PROPERTY_NAME, propertyName);
            return this;
        }

        public ILinkRuntimeWithOptional To(Object registry, Type registryClass)
        {
            PropertyValue(AbstractLinkContainer.PROPERTY_REGISTRY, registry);
            PropertyValue(AbstractLinkContainer.PROPERTY_REGISTRY_TYPE, registryClass);
            return this;
        }

        public ILinkRuntimeWithOptional To(Object registry, IEventDelegate<D> eventDelegate)
        {
            return To(registry, eventDelegate.EventName);
        }
    }
}