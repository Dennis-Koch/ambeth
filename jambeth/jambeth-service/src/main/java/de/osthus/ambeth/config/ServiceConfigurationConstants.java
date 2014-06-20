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

	@ConfigurationConstantDescription("TODO")
	public static final String WrapAllInteractions = "ioc.logged";

	@ConfigurationConstantDescription("TODO")
	public static final String LogShortNames = "log.shortnames";

	@ConfigurationConstantDescription("TODO")
	public static final String NetworkClientMode = "network.clientmode";

	@ConfigurationConstantDescription("TODO")
	public static final String SlaveMode = "service.slavemode";

	@ConfigurationConstantDescription("TODO")
	public static final String OfflineMode = "network.offlinemode";

	@ConfigurationConstantDescription("TODO")
	public static final String OfflineModeSupported = "network.offlinemode.supported";

	@ConfigurationConstantDescription("TODO")
	public static final String GenericTransferMapping = "transfermapping.generic";

	@ConfigurationConstantDescription("TODO")
	public static final String IndependentMetaData = "metadata.independent";

	@ConfigurationConstantDescription("TODO")
	public static final String mappingFile = "mapping.file";

	@Deprecated
	@ConfigurationConstantDescription("TODO")
	public static final String mappingResource = "mapping.resource";

	@ConfigurationConstantDescription("TODO")
	public static final String valueObjectFile = "valueobject.file";

	@Deprecated
	@ConfigurationConstantDescription("TODO")
	public static final String valueObjectResource = "valueobject.resource";

	private ServiceConfigurationConstants()
	{
		// Intended blank
	}
}
