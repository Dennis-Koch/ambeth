package de.osthus.ambeth.config;

import de.osthus.ambeth.annotation.ConfigurationConstantDescription;
import de.osthus.ambeth.annotation.ConfigurationConstants;

@ConfigurationConstants
public final class XmlConfigurationConstants
{
	@ConfigurationConstantDescription("TODO")
	public static final String PackageScanPatterns = "ambeth.xml.transfer.pattern";

	private XmlConfigurationConstants()
	{
		// Intended blank
	}
}
