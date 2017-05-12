using System;
using De.Osthus.Ambeth.Util;
using System.Reflection;
using De.Osthus.Ambeth.Ioc.Config;
using De.Osthus.Ambeth.Ioc.Extendable;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Proxy;
using De.Osthus.Ambeth.Config;

namespace De.Osthus.Ambeth.Ioc.Link
{
    public class LinkController : ILinkController, IInitializingBean
    {
        [LogInstance]
        public ILogger Log { private get; set; }

        protected static readonly Object[] emptyArgs = new Object[0];

        public IExtendableRegistry ExtendableRegistry { protected get; set; }

        public IProxyFactory ProxyFactory { protected get; set; }

        public IProperties Props { protected get; set; }

        public void AfterPropertiesSet()
        {
            ParamChecker.AssertNotNull(ExtendableRegistry, "ExtendableRegistry");
            ParamChecker.AssertNotNull(Props, "Props");
            ParamChecker.AssertNotNull(ProxyFactory, "ProxyFactory");
        }

        protected Object[] createArgumentArray(Object[] args)
        {
            Object[] realArguments = new Object[args.Length + 1];
            Array.Copy(args, 0, realArguments, 1, args.Length);
            return realArguments;
        }

        public ILinkRegistryNeededRuntime Link(IServiceContext serviceContext, String listenerBeanName)
        {
            return Link(serviceContext, listenerBeanName, (String)null);
        }

        public ILinkRegistryNeededRuntime Link(IServiceContext serviceContext, String listenerBeanName, String methodName)
        {
            LinkRuntime<LinkContainer> linkRuntime = new LinkRuntime<LinkContainer>((ServiceContext)serviceContext, typeof(LinkContainer));
            linkRuntime.Listener(listenerBeanName);
            if (methodName != null)
            {
                linkRuntime.ListenerMethod(methodName);
            }
            return linkRuntime;
        }

        public ILinkRegistryNeededRuntime Link(IServiceContext serviceContext, IBeanConfiguration listenerBean)
        {
            return Link(serviceContext, listenerBean, (String)null);
        }

        public ILinkRegistryNeededRuntime Link(IServiceContext serviceContext, IBeanConfiguration listenerBean, String methodName)
        {
            LinkRuntime<LinkContainer> linkRuntime = new LinkRuntime<LinkContainer>((ServiceContext)serviceContext, typeof(LinkContainer));
            linkRuntime.Listener(listenerBean);
            if (methodName != null)
            {
                linkRuntime.ListenerMethod(methodName);
            }
            return linkRuntime;
        }

        public ILinkRegistryNeededRuntime Link(IServiceContext serviceContext, Object listener, String methodName)
        {
            if (listener is String)
            {
                return Link(serviceContext, (String)listener);
            }
            else if (listener is IBeanConfiguration)
            {
                return Link(serviceContext, (IBeanConfiguration)listener);
            }
            LinkRuntime<LinkContainer> linkRuntime = new LinkRuntime<LinkContainer>((ServiceContext)serviceContext, typeof(LinkContainer));
            linkRuntime.Listener(listener);
            if (methodName != null)
            {
                linkRuntime.ListenerMethod(methodName);
            }
            return linkRuntime;
        }


        public ILinkRegistryNeededRuntime<D> Link<D>(IServiceContext serviceContext, D listener)
        {
            LinkRuntime<D> linkRuntime = new LinkRuntime<D>((ServiceContext)serviceContext, typeof(LinkContainer));
            linkRuntime.Listener(listener);
            return linkRuntime;
        }


        public LinkConfiguration<Object> CreateLinkConfiguration(String listenerBeanName, String methodName)
        {
            LinkConfiguration<Object> linkConfiguration = new LinkConfiguration<Object>(typeof(LinkContainer), ProxyFactory, Props);
            linkConfiguration.PropertyValue(LinkContainer.PROPERTY_LISTENER_NAME, listenerBeanName);
            if (methodName != null)
            {
                linkConfiguration.PropertyValue(LinkContainer.PROPERTY_LISTENER_METHOD_NAME, methodName);
            }
            return linkConfiguration;
        }


        public LinkConfiguration<Object> CreateLinkConfiguration(IBeanConfiguration listenerBean, String methodName)
        {
            LinkConfiguration<Object> linkConfiguration = new LinkConfiguration<Object>(typeof(LinkContainer), ProxyFactory, Props);
            linkConfiguration.PropertyValue(LinkContainer.PROPERTY_LISTENER_BEAN, listenerBean);
            if (methodName != null)
            {
                linkConfiguration.PropertyValue(LinkContainer.PROPERTY_LISTENER_METHOD_NAME, methodName);
            }
            return linkConfiguration;
        }


        public LinkConfiguration<Object> CreateLinkConfiguration(Object listener, String methodName)
        {
            if (listener is String)
            {
                return CreateLinkConfiguration((String)listener, methodName);
            }
            else if (listener is IBeanConfiguration)
            {
                return CreateLinkConfiguration((IBeanConfiguration)listener, methodName);
            }
            // else if (listener is Delegate)
            // {
            // throw new Exception("Illegal state: Delegate can not have an additional methodName");
            // }
            LinkConfiguration<Object> linkConfiguration = new LinkConfiguration<Object>(typeof(LinkContainer), ProxyFactory, Props);
            linkConfiguration.PropertyValue(LinkContainer.PROPERTY_LISTENER, listener);
            if (methodName != null)
            {
                linkConfiguration.PropertyValue(LinkContainer.PROPERTY_LISTENER_METHOD_NAME, methodName);
            }
            return linkConfiguration;
        }


        public LinkConfiguration<D> CreateLinkConfiguration<D>(D listener)
        {
            LinkConfiguration<D> linkConfiguration = new LinkConfiguration<D>(typeof(LinkContainer), ProxyFactory, Props);
            linkConfiguration.PropertyValue(LinkContainer.PROPERTY_LISTENER, listener);
            return linkConfiguration;
        }

        [Obsolete]
        protected AbstractLinkContainerOld createLinkContainer(Type registryType, Object[] arguments)
        {
            Object[] linkArguments;
            MethodInfo[] methods = ExtendableRegistry.GetAddRemoveMethods(registryType, arguments, out linkArguments);
            LinkContainerOld linkContainer = new LinkContainerOld();
            linkContainer.AddMethod = methods[0];
            linkContainer.RemoveMethod = methods[1];
            linkContainer.Arguments = linkArguments;
            return linkContainer;
        }

        [Obsolete]
        protected AbstractLinkContainerOld createLinkContainer(Type registryType, String methodName)
        {
            Object[] linkArguments;
            MethodInfo[] methods = ExtendableRegistry.GetAddRemoveMethods(registryType, null, out linkArguments);
            LinkContainerOld linkContainer = new LinkContainerOld();
            linkContainer.AddMethod = methods[0];
            linkContainer.RemoveMethod = methods[1];
            linkContainer.Arguments = linkArguments;
            if (methodName != null)
            {
                linkContainer.Listener = new LateDelegate(methods[0].GetParameters()[0].ParameterType, methodName);
            }
            return linkContainer;
        }

        [Obsolete]
        protected AbstractLinkContainerOld createLinkContainer(String eventName, String methodName)
        {
            PropertyChangedLinkContainerOld linkContainer = new PropertyChangedLinkContainerOld();
            linkContainer.PropertyName = eventName;
            linkContainer.Arguments = new Object[1];
            if (methodName != null)
            {
                linkContainer.MethodName = methodName;
                //linkContainer.Listener = new LateDelegate(null, methodName);
            }
            return linkContainer;
        }

        [Obsolete]
        protected BeanConfiguration createLinkContainerConfiguration(Type registryType, Object[] arguments)
        {
            Object[] linkArguments;
            MethodInfo[] methods = ExtendableRegistry.GetAddRemoveMethods(registryType, arguments, out linkArguments);

            BeanConfiguration beanConfiguration = new BeanConfiguration(typeof(LinkContainerOld), null, ProxyFactory, Props);
            beanConfiguration.PropertyValue("AddMethod", methods[0]);
            beanConfiguration.PropertyValue("RemoveMethod", methods[1]);
            beanConfiguration.PropertyValue("Arguments", linkArguments);
            return beanConfiguration;
        }

        [Obsolete]
        protected BeanConfiguration createLinkContainerConfiguration(Type registryType, String methodName)
        {
            Object[] linkArguments;
            MethodInfo[] methods = ExtendableRegistry.GetAddRemoveMethods(registryType, null, out linkArguments);

            BeanConfiguration beanConfiguration = new BeanConfiguration(typeof(LinkContainerOld), null, ProxyFactory, Props);
            beanConfiguration.PropertyValue("AddMethod", methods[0]);
            beanConfiguration.PropertyValue("RemoveMethod", methods[1]);
            beanConfiguration.PropertyValue("Arguments", linkArguments);
            if (methodName != null)
            {
                beanConfiguration.PropertyValue("Listener", new LateDelegate(methods[0].GetParameters()[0].ParameterType, methodName));
            }
            return beanConfiguration;
        }

        [Obsolete]
        protected BeanConfiguration createLinkContainerConfiguration(String eventName)
        {
            BeanConfiguration beanConfiguration = new BeanConfiguration(typeof(PropertyChangedLinkContainerOld), null, ProxyFactory, Props);
            beanConfiguration.PropertyValue("PropertyName", eventName);
            beanConfiguration.PropertyValue("Arguments", new Object[1]);
            return beanConfiguration;
        }

        [Obsolete]
        public void Link<R>(IServiceContext serviceContext, String registryBeanName, String listenerBeanName, Object[] arguments)
        {
            Link(serviceContext, registryBeanName, listenerBeanName, typeof(R), arguments);
        }

        [Obsolete]
        public void Link(IServiceContext serviceContext, String registryBeanName, String listenerBeanName, Type registryClass, Object[] arguments)
        {
            ParamChecker.AssertParamNotNull(serviceContext, "serviceContext");
            ParamChecker.AssertParamNotNull(registryBeanName, "registryBeanName");
            ParamChecker.AssertParamNotNull(listenerBeanName, "listenerBeanName");
            ParamChecker.AssertParamNotNull(registryClass, "registryClass");
            ParamChecker.AssertParamNotNull(arguments, "arguments");

            AbstractLinkContainerOld linkContainer = createLinkContainer(registryClass, arguments);
            linkContainer.RegistryBeanName = registryBeanName;
            linkContainer.ListenerBeanName = listenerBeanName;
            linkContainer.BeanContext = serviceContext;

            serviceContext.RegisterWithLifecycle(linkContainer).Finish();
        }

        [Obsolete]
        public void Link<R>(IServiceContext serviceContext, String registryBeanName, String listenerBeanName)
        {
            Link(serviceContext, registryBeanName, listenerBeanName, typeof(R), emptyArgs);
        }

        [Obsolete]
        public void Link(IServiceContext serviceContext, String registryBeanName, String listenerBeanName, Type registryClass)
        {
            Link(serviceContext, registryBeanName, listenerBeanName, registryClass, emptyArgs);
        }

        [Obsolete]
        public void Link<R>(IServiceContext serviceContext, IBeanConfiguration listenerBean)
        {
            Link(serviceContext, listenerBean, typeof(R), emptyArgs);
        }

        [Obsolete]
        public void Link(IServiceContext serviceContext, IBeanConfiguration listenerBean, Type autowiredRegistryClass)
        {
            Link(serviceContext, listenerBean, autowiredRegistryClass, emptyArgs);
        }

        [Obsolete]
        public void Link<R>(IServiceContext serviceContext, IBeanConfiguration listenerBean, Object[] arguments)
        {
            Link(serviceContext, listenerBean, typeof(R), arguments);
        }

        [Obsolete]
        public void Link(IServiceContext serviceContext, IBeanConfiguration listenerBean, Type autowiredRegistryClass, Object[] arguments)
        {
            ParamChecker.AssertParamNotNull(serviceContext, "serviceContext");
            ParamChecker.AssertParamNotNull(listenerBean, "listenerBean");
            ParamChecker.AssertParamNotNull(autowiredRegistryClass, "autowiredRegistryClass");
            ParamChecker.AssertParamNotNull(arguments, "arguments");

            AbstractLinkContainerOld linkContainer = createLinkContainer(autowiredRegistryClass, arguments);
            linkContainer.RegistryBeanAutowiredType = autowiredRegistryClass;
            linkContainer.ListenerBean = listenerBean;
            linkContainer.BeanContext = serviceContext;

            serviceContext.RegisterWithLifecycle(linkContainer).Finish();
        }

        [Obsolete]
        public void Link<R>(IServiceContext serviceContext, String listenerBeanName)
        {
            Link(serviceContext, listenerBeanName, typeof(R), emptyArgs);
        }

        [Obsolete]
        public void Link(IServiceContext serviceContext, String listenerBeanName, Type autowiredRegistryClass)
        {
            Link(serviceContext, listenerBeanName, autowiredRegistryClass, emptyArgs);
        }

        [Obsolete]
        public void Link<R>(IServiceContext serviceContext, String listenerBeanName, Object[] arguments)
        {
            Link(serviceContext, listenerBeanName, typeof(R), arguments);
        }

        [Obsolete]
        public void Link(IServiceContext serviceContext, String listenerBeanName, Type autowiredRegistryClass, Object[] arguments)
        {
            ParamChecker.AssertParamNotNull(serviceContext, "serviceContext");
            ParamChecker.AssertParamNotNull(listenerBeanName, "listenerBeanName");
            ParamChecker.AssertParamNotNull(autowiredRegistryClass, "autowiredRegistryClass");
            ParamChecker.AssertParamNotNull(arguments, "arguments");

            AbstractLinkContainerOld linkContainer = createLinkContainer(autowiredRegistryClass, arguments);
            linkContainer.RegistryBeanAutowiredType = autowiredRegistryClass;
            linkContainer.ListenerBeanName = listenerBeanName;
            linkContainer.BeanContext = serviceContext;

            serviceContext.RegisterWithLifecycle(linkContainer).Finish();
        }

        [Obsolete]
        public IBeanConfiguration createLinkConfiguration(String registryBeanName, String listenerBeanName, Type registryClass)
        {
            return createLinkConfiguration(registryBeanName, listenerBeanName, registryClass, emptyArgs);
        }

        [Obsolete]
        public IBeanConfiguration createLinkConfiguration(String registryBeanName, String listenerBeanName, Type registryClass, Object[] arguments)
        {
            ParamChecker.AssertParamNotNull(registryBeanName, "registryBeanName");
            ParamChecker.AssertParamNotNull(listenerBeanName, "listenerBeanName");
            ParamChecker.AssertParamNotNull(registryClass, "registryClass");
            ParamChecker.AssertParamNotNull(arguments, "arguments");

            BeanConfiguration linkContainer = createLinkContainerConfiguration(registryClass, arguments);
            linkContainer.PropertyValue("RegistryBeanName", registryBeanName);
            linkContainer.PropertyValue("ListenerBeanName", listenerBeanName);
            return linkContainer;
        }

        [Obsolete]
        public IBeanConfiguration createLinkConfiguration(String listenerBeanName, Type autowiredRegistryClass)
        {
            return createLinkConfiguration(listenerBeanName, autowiredRegistryClass, emptyArgs);
        }

        [Obsolete]
        public IBeanConfiguration createLinkConfiguration(String listenerBeanName, Type autowiredRegistryClass, Object[] arguments)
        {
            ParamChecker.AssertParamNotNull(listenerBeanName, "listenerBeanName");
            ParamChecker.AssertParamNotNull(autowiredRegistryClass, "autowiredRegistryClass");
            ParamChecker.AssertParamNotNull(arguments, "arguments");

            BeanConfiguration linkContainer = createLinkContainerConfiguration(autowiredRegistryClass, arguments);
            linkContainer.PropertyValue("RegistryBeanAutowiredType", autowiredRegistryClass);
            linkContainer.PropertyValue("ListenerBeanName", listenerBeanName);
            return linkContainer;
        }

        [Obsolete]
        public IBeanConfiguration createEventLinkConfiguration(String eventProviderBeanName, Type eventInterface, String listenerBeanName, String methodName)
        {
            ParamChecker.AssertParamNotNull(eventProviderBeanName, "eventProviderBeanName");
            ParamChecker.AssertParamNotNull(eventInterface, "eventInterface");
            ParamChecker.AssertParamNotNull(listenerBeanName, "listenerBeanName");
            ParamChecker.AssertParamNotNull(methodName, "methodName");

            BeanConfiguration linkContainer = createLinkContainerConfiguration(eventInterface, methodName);
            linkContainer.PropertyValue("RegistryBeanName", eventProviderBeanName);
            linkContainer.PropertyValue("ListenerBeanName", listenerBeanName);
            return linkContainer;
        }

        [Obsolete]
        public IBeanConfiguration createEventLinkConfiguration(String eventProviderBeanName, Type eventInterface, String handlerDelegateBeanName)
        {
            ParamChecker.AssertParamNotNull(eventProviderBeanName, "eventProviderBeanName");
            ParamChecker.AssertParamNotNull(eventInterface, "eventInterface");
            ParamChecker.AssertParamNotNull(handlerDelegateBeanName, "handlerDelegateBeanName");

            BeanConfiguration linkContainer = createLinkContainerConfiguration(eventInterface, (String)null);
            linkContainer.PropertyValue("RegistryBeanName", eventProviderBeanName);
            linkContainer.PropertyValue("ListenerBeanName", handlerDelegateBeanName);
            return linkContainer;
        }

        [Obsolete]
        public IBeanConfiguration createEventLinkConfiguration(String eventProviderBeanName, Type eventInterface, Delegate handlerDelegate)
        {
            ParamChecker.AssertParamNotNull(eventProviderBeanName, "eventProviderBeanName");
            ParamChecker.AssertParamNotNull(eventInterface, "eventInterface");
            ParamChecker.AssertParamNotNull(handlerDelegate, "handlerDelegate");

            BeanConfiguration linkContainer = createLinkContainerConfiguration(eventInterface, (String)null);
            linkContainer.PropertyValue("RegistryBeanName", eventProviderBeanName);
            linkContainer.PropertyValue("Listener", handlerDelegate);
            return linkContainer;
        }

        [Obsolete]
        public IBeanConfiguration createEventLinkConfiguration<D>(String eventProviderBeanName, IEventDelegate<D> eventName, String listenerBeanName, String methodName)
        {
            ParamChecker.AssertParamNotNull(eventProviderBeanName, "eventProviderBeanName");
            ParamChecker.AssertParamNotNull(eventName, "eventName");
            ParamChecker.AssertParamNotNull(listenerBeanName, "listenerBeanName");
            ParamChecker.AssertParamNotNull(methodName, "methodName");

            BeanConfiguration linkContainer = createLinkContainerConfiguration(eventName.EventName);
            linkContainer.PropertyValue("RegistryBeanName", eventProviderBeanName);
            linkContainer.PropertyValue("ListenerBeanName", listenerBeanName);
            linkContainer.PropertyValue("MethodName", methodName);
            return linkContainer;
        }

        [Obsolete]
        public IBeanConfiguration createEventLinkConfiguration<D>(String eventProviderBeanName, IEventDelegate<D> eventName, String handlerDelegateBeanName)
        {
            ParamChecker.AssertParamNotNull(eventProviderBeanName, "eventProviderBeanName");
            ParamChecker.AssertParamNotNull(eventName, "eventName");
            ParamChecker.AssertParamNotNull(handlerDelegateBeanName, "handlerDelegateBeanName");

            BeanConfiguration linkContainer = createLinkContainerConfiguration(eventName.EventName);
            linkContainer.PropertyValue("RegistryBeanName", eventProviderBeanName);
            linkContainer.PropertyValue("ListenerBeanName", handlerDelegateBeanName);
            return linkContainer;
        }

        [Obsolete]
        public IBeanConfiguration createEventLinkConfiguration<D>(String eventProviderBeanName, IEventDelegate<D> eventName, D handlerDelegate)
        {
            ParamChecker.AssertParamNotNull(eventProviderBeanName, "eventProviderBeanName");
            ParamChecker.AssertParamNotNull(eventName, "eventName");
            ParamChecker.AssertParamNotNull(handlerDelegate, "handlerDelegate");

            BeanConfiguration linkContainer = createLinkContainerConfiguration(eventName.EventName);
            linkContainer.PropertyValue("RegistryBeanName", eventProviderBeanName);
            linkContainer.PropertyValue("Listener", handlerDelegate);
            return linkContainer;
        }

        [Obsolete]
        public void LinkToEvent<D>(IServiceContext serviceContext, String eventProviderBeanName, IEventDelegate<D> eventName, String listenerBeanName, String methodName)
        {
            ParamChecker.AssertParamNotNull(serviceContext, "serviceContext");
            ParamChecker.AssertParamNotNull(eventProviderBeanName, "eventProviderBeanName");
            ParamChecker.AssertParamNotNull(eventName, "eventName");
            ParamChecker.AssertParamNotNull(listenerBeanName, "listenerBeanName");
            ParamChecker.AssertParamNotNull(methodName, "methodName");

            AbstractLinkContainerOld linkContainer = createLinkContainer(eventName.EventName, methodName);
            linkContainer.RegistryBeanName = eventProviderBeanName;
            linkContainer.ListenerBeanName = listenerBeanName;

            serviceContext.RegisterWithLifecycle(linkContainer).Finish();
        }

        [Obsolete]
        public void LinkToEvent<D>(IServiceContext serviceContext, String eventProviderBeanName, IEventDelegate<D> eventName, String handlerDelegateBeanName)
        {
            ParamChecker.AssertParamNotNull(serviceContext, "serviceContext");
            ParamChecker.AssertParamNotNull(eventProviderBeanName, "eventProviderBeanName");
            ParamChecker.AssertParamNotNull(eventName, "eventName");
            ParamChecker.AssertParamNotNull(handlerDelegateBeanName, "handlerDelegateBeanName");

            AbstractLinkContainerOld linkContainer = createLinkContainer(eventName.EventName, (String)null);
            linkContainer.RegistryBeanName = eventProviderBeanName;
            linkContainer.ListenerBeanName = handlerDelegateBeanName;

            serviceContext.RegisterWithLifecycle(linkContainer).Finish();
        }

        [Obsolete]
        public void LinkToEvent<D>(IServiceContext serviceContext, String eventProviderBeanName, IEventDelegate<D> eventName, D handlerDelegate)
        {
            ParamChecker.AssertParamNotNull(serviceContext, "serviceContext");
            ParamChecker.AssertParamNotNull(eventProviderBeanName, "eventProviderBeanName");
            ParamChecker.AssertParamNotNull(eventName, "eventName");
            ParamChecker.AssertParamNotNull(handlerDelegate, "handlerDelegate");

            AbstractLinkContainerOld linkContainer = createLinkContainer(eventName.EventName, (String)null);
            linkContainer.RegistryBeanName = eventProviderBeanName;
            linkContainer.Listener = handlerDelegate;

            serviceContext.RegisterWithLifecycle(linkContainer).Finish();
        }

        [Obsolete]
        public void LinkToEvent<R>(IServiceContext serviceContext, String eventProviderBeanName, String listenerBeanName, String methodName)
        {
            ParamChecker.AssertParamNotNull(serviceContext, "serviceContext");
            ParamChecker.AssertParamNotNull(eventProviderBeanName, "eventProviderBeanName");
            ParamChecker.AssertParamNotNull(listenerBeanName, "listenerBeanName");
            ParamChecker.AssertParamNotNull(methodName, "methodName");

            AbstractLinkContainerOld linkContainer = createLinkContainer(typeof(R), methodName);
            linkContainer.RegistryBeanName = eventProviderBeanName;
            linkContainer.ListenerBeanName = listenerBeanName;

            serviceContext.RegisterWithLifecycle(linkContainer).Finish();
        }

        [Obsolete]
        public void LinkToEvent<R>(IServiceContext serviceContext, String eventProviderBeanName, String handlerDelegateBeanName)
        {
            ParamChecker.AssertParamNotNull(serviceContext, "serviceContext");
            ParamChecker.AssertParamNotNull(eventProviderBeanName, "eventProviderBeanName");
            ParamChecker.AssertParamNotNull(handlerDelegateBeanName, "handlerDelegateBeanName");

            AbstractLinkContainerOld linkContainer = createLinkContainer(typeof(R), (String)null);
            linkContainer.RegistryBeanName = eventProviderBeanName;
            linkContainer.ListenerBeanName = handlerDelegateBeanName;

            serviceContext.RegisterWithLifecycle(linkContainer).Finish();
        }

        [Obsolete]
        public void LinkToEvent<R>(IServiceContext serviceContext, String eventProviderBeanName, Delegate handlerDelegate)
        {
            ParamChecker.AssertParamNotNull(serviceContext, "serviceContext");
            ParamChecker.AssertParamNotNull(eventProviderBeanName, "eventProviderBeanName");
            ParamChecker.AssertParamNotNull(handlerDelegate, "handlerDelegate");

            AbstractLinkContainerOld linkContainer = createLinkContainer(typeof(R), (String)null);
            linkContainer.RegistryBeanName = eventProviderBeanName;
            linkContainer.Listener = handlerDelegate;

            serviceContext.RegisterWithLifecycle(linkContainer).Finish();
        }
    }
}