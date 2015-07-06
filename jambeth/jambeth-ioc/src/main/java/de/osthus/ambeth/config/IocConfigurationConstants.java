package de.osthus.ambeth.config;

import de.osthus.ambeth.annotation.ConfigurationConstants;
import de.osthus.ambeth.objectcollector.ICollectableControllerExtendable;

@ConfigurationConstants
public final class IocConfigurationConstants
{
	private IocConfigurationConstants()
	{
	}

	/**
	 * Switches the object collector/object pool feature on ("true") or off ("false"). Default is "true".
	 * 
	 * @see ICollectableControllerExtendable
	 */
	public static final String UseObjectCollector = "ambeth.ioc.objectcollector.active";

	/**
	 * Defines whether IoC classes (especially beans and properties) should save the stack trace of their declaration. If set to true the classes store a
	 * compact form of the stack trace during constructor invocation in a property, mostly called <code>declarationStackTrace</code>. Valid values: "true" or
	 * "false". Default is "false".
	 */
	public static final String TrackDeclarationTrace = "ambeth.ioc.declaration.trace.active";

	/**
	 * Allows to monitor all IoC managed beans via JMX. Valid values: "true" or "false". Default is "true".
	 */
	public static final String MonitorBeansActive = "ambeth.ioc.monitoring.active";

	/**
	 * Allows to run the IoC container in debug mode. This e.g. disables several security behaviors to ease debugging. Valid values: "true" or "false". Default
	 * is "false".
	 */
	public static final String DebugModeActive = "ambeth.ioc.debug.active";

	/**
	 * Allows Ambeth to transparently fork several algorithms to reduce execution time. This is very beneficial e.g. for large JDBC SELECT usecases or Ambeth
	 * Entity Prefetch logic. Valid values: "true" or "false". Default is "true". is "false".
	 */
	public static final String TransparentParallelizationActive = "ambeth.transparent.parallel.active";;
}
