package de.osthus.ambeth.ioc;

import java.lang.annotation.Annotation;

import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.ioc.config.IBeanConfiguration;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.ioc.hierarchy.IBeanContextHolder;
import de.osthus.ambeth.ioc.link.ILinkRuntimeExtendable;
import de.osthus.ambeth.threading.IBackgroundWorkerParamDelegate;
import de.osthus.ambeth.util.IDisposable;

/**
 * Interface to access a running jAmbeth IoC context from application code.
 */
public interface IServiceContext extends IDisposable, ILinkRuntimeExtendable
{
	/**
	 * Returns the name of the context
	 * 
	 * @return The name of the context
	 */
	String getName();

	/**
	 * Checks if the context is already fully disposed. It is not the opposite of isRunning(). The context also could be starting or in the process of being
	 * disposed.
	 * 
	 * @return True if the context is disposed, otherwise false.
	 */
	boolean isDisposed();

	/**
	 * Checks if the context is running. It is not the opposite of isDisposed(). The context also could be starting or in the process of being disposed.
	 * 
	 * @return True if the context is running, otherwise false.
	 */
	boolean isRunning();

	/**
	 * Getter for the parent context of this context.
	 * 
	 * @return Parent context or null if this is the root context.
	 */
	IServiceContext getParent();

	/**
	 * Getter for the root context.
	 * 
	 * @return Root context or this if this is the root context.
	 */
	IServiceContext getRoot();

	/**
	 * Creates a child context of this context with the additional beans from the given initializing modules.
	 * 
	 * @param serviceModules
	 *            Initializing modules defining the content of the new context.
	 * @return New IoC context.
	 */
	IServiceContext createService(Class<?>... serviceModules);

	/**
	 * Creates a child context of this context with the additional beans from the given initializing modules.
	 * 
	 * @param childContextName
	 *            The name of the childContext. This makes sense if the context hierarchy will be monitored e.g. via JMX clients
	 * @param serviceModules
	 *            Initializing modules defining the content of the new context.
	 * @return New IoC context.
	 */
	IServiceContext createService(String childContextName, Class<?>... serviceModules);

	/**
	 * Creates a child context of this context with the additional beans from the given initializing modules plus everything you do in the
	 * RegisterPhaseDelegate.
	 * 
	 * @param registerPhaseDelegate
	 *            Similar to an already instantiated module.
	 * @param serviceModules
	 *            Initializing modules defining the content of the new context.
	 * @return New IoC context.
	 */
	IServiceContext createService(IBackgroundWorkerParamDelegate<IBeanContextFactory> registerPhaseDelegate, Class<?>... serviceModules);

	/**
	 * Creates a child context of this context with the additional beans from the given initializing modules plus everything you do in the
	 * RegisterPhaseDelegate.
	 * 
	 * @param childContextName
	 *            The name of the childContext. This makes sense if the context hierarchy will be monitored e.g. via JMX clients
	 * @param registerPhaseDelegate
	 *            Similar to an already instantiated module.
	 * @param serviceModules
	 *            Initializing modules defining the content of the new context.
	 * @return New IoC context.
	 */
	IServiceContext createService(String childContextName, IBackgroundWorkerParamDelegate<IBeanContextFactory> registerPhaseDelegate,
			Class<?>... serviceModules);

	/**
	 * For future feature of complex context hierarchies.
	 * 
	 * @param autowiredBeanClass
	 *            Type the service bean is autowired to.
	 * @return Lazy holder for the requested bean.
	 */
	<V> IBeanContextHolder<V> createHolder(Class<V> autowiredBeanClass);

	/**
	 * For future feature of complex context hierarchies.
	 * 
	 * @param beanName
	 *            Name of the service bean to lookup.
	 * @param expectedClass
	 *            Type the service bean is casted to.
	 * @return Lazy holder for the requested bean.
	 */
	<V> IBeanContextHolder<V> createHolder(String beanName, Class<V> expectedClass);

	/**
	 * Service bean lookup by name. Identical to getService(serviceName, true).
	 * 
	 * @param serviceName
	 *            Name of the service bean to lookup.
	 * @return Requested service bean.
	 */
	Object getService(String serviceName);

	/**
	 * Service bean lookup by name that may return null.
	 * 
	 * @param serviceName
	 *            Name of the service bean to lookup.
	 * @param checkExistence
	 *            Flag if bean is required to exist.
	 * @return Requested service bean or null if bean does not exist and existence is not checked.
	 */
	Object getService(String serviceName, boolean checkExistence);

	/**
	 * Service bean lookup by name with defined return type.
	 * 
	 * @param serviceName
	 *            Name of the service bean to lookup.
	 * @param targetType
	 *            Type the service bean is casted to.
	 * @return Requested service bean.
	 */
	<V> V getService(String serviceName, Class<V> targetType);

	/**
	 * Service bean lookup by name with defined return type.
	 * 
	 * @param serviceName
	 *            Name of the service bean to lookup.
	 * @param targetType
	 *            Type the service bean is casted to.
	 * @param checkExistence
	 *            Flag if bean is required to exist.
	 * @return Requested service bean or null if bean does not exist and existence is not checked.
	 */
	<V> V getService(String serviceName, Class<V> targetType, boolean checkExistence);

	/**
	 * Service bean lookup by type. Identical to getService(autowiredType, true)
	 * 
	 * @param type
	 *            Type the service bean is autowired to.
	 * @return Requested service bean.
	 */
	<T> T getService(Class<T> type);

	/**
	 * Service bean lookup by type that may return null.
	 * 
	 * @param type
	 *            Type the service bean is autowired to.
	 * @param checkExistence
	 *            Flag if bean is required to exist.
	 * @return Requested service bean or null if bean does not exist and existence is not checked.
	 */
	<T> T getService(Class<T> type, boolean checkExistence);

	/**
	 * Lookup for all beans assignable to a given type.
	 * 
	 * @param type
	 *            Lookup type.
	 * @return All beans assignable to a given type.
	 */
	<T> IList<T> getObjects(Class<T> type);

	/**
	 * Lookup for all beans annotated with a given annotation.
	 * 
	 * @param type
	 *            Annotation type to look for.
	 * @return Annotated beans.
	 */
	<T extends Annotation> IList<Object> getAnnotatedObjects(Class<T> type);

	/**
	 * Lookup for all beans implementing a given interface.
	 * 
	 * @param interfaceType
	 *            Interface type to look for.
	 * @return Implementing beans.
	 */
	<T> IList<T> getImplementingObjects(Class<T> interfaceType);

	/**
	 * Links an external bean instance to the contexts dispose life cycle hook.
	 * 
	 * @param disposableBean
	 *            Bean instance to be disposed with this context.
	 */
	void registerDisposable(IDisposableBean disposableBean);

	/**
	 * Adds a callback to be executed during context shutdown.
	 * 
	 * @param disposeCallback
	 *            Callback to be executed.
	 */
	void registerDisposeHook(IBackgroundWorkerParamDelegate<IServiceContext> disposeCallback);

	/**
	 * Adds an external bean to the context and links it to the contexts dispose life cycle hook. Injections are done by the context.
	 * 
	 * @param object
	 *            External bean instance.
	 * @return IBeanRuntime to add things to the bean or finish the registration.
	 */
	<V> IBeanRuntime<V> registerWithLifecycle(V object);

	/**
	 * Adds an external bean to the context while the context is already running. No injections are done by the context.
	 * 
	 * @param externalBean
	 *            External bean instance.
	 * @return IBeanRuntime to add things to the bean or finish the registration.
	 */
	<V> IBeanRuntime<V> registerExternalBean(V externalBean);

	/**
	 * Registers an anonymous bean while the context is already running.
	 * 
	 * @param beanType
	 *            Class of the bean to be instantiated.
	 * @return IBeanRuntime to add things to the bean or finish the registration.
	 */
	<V> IBeanRuntime<V> registerAnonymousBean(Class<V> beanType);

	/**
	 * Registers an anonymous bean while the context is already running.
	 * 
	 * @param beanType
	 *            Class of the bean to be instantiated.
	 * @return IBeanRuntime to add things to the bean or finish the registration.
	 */
	<V> IBeanRuntime<V> registerBean(Class<V> beanType);

	/**
	 * Finder for configuration of a named bean. Makes it possible to read and change the IoC configuration of a bean during runtime.
	 * 
	 * @param beanName
	 *            Name of the bean.
	 * @return Configuration of a named bean.
	 */
	IBeanConfiguration getBeanConfiguration(String beanName);

	/**
	 * Disabled.
	 * 
	 * @param sb
	 *            Target StringBuilder.
	 */
	void printContent(StringBuilder sb);
}
