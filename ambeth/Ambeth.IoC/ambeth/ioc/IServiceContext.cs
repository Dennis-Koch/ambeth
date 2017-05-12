using De.Osthus.Ambeth.Ioc.Config;
using De.Osthus.Ambeth.Ioc.Factory;
using De.Osthus.Ambeth.Ioc.Hierarchy;
using De.Osthus.Ambeth.Ioc.Link;
using De.Osthus.Ambeth.Threading;
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

        /// <summary>
        /// Checks if the context is already fully disposed. It is not the opposite of isRunning(). The context also could be starting or in the process of being disposed.
        /// </summary>
        bool IsDisposed { get; }

        /// <summary>
        /// Checks if the context is running. It is not the opposite of isDisposed(). The context also could be starting or in the process of being disposed.
        /// </summary>
        bool IsRunning { get; }

        /// <summary>
        /// Getter for the parent context of this context.
        /// </summary>
        /// <returns>Parent context or null if this is the root context.</returns>
        IServiceContext GetParent();

        /// <summary>
        /// Getter for the root context.
        /// </summary>
        /// <returns>Root context or this if this is the root context.</returns>
        IServiceContext GetRoot();

        /// <summary>
        /// Creates a child context of this context with the additional beans from the given initializing modules.
        /// </summary>
        /// <param name="serviceModules">Initializing modules defining the content of the new context.</param>
        /// <returns>New IoC context.</returns>
        IServiceContext CreateService(params Type[] serviceModules);

        /// <summary>
        /// Creates a child context of this context with the additional beans from the given initializing modules.
        /// </summary>
        /// <param name="childContextName">The name of the childContext. This makes sense if the context hierarchy will be monitored e.g. via JMX clients</param>
        /// <param name="serviceModules">Initializing modules defining the content of the new context.</param>
        /// <returns>New IoC context.</returns>
        IServiceContext CreateService(String childContextName, params Type[] serviceModules);

        /// <summary>
        /// Creates a child context of this context with the additional beans from the given initializing modules plus everything you do in the RegisterPhaseDelegate.
        /// </summary>
        /// <param name="registerPhaseDelegate">Similar to an already instantiated module.</param>
        /// <param name="serviceModules">Initializing modules defining the content of the new context.</param>
        /// <returns>New IoC context.</returns>
        IServiceContext CreateService(IBackgroundWorkerParamDelegate<IBeanContextFactory> registerPhaseDelegate, params Type[] serviceModules);

        /// <summary>
        ///  Creates a child context of this context with the additional beans from the given initializing modules plus everything you do in the RegisterPhaseDelegate.
        /// </summary>
        /// <param name="childContextName">The name of the childContext. This makes sense if the context hierarchy will be monitored e.g. via JMX clients</param>
        /// <param name="registerPhaseDelegate">Similar to an already instantiated module.</param>
        /// <param name="serviceModules">Initializing modules defining the content of the new context.</param>
        /// <returns>New IoC context.</returns>
        IServiceContext CreateService(String childContextName, IBackgroundWorkerParamDelegate<IBeanContextFactory> registerPhaseDelegate, params Type[] serviceModules);

        IBeanContextHolder<V> CreateService<V>(params Type[] serviceModules);

        IBeanContextHolder<V> CreateService<V>(IBackgroundWorkerParamDelegate<IBeanContextFactory> registerPhaseDelegate, params Type[] serviceModules);

        IBeanContextHolder<V> CreateHolder<V>();

        IBeanContextHolder<V> CreateHolder<V>(String beanName);

        /// <summary>
        /// Service bean lookup by name. Identical to GetService(serviceName, true).
        /// </summary>
        /// <param name="serviceName">Name of the service bean to lookup.</param>
        /// <returns>Requested service bean.</returns>
        Object GetService(String serviceName);

        /// <summary>
        /// Service bean lookup by name that may return null.
        /// </summary>
        /// <param name="serviceName">Name of the service bean to lookup.</param>
        /// <param name="checkExistence">Flag if bean is required to exist.</param>
        /// <returns>Requested service bean or null if bean does not exist and existence is not checked.</returns>
        Object GetService(String serviceName, bool checkExistence);

        V GetService<V>(String serviceName);

        V GetService<V>(String serviceName, bool checkExistence);

        V GetService<V>();

        V GetService<V>(bool checkExistence);

        /// <summary>
        /// Service bean lookup by type. Identical to GetService(autowiredType, true)
        /// </summary>
        /// <param name="autowiredType">Type the service bean is autowired to.</param>
        /// <returns>Requested service bean.</returns>
        Object GetService(Type autowiredType);

        /// <summary>
        /// Service bean lookup by type that may return null.
        /// </summary>
        /// <param name="autowiredType">Type the service bean is autowired to.</param>
        /// <param name="checkExistence">Flag if bean is required to exist.</param>
        /// <returns>Requested service bean or null if bean does not exist and existence is not checked.</returns>
        Object GetService(Type autowiredType, bool checkExistence);

        /// <summary>
        /// Lookup for all beans assignable to a given type.
        /// </summary>
        /// <typeparam name="V">Lookup type.</typeparam>
        /// <returns>All beans assignable to a given type.</returns>
        IList<V> GetObjects<V>();

        //IList<Object> getAnnotatedObjects<A>() where A : Attribute;

        /// <summary>
        /// Lookup for all beans implementing a given interface.
        /// </summary>
        /// <typeparam name="T">Interface type to look for.</typeparam>
        /// <returns>Implementing beans.</returns>
        IList<T> GetImplementingObjects<T>();

        /// <summary>
        /// Links an external bean instance to the contexts dispose life cycle hook.
        /// </summary>
        /// <param name="disposableBean">Bean instance to be disposed with this context.</param>
        void RegisterDisposable(IDisposableBean disposableBean);

        /// <summary>
        /// Adds a callback to be executed during context shutdown.
        /// </summary>
        /// <param name="waitCallback">Callback to be executed.</param>
        void RegisterDisposeHook(IBackgroundWorkerParamDelegate<IServiceContext> waitCallback);

        /// <summary>
        /// Adds an external bean to the context and links it to the contexts dispose life cycle hook. Injections are done by the context.
        /// </summary>
        /// <typeparam name="V">May be any type</typeparam>
        /// <param name="externalBean">External bean instance.</param>
        /// <returns>IBeanRuntime to add things to the bean or finish the registration.</returns>
        IBeanRuntime<V> RegisterWithLifecycle<V>(V externalBean);

        /// <summary>
        /// Adds an external bean to the context while the context is already running. No injections are done by the context.
        /// </summary>
        /// <typeparam name="V">May be any type</typeparam>
        /// <param name="externalBean">External bean instance.</param>
        /// <returns>IBeanRuntime to add things to the bean or finish the registration.</returns>
        IBeanRuntime<V> RegisterExternalBean<V>(V externalBean);

        /// <summary>
        /// Registers an anonymous bean while the context is already running.
        /// </summary>
        /// <typeparam name="V">Class of the bean to be instantiated.</typeparam>
        /// <returns>IBeanRuntime to add things to the bean or finish the registration.</returns>
        IBeanRuntime<V> RegisterAnonymousBean<V>();

        /// <summary>
        /// Registers an anonymous bean while the context is already running.
        /// </summary>
        /// <typeparam name="Object">May be anything</typeparam>
        /// <param name="beanType">Class of the bean to be instantiated.</param>
        /// <returns>IBeanRuntime to add things to the bean or finish the registration.</returns>
        IBeanRuntime<Object> RegisterAnonymousBean<Object>(Type beanType);

        /// <summary>
        /// Registers an anonymous bean while the context is already running.
        /// </summary>
        /// <typeparam name="V">Class of the bean to be instantiated.</typeparam>
        /// <returns>IBeanRuntime to add things to the bean or finish the registration.</returns>
        IBeanRuntime<V> RegisterBean<V>();

        /// <summary>
        /// Registers an anonymous bean while the context is already running.
        /// </summary>
        /// <typeparam name="Object">May be anything</typeparam>
        /// <param name="beanType">Class of the bean to be instantiated.</param>
        /// <returns>IBeanRuntime to add things to the bean or finish the registration.</returns>
        IBeanRuntime<Object> RegisterBean<Object>(Type beanType);

        /// <summary>
        /// Finder for configuration of a named bean. Makes it possible to read and change the IoC configuration of a bean during runtime.
        /// </summary>
        /// <param name="beanName">Name of the bean.</param>
        /// <returns> Configuration of a named bean.</returns>
        IBeanConfiguration GetBeanConfiguration(String beanName);

        void PrintContent(StringBuilder sb);
    }
}
