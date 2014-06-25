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
                IBeanRuntime<MergeInterceptor> interceptorBC = beanContext.RegisterWithLifecycle(mergeInterceptor);
                interceptorBC.PropertyValue("Behavior", behavior);
                return interceptorBC.Finish();
            }
            beanContextFactory.RegisterWithLifecycle(mergeInterceptor).PropertyValue("Behavior", behavior);
            return mergeInterceptor;
        }

        protected IMethodLevelBehavior<Attribute> CreateInterceptorModeBehavior(Type beanType)
        {
            HashMap<MethodKey, Attribute> methodToAnnotationMap = new HashMap<MethodKey, Attribute>();
            MethodInfo[] methods = ReflectUtil.GetMethods(beanType);
            foreach (MethodInfo method in methods)
            {
                Attribute annotation = LookForAnnotation(method);
                if (annotation != null)
                {
                    methodToAnnotationMap.Put(new MethodKey(method), annotation);
                    continue;
                }
                Type[] parameters = TypeUtil.GetParameterTypesToTypes(method.GetParameters());
                foreach (Type currInterface in beanType.GetInterfaces())
                {
                    MethodInfo methodOnInterface = ReflectUtil.GetDeclaredMethod(true, currInterface, null, method.Name, parameters);
                    if (methodOnInterface == null)
                    {
                        continue;
                    }
                    annotation = LookForAnnotation(methodOnInterface);
                    if (annotation == null)
                    {
                        continue;
                    }
                    methodToAnnotationMap.Put(new MethodKey(method), annotation);
                    break;
                }
            }
            return new MethodLevelBehavior<Attribute>(null, methodToAnnotationMap);
        }

        protected virtual Attribute LookForAnnotation(MethodInfo method)
        {
            IgnoreAttribute noProxy = AnnotationUtil.GetAnnotation<IgnoreAttribute>(method, false);
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