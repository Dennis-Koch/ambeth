package com.koch.ambeth.audit.server;

/*-
 * #%L
 * jambeth-audit-server
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

import java.util.Map.Entry;

import com.koch.ambeth.service.metadata.Member;
import com.koch.ambeth.util.collections.HashMap;
import com.koch.ambeth.util.collections.IdentityHashMap;

public class AuditConfiguration implements IAuditConfiguration {
	protected final boolean auditActive;

	protected final boolean reasonRequired;

	protected final HashMap<String, IAuditMemberConfiguration> memberNameToConfigurationMap;

	protected final IdentityHashMap<Member, IAuditMemberConfiguration> memberToConfigurationMap;

	public AuditConfiguration(boolean auditActive, boolean reasonRequired,
			IdentityHashMap<Member, IAuditMemberConfiguration> memberToConfigurationMap) {
		this.auditActive = auditActive;
		this.reasonRequired = reasonRequired;
		this.memberToConfigurationMap = memberToConfigurationMap;
		memberNameToConfigurationMap = HashMap.create(memberToConfigurationMap.size(), 0.5f);
		for (Entry<Member, IAuditMemberConfiguration> entry : memberToConfigurationMap) {
			memberNameToConfigurationMap.put(entry.getKey().getName(), entry.getValue());
		}
	}

	@Override
	public boolean isAuditActive() {
		return auditActive;
	}

	@Override
	public boolean isReasonRequired() {
		return reasonRequired;
	}

	@Override
	public IAuditMemberConfiguration getMemberConfiguration(String memberName) {
		return memberNameToConfigurationMap.get(memberName);
	}

	@Override
	public IAuditMemberConfiguration getMemberConfiguration(Member member) {
		return memberToConfigurationMap.get(member);
	}
}
