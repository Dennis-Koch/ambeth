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
	public static final String AuditMethodActive = "audit.method.active";
}
