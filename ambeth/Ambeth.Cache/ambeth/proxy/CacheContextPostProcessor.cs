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

        protected override ICascadedInterceptor HandleServiceIntern(IBeanContextFactory beanContextFactory, IServiceContext beanContext, IBeanConfiguration beanConfiguration, Type type, ISet<Type> requestedTypes)
        {
            CacheContext cacheContext = annotationCache.GetAnnotation(type);
            if (cacheContext == null)
            {
                return null;
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
                default:
                    throw new Exception("Not supported type: " + cacheType);
            }
            if (beanContext.IsRunning)
            {
                IBeanRuntime<CacheContextInterceptor> interceptorBR = beanContext.RegisterAnonymousBean<CacheContextInterceptor>();
                interceptorBR.PropertyRef("CacheProvider", cacheProviderName);

                // beanContextFactory.link(beanName, IServiceExtendable.class, value);
                return interceptorBR.Finish();
            }
            IBeanConfiguration interceptorBC = beanContextFactory.RegisterAnonymousBean<CacheContextInterceptor>();
            interceptorBC.PropertyRef("CacheProvider", cacheProviderName);

            // beanContextFactory.link(beanName, IServiceExtendable.class, value);
            return (ICascadedInterceptor)interceptorBC.GetInstance();
        }
    }
}