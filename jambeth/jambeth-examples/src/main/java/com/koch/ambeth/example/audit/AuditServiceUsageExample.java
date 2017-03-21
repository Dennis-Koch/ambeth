package com.koch.ambeth.example.audit;

/*-
 * #%L
 * jambeth-examples
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
import com.koch.ambeth.security.audit.model.Audited;

@Audited
public class AuditServiceUsageExample implements IMyAuditedService {
	@LogInstance
	private ILogger log;

	@Override
	public boolean myAuditedMethod(String someArg) {
		if (log.isInfoEnabled()) {
			log.info("This call will be audited: " + someArg);
		}
		return true;
	}

	@Audited(false)
	@Override
	public boolean myNotAuditedMethod(String someArg) {
		if (log.isInfoEnabled()) {
			log.info("This call is not audited: " + someArg);
		}
		return false;
	}
}
