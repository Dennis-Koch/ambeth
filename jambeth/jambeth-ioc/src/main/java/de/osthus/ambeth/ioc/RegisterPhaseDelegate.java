package de.osthus.ambeth.ioc;

import de.osthus.ambeth.ioc.factory.IBeanContextFactory;

/**
 * Callback interface for the registering phase of the bean context. The usage is similar to registering an instance of an anonymous {@link IInitializingModule}
 * .
 */
public interface RegisterPhaseDelegate
{
	/**
	 * Callback method that is being called by the {@link IBeanContextFactory} during start.
	 * 
	 * @param childContextFactory
	 *            Instance of the calling BeanContextFactory.
	 */
	void invoke(IBeanContextFactory childContextFactory);
}
