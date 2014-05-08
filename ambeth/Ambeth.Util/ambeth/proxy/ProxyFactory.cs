using System;
using System.Collections.Generic;
using System.Reflection.Emit;
using System.Runtime.Serialization;
using System.Reflection;
using Castle.DynamicProxy;
#if !SILVERLIGHT
#else
using Castle.Core.Interceptor;
#endif

namespace De.Osthus.Ambeth.Proxy
{
    public class ProxyFactory : IProxyFactory
    {
        protected readonly ProxyGenerator proxyGenerator;
        
        public IProxyOptionsFactory ProxyOptionsFactory { get; set; }

        public ProxyFactory()
        {
            ModuleScope scope;
#if !SILVERLIGHT
            scope = new ModuleScope(false, false, ModuleScope.DEFAULT_ASSEMBLY_NAME, ModuleScope.DEFAULT_FILE_NAME, "De.Osthus.Ambeth.Proxy", "De.Osthus.Ambeth.Proxy.Transfer");
#else
            scope = new ModuleScope(false, ModuleScope.DEFAULT_ASSEMBLY_NAME, ModuleScope.DEFAULT_FILE_NAME, "De.Osthus.Ambeth.Proxy", "De.Osthus.Ambeth.Proxy.Transfer");
#endif
            IProxyBuilder builder = new DefaultProxyBuilder(scope);
            proxyGenerator = new ProxyGenerator(builder);
            ProxyOptionsFactory = new DefaultProxyOptionsFactory();
        }

        public T CreateProxy<T>(params IInterceptor[] interceptors)
        {
            return (T)CreateProxy(typeof(T), interceptors);
        }

        public T CreateProxy<T>(Type[] interfaces, params IInterceptor[] interceptors)
        {
            return (T)CreateProxy(typeof(T), interfaces, interceptors);
        }

        public Object CreateProxy(Type type, params IInterceptor[] interceptors)
        {
            if (type.IsInterface)
            {
                return proxyGenerator.CreateInterfaceProxyWithoutTarget(type, ProxyOptionsFactory.CreateProxyGenerationOptions(), interceptors);
            }
            return proxyGenerator.CreateClassProxy(type, ProxyOptionsFactory.CreateProxyGenerationOptions(), interceptors);
        }

        public Object CreateProxy(Type[] interfaces, params IInterceptor[] interceptors)
        {
            for (int a = 0, size = interfaces.Length; a < size; a++)
		    {
			    Type interfaceType = interfaces[a];
			    if (!interfaceType.IsInterface)
			    {
				    Type[] newInterfaces = new Type[interfaces.Length - 1];
				    Array.Copy(interfaces, 0, newInterfaces, 0, a);
                    if (interfaces.Length - a > 1)
				    {
                        Array.Copy(interfaces, a + 1, newInterfaces, a, interfaces.Length - a - 1);
				    }
				    return CreateProxy(interfaceType, newInterfaces, interceptors);
			    }
		    }
            return CreateProxy(typeof(Object), interfaces, interceptors);
        }

        public Object CreateProxy(Type type, Type[] interfaces, params IInterceptor[] interceptors)
        {
            for (int a = interfaces.Length; a-- > 0;)
            {
                if (typeof(IProxyTargetAccessor).Equals(interfaces[a]))
                {
                    List<Type> allInterfaces = new List<Type>(interfaces.Length);
                    allInterfaces.AddRange(interfaces);
                    allInterfaces.Remove(typeof(IProxyTargetAccessor));
                    interfaces = allInterfaces.ToArray();
                    break;
                }
            }
            if (type.IsInterface)
            {
                return proxyGenerator.CreateInterfaceProxyWithoutTarget(type, interfaces, ProxyOptionsFactory.CreateProxyGenerationOptions(), interceptors);
            }
            return proxyGenerator.CreateClassProxy(type, interfaces, ProxyOptionsFactory.CreateProxyGenerationOptions(), interceptors);
        }

        public ICascadedInterceptor Wrap(Object target, ICascadedInterceptor interceptor)
        {
            interceptor.Target = target;
            return interceptor;
        }
    }
}
