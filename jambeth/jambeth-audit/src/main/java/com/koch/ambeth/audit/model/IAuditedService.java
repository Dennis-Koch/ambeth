package com.koch.ambeth.audit.model;

/*-
 * #%L
 * jambeth-audit
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

import com.koch.ambeth.security.audit.model.Audited;
import com.koch.ambeth.util.annotation.Interning;

/**
 * For each invocation of a service component tracked with the audit trail a new instance of this
 * will be created. So if you have a service called by each user during login you will also have the
 * same amount ditedEntityRelationProperty}.
 */
@Audited(false)
public interface IAuditedService {
	String Arguments = "Arguments";

	String Entry = "Entry";

	String MethodName = "MethodName";

	String Order = "Order";

	String ServiceType = "ServiceType";

	String SpentTime = "SpentTime";

	String[] getArguments();

	IAuditEntry getEntry();

	@Interning // it can be assumed that the variance of distinct method names is limited
	String getMethodName();

	int getOrder();

	@Interning // it can be assumed that the variance of distinct service names is limited
	String getServiceType();

	long getSpentTime();
}
