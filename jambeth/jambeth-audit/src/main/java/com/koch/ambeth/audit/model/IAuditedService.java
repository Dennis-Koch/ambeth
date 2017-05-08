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

@Audited(false)
public interface IAuditedService {
	public static final String Arguments = "Arguments";

	public static final String Entry = "Entry";

	public static final String MethodName = "MethodName";

	public static final String Order = "Order";

	public static final String ServiceType = "ServiceType";

	public static final String SpentTime = "SpentTime";

	String[] getArguments();

	IAuditEntry getEntry();

	String getMethodName();

	int getOrder();

	String getServiceType();

	long getSpentTime();
}
