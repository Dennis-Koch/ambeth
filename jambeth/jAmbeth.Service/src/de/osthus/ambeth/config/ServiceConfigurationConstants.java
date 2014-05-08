package de.osthus.ambeth.config;

import de.osthus.ambeth.annotation.ConfigurationConstantDescription;
import de.osthus.ambeth.annotation.ConfigurationConstants;

@ConfigurationConstants
public final class ServiceConfigurationConstants
{
	@ConfigurationConstantDescription("TODO")
	public static final String ServiceBaseUrl = "service.baseurl";

	@ConfigurationConstantDescription("TODO")
	public static final String ServiceProtocol = "service.protocol";

	@ConfigurationConstantDescription("TODO")
	public static final String ServiceHostName = "service.host";

	@ConfigurationConstantDescription("TODO")
	public static final String ServiceHostPort = "service.port";

	@ConfigurationConstantDescription("TODO")
	public static final String TypeInfoProviderType = "service.typeinfoprovider.type";

	@ConfigurationConstantDescription("TODO")
	public static final String ToOneDefaultCascadeLoadMode = "cache.cascadeload.toone";

	@ConfigurationConstantDescription("TODO")
	public static final String ToManyDefaultCascadeLoadMode = "cache.cascadeload.tomany";

	private ServiceConfigurationConstants()
	{
		// Intended blank
	}
}
