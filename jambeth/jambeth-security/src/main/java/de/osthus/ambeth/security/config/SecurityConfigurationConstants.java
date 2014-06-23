package de.osthus.ambeth.security.config;

import de.osthus.ambeth.annotation.ConfigurationConstantDescription;
import de.osthus.ambeth.annotation.ConfigurationConstants;

@ConfigurationConstants
public final class SecurityConfigurationConstants
{
	@ConfigurationConstantDescription("TODO")
	public static final String DefaultReadPrivilegeActive = "security.privilege.default.read";

	@ConfigurationConstantDescription("TODO")
	public static final String DefaultCreatePrivilegeActive = "security.privilege.default.create";

	@ConfigurationConstantDescription("TODO")
	public static final String DefaultUpdatePrivilegeActive = "security.privilege.default.update";

	@ConfigurationConstantDescription("TODO")
	public static final String DefaultDeletePrivilegeActive = "security.privilege.default.delete";

	@ConfigurationConstantDescription("TODO")
	public static final String DefaultExecutePrivilegeActive = "security.privilege.default.execute";

	@ConfigurationConstantDescription("TODO")
	public static final String SecurityActive = "ambeth.security.active";

	private SecurityConfigurationConstants()
	{
		// Intended blank
	}
}
