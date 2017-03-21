package com.koch.ambeth.security.server.service;

/*-
 * #%L
 * jambeth-security-server
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

import java.util.List;

import com.koch.ambeth.security.privilege.transfer.IPrivilegeOfService;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.service.model.ISecurityScope;
import com.koch.ambeth.util.threading.IResultingBackgroundWorkerDelegate;

public class PrivilegeServiceCall
		implements IResultingBackgroundWorkerDelegate<List<IPrivilegeOfService>> {
	private final IObjRef[] objRefs;

	private final ISecurityScope[] securityScopes;

	private final PrivilegeService privilegeService;

	public PrivilegeServiceCall(IObjRef[] objRefs, ISecurityScope[] securityScopes,
			PrivilegeService privilegeService) {
		this.objRefs = objRefs;
		this.securityScopes = securityScopes;
		this.privilegeService = privilegeService;
	}

	@Override
	public List<IPrivilegeOfService> invoke() throws Throwable {
		return privilegeService.getPrivilegesIntern(objRefs, securityScopes);
	}
}
