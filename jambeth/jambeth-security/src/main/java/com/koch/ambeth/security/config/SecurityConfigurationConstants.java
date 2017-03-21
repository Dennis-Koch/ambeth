package com.koch.ambeth.security.config;

/*-
 * #%L
 * jambeth-security
 * %%
 * Copyright (C) 2017 Koch Softwaredevelopment
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 * #L%
 */

import com.koch.ambeth.util.annotation.ConfigurationConstantDescription;
import com.koch.ambeth.util.annotation.ConfigurationConstants;

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

	private SecurityConfigurationConstants()
	{
		// Intended blank
	}
}
