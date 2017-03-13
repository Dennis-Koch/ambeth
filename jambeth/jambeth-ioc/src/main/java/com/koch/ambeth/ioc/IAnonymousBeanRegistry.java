package com.koch.ambeth.ioc;

import com.koch.ambeth.ioc.config.IBeanConfiguration;
import com.koch.ambeth.util.IDisposable;

/**
 * Bean registry interface covering the register methods for anonymous beans.
 */
public interface IAnonymousBeanRegistry
{
	/**
	 * To register an already instantiated anonymous bean in the context. Injections will be made and life cycle methods will be called.
	 * 
	 * @param object
	 *            Bean instance to register.
	 * @return The bean configuration instance to add properties and configurations.
	 */
	IBeanConfiguration registerWithLifecycle(Object object);

	/**
	 * Register an object instance implementing {@link IDisposable} for the bean context shutdown life cycle event.
	 * 
	 * @param disposable
	 *            Disposable object instance.
	 */
	void registerDisposable(IDisposable disposable);

	/**
	 * Register an object instance implementing {@link IDisposableBean} for the bean context shutdown life cycle event.
	 * 
	 * @param disposable
	 *            Disposable object instance.
	 */
	void registerDisposable(IDisposableBean disposableBean);

	/**
	 * To register an already instantiated anonymous bean in the context. Injections will be made, life cycle methods will not be called.
	 * 
	 * @param externalBean
	 *            Bean instance to register.
	 * @return The bean configuration instance to add properties and configurations.
	 */
	IBeanConfiguration registerExternalBean(Object externalBean);

	/**
	 * To register an anonymous bean in the context.
	 * 
	 * @param beanType
	 *            Class of the bean to be instantiated.
	 * @return The bean configuration instance to add properties and configurations.
	 */
	IBeanConfiguration registerAnonymousBean(Class<?> beanType);

	/**
	 * To register an anonymous bean in the context.
	 * 
	 * @param beanType
	 *            Class of the bean to be instantiated.
	 * @return The bean configuration instance to add properties and configurations.
	 */
	IBeanConfiguration registerBean(Class<?> beanType);
}
