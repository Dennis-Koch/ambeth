package de.osthus.ambeth.security.config;

import de.osthus.ambeth.annotation.ConfigurationConstantDescription;
import de.osthus.ambeth.annotation.ConfigurationConstants;

@ConfigurationConstants
public final class SecurityConfigurationConstants
{
	@ConfigurationConstantDescription("TODO")
	public static final String DefaultReadPrivilegeActive = "security.privilege.default.read-entity";

	@ConfigurationConstantDescription("TODO")
	public static final String DefaultReadPropertyPrivilegeActive = "security.privilege.default.read-property";

	@ConfigurationConstantDescription("TODO")
	public static final String DefaultCreatePrivilegeActive = "security.privilege.default.create-entity";

	@ConfigurationConstantDescription("TODO")
	public static final String DefaultCreatePropertyPrivilegeActive = "security.privilege.default.create-property";

	@ConfigurationConstantDescription("TODO")
	public static final String DefaultUpdatePrivilegeActive = "security.privilege.default.update-entity";

	@ConfigurationConstantDescription("TODO")
	public static final String DefaultUpdatePropertyPrivilegeActive = "security.privilege.default.update-property";

	@ConfigurationConstantDescription("TODO")
	public static final String DefaultDeletePrivilegeActive = "security.privilege.default.delete-entity";

	@ConfigurationConstantDescription("TODO")
	public static final String DefaultDeletePropertyPrivilegeActive = "security.privilege.default.delete-property";

	@ConfigurationConstantDescription("TODO")
	public static final String DefaultExecutePrivilegeActive = "security.privilege.default.execute";

	@ConfigurationConstantDescription("TODO")
	public static final String SecurityActive = "ambeth.security.active";

	private SecurityConfigurationConstants()
	{
		// Intended blank
	}
}
