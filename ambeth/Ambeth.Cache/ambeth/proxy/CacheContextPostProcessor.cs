using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Annotation;
using De.Osthus.Ambeth.Cache;
using De.Osthus.Ambeth.Ioc.Factory;
using System;
using System.Collections.Generic;
using De.Osthus.Ambeth.Ioc.Config;
using De.Osthus.Ambeth.Cache.Interceptor;
using De.Osthus.Ambeth.Cache.Config;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Ioc.Annotation;

namespace De.Osthus.Ambeth.Proxy
{
    public class CacheContextPostProcessor : AbstractCascadePostProcessor
    {
        [LogInstance]
        public ILogger Log { private get; set; }

        public class CacheContextAnnotationCache : AnnotationCache<CacheContext>
        {
            protected override bool AnnotationEquals(CacheContext left, CacheContext right)
            {
                return Equals(left.CacheType, right.CacheType);
            }
        }

        protected AnnotationCache<CacheContext> annotationCache = new CacheContextAnnotationCache();

        [Autowired]
        public CachePostProcessor CachePostProcessor { protected get; set; }

        protected override ICascadedInterceptor HandleServiceIntern(IBeanContextFactory beanContextFactory, IServiceContext beanContext, IBeanConfiguration beanConfiguration, Type type, ISet<Type> requestedTypes)
        {
            CacheContext cacheContext = annotationCache.GetAnnotation(type);
            if (cacheContext == null)
            {
                return null;
            }
            IMethodLevelBehavior<Attribute> cacheBehavior = CachePostProcessor.CreateInterceptorModeBehavior(type);

            CacheInterceptor interceptor = new CacheInterceptor();
            if (beanContext.IsRunning)
            {
                interceptor = beanContext.RegisterWithLifecycle(interceptor).PropertyValue("Behavior", cacheBehavior).IgnoreProperties("ProcessService").Finish();
            }
            else
            {
                beanContextFactory.RegisterWithLifecycle(interceptor).PropertyValue("Behavior", cacheBehavior).IgnoreProperties("ProcessService");
            }

            CacheType cacheType = cacheContext.CacheType;
            String cacheProviderName;
            switch (cacheType)
            {
                case CacheType.PROTOTYPE:
                    {
                        cacheProviderName = CacheNamedBeans.CacheProviderPrototype;
                        break;
                    }
                case CacheType.SINGLETON:
                    {
                        cacheProviderName = CacheNamedBeans.CacheProviderSingleton;
                        break;
                    }
                case CacheType.THREAD_LOCAL:
                    {
                        cacheProviderName = CacheNamedBeans.CacheProviderThreadLocal;
                        break;
                    }
                case CacheType.DEFAULT:
                    {
                        return interceptor;
                    }
                default:
                    throw new Exception("Not supported type: " + cacheType);
            }
            CacheContextInterceptor ccInterceptor = new CacheContextInterceptor();
            if (beanContext.IsRunning)
            {
                IBeanRuntime<CacheContextInterceptor> interceptorBR = beanContext.RegisterWithLifecycle(ccInterceptor);
                interceptorBR.PropertyRef("CacheProvider", cacheProviderName).PropertyValue("Target", interceptor);
                ccInterceptor = interceptorBR.Finish();
            }
            else
            {
                IBeanConfiguration interceptorBC = beanContextFactory.RegisterWithLifecycle(ccInterceptor);
                interceptorBC.PropertyRef("CacheProvider", cacheProviderName).PropertyValue("Target", interceptor);
            }
            return ccInterceptor;
        }
    }
}