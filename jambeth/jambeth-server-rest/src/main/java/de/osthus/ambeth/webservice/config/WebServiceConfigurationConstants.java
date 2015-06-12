package de.osthus.ambeth.webservice.config;

import de.osthus.ambeth.annotation.ConfigurationConstantDescription;
import de.osthus.ambeth.annotation.ConfigurationConstants;

@ConfigurationConstants
public final class WebServiceConfigurationConstants
{
	private WebServiceConfigurationConstants()
	{
		// intended blank
	}

	@ConfigurationConstantDescription("TODO")
	public static final String SessionAuthorizationTimeToLive = "ambeth.session.authorization.ttl";
}
