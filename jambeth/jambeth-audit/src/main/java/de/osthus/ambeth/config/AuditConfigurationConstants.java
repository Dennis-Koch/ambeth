package de.osthus.ambeth.config;

import de.osthus.ambeth.annotation.ConfigurationConstantDescription;
import de.osthus.ambeth.annotation.ConfigurationConstants;

@ConfigurationConstants
public final class AuditConfigurationConstants
{
	private AuditConfigurationConstants()
	{
		// intended blank
	}

	@ConfigurationConstantDescription("TODO")
	public static final String AuditActive = "audit.active";

	@ConfigurationConstantDescription("TODO")
	public static final String AuditedEntityDefaultModeActive = "audit.entity.defaultmode.active";

	@ConfigurationConstantDescription("TODO")
	public static final String AuditedEntityPropertyDefaultModeActive = "audit.entity.property.defaultmode.active";

	@ConfigurationConstantDescription("TODO")
	public static final String AuditedServiceDefaultModeActive = "audit.service.defaultmode.active";

	@ConfigurationConstantDescription("TODO")
	public static final String AuditedInformationHashAlgorithm = "audit.hashalgorithm.name";
}
