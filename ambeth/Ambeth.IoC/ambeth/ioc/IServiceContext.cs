using De.Osthus.Ambeth.Ioc.Config;
using De.Osthus.Ambeth.Ioc.Hierarchy;
using De.Osthus.Ambeth.Ioc.Link;
using System;
using System.Collections.Generic;
using System.Text;
using System.Threading;

namespace De.Osthus.Ambeth.Ioc
{

    public interface IServiceContext : IDisposable, ILinkRuntimeExtendable
    {
        /// <summary>
        /// Returns the name of the context
        /// </summary>
        String Name { get; }

        bool IsDisposed { get; }

        bool IsRunning { get; }

        IServiceContext GetParent();

        IServiceContext GetRoot();

        IServiceContext CreateService(params Type[] serviceModules);

        IServiceContext CreateService(RegisterPhaseDelegate registerPhaseDelegate, params Type[] serviceModules);

        IBeanContextHolder<V> CreateService<V>(params Type[] serviceModules);

        IBeanContextHolder<V> CreateService<V>(RegisterPhaseDelegate registerPhaseDelegate, params Type[] serviceModules);

        IBeanContextHolder<V> CreateHolder<V>();

        IBeanContextHolder<V> CreateHolder<V>(String beanName);

        Object GetService(String serviceName);

        Object GetService(String serviceName, bool checkExistence);

        V GetService<V>(String serviceName);

        V GetService<V>(String serviceName, bool checkExistence);

        V GetService<V>();

        V GetService<V>(bool checkExistence);

        Object GetService(Type autowiredType);

        Object GetService(Type autowiredType, bool checkExistence);

        IList<V> GetObjects<V>();

        //IList<Object> getAnnotatedObjects<A>() where A : Attribute;

        IList<T> GetImplementingObjects<T>();

        void RegisterDisposable(IDisposableBean disposableBean);

        void RegisterDisposeHook(WaitCallback waitCallback);

        IBeanRuntime<V> RegisterWithLifecycle<V>(V externalBean);

        IBeanRuntime<V> RegisterExternalBean<V>(V externalBean);

        IBeanRuntime<V> RegisterAnonymousBean<V>();

        IBeanRuntime<V> RegisterAnonymousBean<V>(Type beanType);

        IBeanConfiguration GetBeanConfiguration(String beanName);

        void PrintContent(StringBuilder sb);
    }
}
