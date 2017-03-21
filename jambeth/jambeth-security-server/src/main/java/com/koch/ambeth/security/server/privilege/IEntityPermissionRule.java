package com.koch.ambeth.security.server.privilege;

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

import com.koch.ambeth.merge.util.IPrefetchConfig;
import com.koch.ambeth.security.IAuthorization;
import com.koch.ambeth.security.server.privilege.evaluation.IEntityPermissionEvaluation;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.service.model.ISecurityScope;

public interface IEntityPermissionRule<T> extends IPermissionRule
{
	/**
	 * This greatly increases security processing with lists of entities, because all necessary valueholders can be initialized with the least possible database
	 * roundtrips. Use this feature carefully: Mention exactly what you will need later, nothing more or less.
	 * 
	 * @param entityType
	 * @param prefetchConfig
	 */
	void buildPrefetchConfig(Class<? extends T> entityType, IPrefetchConfig prefetchConfig);

	/**
	 * Use this to implement per-entity-instance security (in SQL-terminology: row-level-security) and/or per-property-instance security (in SQL:
	 * cell-level-security)
	 * 
	 * @param objRef
	 * @param entity
	 * @param currentUser
	 * @param securityScopes
	 * @param pe
	 */
	void evaluatePermissionOnInstance(IObjRef objRef, T entity, IAuthorization currentUser, ISecurityScope[] securityScopes, IEntityPermissionEvaluation pe);
}
