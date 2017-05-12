using De.Osthus.Ambeth.Annotation;
using De.Osthus.Ambeth.Collections;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Merge.Interceptor;
using De.Osthus.Ambeth.Util;
using System;
using System.Reflection;

namespace De.Osthus.Ambeth.Proxy
{
    public class MergePostProcessor : AbstractCascadePostProcessor
    {
        [LogInstance]
        public ILogger Log { private get; set; }

        public class MergeContextAnnotationCache : AnnotationCache<MergeContext>
        {
            protected override bool AnnotationEquals(MergeContext left, MergeContext right)
            {
                return true;
            }
        }

        protected readonly AnnotationCache<MergeContext> mergeContextCache = new MergeContextAnnotationCache();
        
        public override PostProcessorOrder GetOrder()
        {
            return PostProcessorOrder.HIGH;
        }

        protected override ICascadedInterceptor HandleServiceIntern(Ioc.Factory.IBeanContextFactory beanContextFactory, Ioc.IServiceContext beanContext, Ioc.Config.IBeanConfiguration beanConfiguration, System.Type type, System.Collections.Generic.ISet<System.Type> requestedTypes)
        {
            MergeContext mergeContext = mergeContextCache.GetAnnotation(type);
            if (mergeContext == null)
            {
                return null;
            }
            IMethodLevelBehavior<Attribute> behavior = CreateInterceptorModeBehavior(type);

            MergeInterceptor mergeInterceptor = new MergeInterceptor();
            if (beanContext.IsRunning)
            {
                return beanContext.RegisterWithLifecycle(mergeInterceptor)//
                    .PropertyValue("Behavior", behavior)//
                    .IgnoreProperties("ServiceName")//
                    .Finish();
            }
            beanContextFactory.RegisterWithLifecycle(mergeInterceptor)//
                .PropertyValue("Behavior", behavior)//
                .IgnoreProperties("ServiceName");
            return mergeInterceptor;
        }
                
        protected override Attribute LookForAnnotation(MemberInfo method)
        {
            Attribute annotation = base.LookForAnnotation(method);
            if (annotation != null)
            {
                return annotation;
            }
            NoProxyAttribute noProxy = AnnotationUtil.GetAnnotation<NoProxyAttribute>(method, false);
            if (noProxy != null)
            {
                return noProxy;
            }
            ProcessAttribute process = AnnotationUtil.GetAnnotation<ProcessAttribute>(method, false);
            if (process != null)
            {
                return process;
            }
            FindAttribute find = AnnotationUtil.GetAnnotation<FindAttribute>(method, false);
            if (find != null)
            {
                return find;
            }
            MergeAttribute merge = AnnotationUtil.GetAnnotation<MergeAttribute>(method, false);
            if (merge != null)
            {
                return merge;
            }
            return AnnotationUtil.GetAnnotation<RemoveAttribute>(method, false);
        }
    }
}