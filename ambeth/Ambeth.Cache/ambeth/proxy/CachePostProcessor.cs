using System;
using System.Collections.Generic;
using De.Osthus.Ambeth.Annotation;
using De.Osthus.Ambeth.Cache.Interceptor;
using De.Osthus.Ambeth.Config;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Ioc.Config;
using De.Osthus.Ambeth.Ioc.Factory;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Service;

namespace De.Osthus.Ambeth.Proxy
{
    public class CachePostProcessor : AbstractCascadePostProcessor
    {
        [LogInstance]
        public ILogger Log { private get; set; }

        public class ServiceAnnotationCache : AnnotationCache<ServiceAttribute>
        {
            protected override bool AnnotationEquals(ServiceAttribute left, ServiceAttribute right)
            {
                return Equals(left.Interface, right.Interface) && Equals(left.Name, right.Name);
            }
        }

        public class ServiceClientAnnotationCache : AnnotationCache<ServiceClientAttribute>
        {
            protected override bool AnnotationEquals(ServiceClientAttribute left, ServiceClientAttribute right)
            {
                return Equals(left.Name, right.Name);
            }
        }

        protected AnnotationCache<ServiceAttribute> serviceAnnotationCache = new ServiceAnnotationCache();

        protected AnnotationCache<ServiceClientAttribute> serviceClientAnnotationCache = new ServiceClientAnnotationCache();

        [Property(ServiceConfigurationConstants.NetworkClientMode, DefaultValue = "false")]
        public bool IsNetworkClientMode { protected get; set; }

        protected override ICascadedInterceptor HandleServiceIntern(IBeanContextFactory beanContextFactory, IServiceContext beanContext, IBeanConfiguration beanConfiguration, Type type, ISet<Type> requestedTypes)
        {
            ServiceAttribute serviceAnnotation = serviceAnnotationCache.GetAnnotation(type);
            if (serviceAnnotation != null)
            {
                return HandleServiceAnnotation(serviceAnnotation, beanContextFactory, beanContext, beanConfiguration, type);
            }
            AnnotationEntry<ServiceClientAttribute> serviceClientAnnotation = serviceClientAnnotationCache.GetAnnotationEntry(type);
            if (serviceClientAnnotation != null)
            {
                return HandleServiceClientAnnotation(serviceClientAnnotation, beanContextFactory, beanContext, beanConfiguration, type);
            }
            return null;
        }

        protected String ExtractServiceName(String serviceName, Type type)
        {
            if (serviceName == null || serviceName.Length == 0)
            {
                serviceName = type.Name;
                if (serviceName.EndsWith("Proxy"))
                {
                    serviceName = serviceName.Substring(0, serviceName.Length - 5);
                }
                if (serviceName[0] == 'I' && Char.IsUpper(serviceName[1]))
                {
                    serviceName = serviceName.Substring(1);
                }
            }
            return serviceName;
        }

        protected ICascadedInterceptor HandleServiceAnnotation(ServiceAttribute serviceAnnotation, IBeanContextFactory beanContextFactory, IServiceContext beanContext, IBeanConfiguration beanConfiguration, Type type)
        {
            if (serviceAnnotation.CustomExport)
            {
                // Do nothing if the service wants to be exported by some special way anywhere else
                return null;
            }
            String beanName = beanConfiguration.GetName();
            String serviceName = ExtractServiceName(serviceAnnotation.Name, type);
            if (!IsNetworkClientMode)
            {
                CacheInterceptor interceptor = new CacheInterceptor();
                if (beanContext.IsRunning)
                {
                    interceptor = beanContext.RegisterWithLifecycle(interceptor).PropertyValue("ServiceName", serviceName).IgnoreProperties("ProcessService").Finish();
                    beanContext.Link(beanName).To<IServiceExtendable>().With(serviceName);
                }
                else
                {
                    beanContextFactory.RegisterWithLifecycle(interceptor).PropertyValue("ServiceName", serviceName).IgnoreProperties("ProcessService");
                    beanContextFactory.Link(beanName).To<IServiceExtendable>().With(serviceName);
                }
                if (Log.InfoEnabled)
                {
                    Log.Info("Registering application service '" + serviceName + "'");
                }
                return interceptor;
            }
            else
            {
                if (Log.InfoEnabled)
                {
                    Log.Info("Registering application mock service '" + serviceName + "'");
                }
                if (beanContext.IsRunning)
                {
                    beanContext.Link(beanName).To<IServiceExtendable>().With(serviceName);
                }
                else
                {
                    beanContextFactory.Link(beanName).To<IServiceExtendable>().With(serviceName);
                }
                return null;
            }
        }

        protected ICascadedInterceptor HandleServiceClientAnnotation(AnnotationEntry<ServiceClientAttribute> serviceClientAnnotation, IBeanContextFactory beanContextFactory, IServiceContext beanContext,
            IBeanConfiguration beanConfiguration, Type type)
        {
            String serviceName = ExtractServiceName(serviceClientAnnotation.Annotation.Name, serviceClientAnnotation.DeclaringType);
            CacheInterceptor interceptor = new CacheInterceptor();
            if (beanContext.IsRunning)
            {
                interceptor = beanContext.RegisterWithLifecycle(interceptor).PropertyValue("ServiceName", serviceName).Finish();
            }
            else
            {
                beanContextFactory.RegisterWithLifecycle(interceptor).PropertyValue("ServiceName", serviceName);
            }

            if (Log.InfoEnabled)
            {
                Log.Info("Creating application service stub for service '" + serviceName + "' accessing with '" + serviceClientAnnotation.DeclaringType.FullName + "'");
            }
            return interceptor;
        }

        protected String BuildCacheInterceptorName(String serviceName)
        {
            return "cacheInterceptor." + serviceName;
        }
    }
}
