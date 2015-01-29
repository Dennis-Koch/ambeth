using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Ioc.Config;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Util;
using De.Osthus.Ambeth.Ioc.Factory;
using System.Reflection;

#if !SILVERLIGHT
using Castle.DynamicProxy;
#else
using Castle.Core.Interceptor;
#endif

namespace De.Osthus.Ambeth.Proxy
{
    public class AbstractCascadePostProcessor : IBeanPostProcessor, IInitializingBean
    {
        [LogInstance]
        public ILogger log;

        public IProxyFactory ProxyFactory { get; set; }

        public virtual void AfterPropertiesSet()
        {
            ParamChecker.AssertNotNull(ProxyFactory, "ProxyFactory");
        }

        public Object PostProcessBean(IBeanContextFactory beanContextFactory, IServiceContext beanContext, IBeanConfiguration beanConfiguration, Type beanType, Object targetBean, ISet<Type> requestedTypes)
        {
            IProxyTargetAccessor factory = null;
            ICascadedInterceptor cascadedInterceptor = null;
            Object proxiedTargetBean = targetBean;
            if (targetBean is IProxyTargetAccessor)
            {
                factory = (IProxyTargetAccessor)targetBean;
                IInterceptor[] interceptors = factory.GetInterceptors();
                IInterceptor callback = (interceptors != null && interceptors.Length > 0 ? interceptors[0] : null);
                if (callback is ICascadedInterceptor)
                {
                    cascadedInterceptor = (ICascadedInterceptor)callback;
                    proxiedTargetBean = cascadedInterceptor.Target;
                }
            }
            ICascadedInterceptor interceptor = HandleServiceIntern(beanContextFactory, beanContext, beanConfiguration, proxiedTargetBean.GetType(), requestedTypes);
            if (interceptor == null)
            {
                return targetBean;
            }
            if (log.DebugEnabled)
            {
                log.Debug(GetType().Name + " intercepted bean with name '" + beanConfiguration.GetName() + "'");
            }
            Object target;
            if (cascadedInterceptor != null)
            {
                target = cascadedInterceptor;
            }
            else
            {
                target = proxiedTargetBean;
            }
            interceptor.Target = target;
            Object proxy = ProxyFactory.CreateProxy(requestedTypes.ToArray(), interceptor);
            postHandleServiceIntern(beanContextFactory, beanContext, beanConfiguration, proxiedTargetBean.GetType(), requestedTypes, proxy);
            return proxy;
        }

        protected virtual void postHandleServiceIntern(IBeanContextFactory beanContextFactory, IServiceContext beanContext, IBeanConfiguration beanConfiguration, Type type, ISet<Type> requestedTypes, Object proxy)
        {
            // Intended blank
        }

        protected virtual ICascadedInterceptor HandleServiceIntern(IBeanContextFactory beanContextFactory, IServiceContext beanContext, IBeanConfiguration beanConfiguration, Type type, ISet<Type> requestedTypes)
        {
            return null;
        }

        public IMethodLevelBehavior<Attribute> CreateInterceptorModeBehavior(Type beanType)
        {
            MethodLevelHashMap<Attribute> methodToAnnotationMap = new MethodLevelHashMap<Attribute>();
            MethodInfo[] methods = ReflectUtil.GetMethods(beanType);
            foreach (MethodInfo method in methods)
            {
                Attribute annotation = LookForAnnotation(method);
                if (annotation != null)
                {
                    methodToAnnotationMap.Put(method, annotation);
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
                    methodToAnnotationMap.Put(method, annotation);
                    break;
                }
            }
            return new MethodLevelBehavior<Attribute>(LookForAnnotation(beanType), methodToAnnotationMap);
        }

        protected virtual Attribute LookForAnnotation(MemberInfo method)
        {
            return null;
        }
    }
}
