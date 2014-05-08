using System;
using System.Reflection;
using De.Osthus.Ambeth.Collections;
using De.Osthus.Ambeth.Ioc.Extendable;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Proxy;
using De.Osthus.Ambeth.Util;
#if SILVERLIGHT
using Castle.Core.Interceptor;
#else
using Castle.DynamicProxy;
#endif

namespace De.Osthus.Ambeth.Ioc.Link
{
    public class LinkContainer : AbstractLinkContainer
    {
        [LogInstance]
        public ILogger Log { private get; set; }

        public IExtendableRegistry ExtendableRegistry { protected get; set; }

        public IProxyFactory ProxyFactory { protected get; set; }

        protected MethodInfo addMethod, removeMethod;

        public override void AfterPropertiesSet()
        {
            base.AfterPropertiesSet();

            ParamChecker.AssertParamNotNull(ExtendableRegistry, "ExtendableRegistry");
            ParamChecker.AssertParamNotNull(ProxyFactory, "ProxyFactory");
        }

        protected override Object ResolveRegistryIntern(Object registry)
        {
            registry = base.ResolveRegistryIntern(registry);
            MethodInfo[] methods;
            Object[] linkArguments;
            if (RegistryPropertyName != null)
            {
                methods = ExtendableRegistry.GetAddRemoveMethods(registry.GetType(), RegistryPropertyName, Arguments, out linkArguments);
            }
            else
            {
                methods = ExtendableRegistry.GetAddRemoveMethods(RegistryBeanAutowiredType, Arguments, out linkArguments);
            }
            Arguments = linkArguments;
            addMethod = methods[0];
            removeMethod = methods[1];
            return registry;
        }

        protected override Object ResolveListenerIntern(Object listener)
        {
            listener = base.ResolveListenerIntern(listener);
            if (ListenerMethodName != null)
            {
                Type parameterType = addMethod.GetParameters()[0].ParameterType;
                MethodInfo[] methodsOnExpectedListenerType = parameterType.GetMethods(BindingFlags.Instance | BindingFlags.Public);
                HashMap<MethodInfo, MethodInfo> mappedMethods = new HashMap<MethodInfo,MethodInfo>();
                foreach (MethodInfo methodOnExpectedListenerType in methodsOnExpectedListenerType)
                {
                    ParameterInfo[] parameters = methodOnExpectedListenerType.GetParameters();
                    Type[] types = new Type[parameters.Length];
                    for (int a = parameters.Length; a-- > 0;)
                    {
                        types[a] = parameters[a].ParameterType;
                    }
                    MethodInfo method = listener.GetType().GetMethod(ListenerMethodName, types);
                    if (method != null)
                    {
                        mappedMethods.Put(methodOnExpectedListenerType, method);
                    }
                }
                IInterceptor interceptor = new DelegateInterceptor(listener, mappedMethods);
                listener = ProxyFactory.CreateProxy(parameterType, interceptor);
            }
            return listener;
        }

        protected override ILogger GetLog()
        {
            return Log;
        }

        protected void EvaluateRegistryMethods(Object registry)
        {
        }

        protected override void HandleLink(Object registry, Object listener)
        {
            EvaluateRegistryMethods(registry);
            Arguments[0] = listener;
            try
            {
                this.addMethod.Invoke(registry, Arguments);
            }
            catch (System.Exception)
            {
                throw;
            }
        }

        protected override void HandleUnlink(Object registry, Object listener)
        {
            Arguments[0] = listener;
            this.removeMethod.Invoke(registry, Arguments);
        }
    }
}
