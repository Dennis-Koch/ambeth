package com.koch.ambeth.server.helloworld.security;

/*-
 * #%L
 * jambeth-server-helloworld
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
import com.koch.ambeth.security.IAuthorization;
import com.koch.ambeth.security.server.privilege.IEntityPermissionRule;
import com.koch.ambeth.security.server.privilege.evaluation.IEntityPermissionEvaluation;
import com.koch.ambeth.server.helloworld.transfer.TestEntity;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.service.model.ISecurityScope;

public class TestEntityPrivilegeProvider implements IEntityPermissionRule<TestEntity>
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	public void buildPrefetchConfig(Class<? extends TestEntity> entityType, IPrefetchConfig prefetchConfig)
	{
		// TestEntity.Relation is needed for security checks, this greatly increased security processing with
		// lists of entities, because all necessary valueholders can be initialized with the least possible database
		// roundtrips. Use this feature carefully: Mention exactly what you will need later, nothing more or less.
		prefetchConfig.add(TestEntity.class, "Relation");
	}

	@Override
	public void evaluatePermissionOnInstance(IObjRef objRef, TestEntity entity, IAuthorization authorizationManager, ISecurityScope[] securityScopes,
			IEntityPermissionEvaluation permissionEvaluation)
	{
		permissionEvaluation.allowRead().allowCreate().allowUpdate().denyDelete().denyExecute();
	}
}
