using De.Osthus.Ambeth.Annotation;
using De.Osthus.Ambeth.Config;
using De.Osthus.Ambeth.Ioc.Config;
using De.Osthus.Ambeth.Ioc.Factory;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Log.Interceptor;
using De.Osthus.Ambeth.Proxy;
using System;
using System.Collections.Generic;

namespace De.Osthus.Ambeth.Ioc
{
    public class LoggingPostProcessor : AbstractCascadePostProcessor
    {
        [LogInstance]
		public ILogger Log { private get; set; }

        public class LoggingAnnotationCache : AnnotationCache<ServiceAttribute>
        {
            protected override bool AnnotationEquals(ServiceAttribute left, ServiceAttribute right)
            {
                return Equals(left.Interface, right.Interface) && Equals(left.Name, right.Name);
            }
        }

        [Property(ServiceConfigurationConstants.WrapAllInteractions, DefaultValue = "false")]
        public bool WrapAllInteractions { get; set; }
        
        protected readonly AnnotationCache<ServiceAttribute> annotationCache = new LoggingAnnotationCache();

        protected override ICascadedInterceptor HandleServiceIntern(IBeanContextFactory beanContextFactory, IServiceContext beanContext, IBeanConfiguration beanConfiguration, Type type, ISet<Type> requestedTypes)
        {
            if (!WrapAllInteractions)
            {
                return null;
            }
            if (annotationCache.GetAnnotation(type) == null)
            {
                return null;
            }
            LogInterceptor logInterceptor = new LogInterceptor();
            if (beanContext.IsRunning)
            {
                logInterceptor = beanContext.RegisterWithLifecycle(logInterceptor).Finish();
            }
            else
            {
                beanContextFactory.RegisterWithLifecycle(logInterceptor);
            }
            return logInterceptor;

            //if (service is IProxyTargetAccessor || service is IInterceptor)
            //{
            //    return service;
            //}
            //if (!requestedType.IsInterface)
            //{
            //    MethodInfo[] methods = service.GetType().GetMethods(BindingFlags.Public | BindingFlags.Instance);
            //    foreach (MethodInfo method in methods)
            //    {
            //        if (typeof(Object).Equals(method.DeclaringType))
            //        {
            //            continue;
            //        }
            //        if (!method.IsVirtual)
            //        {
            //            if (Log.DebugEnabled)
            //            {
            //                Log.Debug(service.GetType().FullName + "." + LogTypesUtil.PrintMethod(method.Name, method.GetParameters(), null, PrintShortStringNames)
            //                    + " is not virtual. Skipping LoggingProxy for this service instance");
            //            }
            //            return service;
            //        }
            //    }
            //}
            //ICascadedInterceptor logInterceptor = BeanContext.MonitorObject<LogInterceptor>();
            //logInterceptor.Target = service;
            //return ProxyFactory.CreateProxy(requestedType, logInterceptor);
        }

        public override PostProcessorOrder GetOrder()
        {
            return PostProcessorOrder.HIGHEST;
        }
    }
}
