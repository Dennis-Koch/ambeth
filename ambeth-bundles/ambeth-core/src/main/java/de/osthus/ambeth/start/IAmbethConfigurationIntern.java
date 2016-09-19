package de.osthus.ambeth.start;

import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.threading.IBackgroundWorkerParamDelegate;

public interface IAmbethConfigurationIntern extends IAmbethConfiguration
{
	/**
	 * Adds the provided module classes to the list of modules to start with the framework context.
	 * 
	 * @param modules
	 *            Ambeth modules
	 * @return This configuration object
	 */
	IAmbethConfigurationIntern withAmbethModules(IBackgroundWorkerParamDelegate<IBeanContextFactory>... moduleDelegates);

	/**
	 * Adds the provided module classes to the list of modules to start with the application context.
	 * 
	 * @param modules
	 *            Application modules
	 * @return This configuration object
	 */
	IAmbethConfigurationIntern withApplicationModules(IBackgroundWorkerParamDelegate<IBeanContextFactory>... moduleDelegates);
}