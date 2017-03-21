package com.koch.ambeth.security.privilege;

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

import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.security.privilege.PrivilegeProvider.PrivilegeKey;
import com.koch.ambeth.security.privilege.model.IPrivilege;
import com.koch.ambeth.util.collections.HashMap;

public class PrivilegeCache implements IPrivilegeCache {
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	protected final HashMap<PrivilegeKey, IPrivilege> privilegeCache =
			new HashMap<>();

	@Override
	public void dispose() {
	}
}
