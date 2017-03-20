package com.koch.ambeth.core.start;

import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.util.threading.IBackgroundWorkerParamDelegate;

public interface IAmbethConfigurationIntern extends IAmbethConfiguration {
	/**
	 * Adds the provided module classes to the list of modules to start with the framework context.
	 *
	 * @param modules Ambeth modules
	 * @return This configuration object
	 */
	@SuppressWarnings("unchecked")
	IAmbethConfigurationIntern withAmbethModules(
			IBackgroundWorkerParamDelegate<IBeanContextFactory>... moduleDelegates);

	/**
	 * Adds the provided module classes to the list of modules to start with the application context.
	 *
	 * @param modules Application modules
	 * @return This configuration object
	 */
	@SuppressWarnings("unchecked")
	IAmbethConfigurationIntern withApplicationModules(
			IBackgroundWorkerParamDelegate<IBeanContextFactory>... moduleDelegates);
}
