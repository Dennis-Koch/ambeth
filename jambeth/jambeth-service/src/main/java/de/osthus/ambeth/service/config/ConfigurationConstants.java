package de.osthus.ambeth.service.config;

import de.osthus.ambeth.annotation.ConfigurationConstantDescription;

@de.osthus.ambeth.annotation.ConfigurationConstants
public final class ConfigurationConstants
{
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

	private ConfigurationConstants()
	{
		// Intended blank
	}
}
