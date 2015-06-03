package de.osthus.ambeth.concept;

import java.io.Closeable;
import java.util.Properties;

import de.osthus.ambeth.config.IProperties;

public interface IAmbethConfiguration
{
	/**
	 * Adds the provided properties to the root {@link de.osthus.ambeth.config.Properties} object.
	 * 
	 * @param properties
	 *            Properties to add
	 * @return This configuration object
	 */
	IAmbethConfiguration withProperties(IProperties properties);

	/**
	 * Adds the provided properties to the root {@link de.osthus.ambeth.config.Properties} object.
	 * 
	 * @param properties
	 *            Properties to add
	 * @return This configuration object
	 */
	IAmbethConfiguration withProperties(Properties properties);

	/**
	 * Adds the provided properties to the root {@link de.osthus.ambeth.config.Properties} object.
	 * 
	 * @param name
	 *            Name of the property to add
	 * @param value
	 *            Value of the property to add
	 * @return This configuration object
	 */
	IAmbethConfiguration withProperty(String name, String value);

	/**
	 * Adds the provided command line arguments to the root {@link de.osthus.ambeth.config.Properties} object.
	 * 
	 * @param args
	 *            Command line arguments
	 * @return This configuration object
	 */
	IAmbethConfiguration withArgs(String... args);

	/**
	 * Adds the provided module classes to the list of modules to start with the framework context.
	 * 
	 * @param modules
	 *            Ambeth modules
	 * @return This configuration object
	 */
	IAmbethConfiguration withAmbethModules(Class<?>... modules);

	/**
	 * Adds the provided module classes to the list of modules to start with the application context.
	 * 
	 * @param modules
	 *            Application modules
	 * @return This configuration object
	 */
	IAmbethConfiguration withApplicationModules(Class<?>... modules);

	/**
	 * Starts the configured Ambeth context. It returns an {@link Closeable} {@link IAmbethApplication} object that also holds the configured application
	 * context. Closing this object shuts down the root context and all of its child contexts.
	 * 
	 * @return Ambeth application object
	 */
	IAmbethApplication start();

	/**
	 * Starts the configured Ambeth context. Also it registers the {@link IAmbethApplication#close()} method with the JVM as a shutdown hook. So after all
	 * modules and services have finished their afterStarted() calls and the Thread leaves the main() methods the root context and application are cleanly shut
	 * down as well.
	 */
	void startAndClose();

	IServiceContext start1();
}