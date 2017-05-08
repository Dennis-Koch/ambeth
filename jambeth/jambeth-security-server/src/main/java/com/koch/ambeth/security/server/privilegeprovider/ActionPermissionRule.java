package com.koch.ambeth.security.server.privilegeprovider;

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

import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.merge.util.IPrefetchConfig;
import com.koch.ambeth.security.IActionPermission;
import com.koch.ambeth.security.IAuthorization;
import com.koch.ambeth.security.server.privilege.IEntityPermissionRule;
import com.koch.ambeth.security.server.privilege.evaluation.IEntityPermissionEvaluation;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.service.model.ISecurityScope;

public class ActionPermissionRule implements IEntityPermissionRule<IActionPermission> {
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	public void buildPrefetchConfig(Class<? extends IActionPermission> entityType,
			IPrefetchConfig prefetchConfig) {
		// intended blank
	}

	@Override
	public void evaluatePermissionOnInstance(IObjRef objRef, IActionPermission entity,
			IAuthorization authorization, ISecurityScope[] securityScopes,
			IEntityPermissionEvaluation pe) {
		if (!authorization.hasActionPermission(entity.getName(), securityScopes)) {
			// this extension only handles the specific case where the user has the corresponding
			// actionPermission associated
			pe.allowRead().skipCUD().denyExecute();
			return;
		}
		// the association implies execution permission (no CUD operations) - these have to be handled
		// by another extension
		pe.allowRead().skipCUD().allowExecute();
		return;
	}
}
