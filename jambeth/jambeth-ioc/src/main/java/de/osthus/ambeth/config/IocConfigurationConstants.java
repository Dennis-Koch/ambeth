package de.osthus.ambeth.config;

import de.osthus.ambeth.annotation.ConfigurationConstantDescription;
import de.osthus.ambeth.annotation.ConfigurationConstants;

@ConfigurationConstants
public final class IocConfigurationConstants
{
	private IocConfigurationConstants()
	{
	}

	@ConfigurationConstantDescription("Switches the object collector/object pool feature on (\"true\") or off (\"false\"). Default is \"true\".")
	public static final String UseObjectCollector = "ambeth.ioc.objectcollector.active";

	@ConfigurationConstantDescription("TODO")
	public static final String TrackDeclarationTrace = "ambeth.ioc.declaration.trace.active";

	@ConfigurationConstantDescription("Allows to monitor all ioc managed beans via JMX. Valid values: \"true\" or \"false\". Default is \"true\".")
	public static final String MonitorBeansActive = "ambeth.ioc.monitoring.active";
}