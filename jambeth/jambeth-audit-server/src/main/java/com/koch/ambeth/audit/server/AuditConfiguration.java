package com.koch.ambeth.audit.server;

import java.util.Map.Entry;

import com.koch.ambeth.service.metadata.Member;
import com.koch.ambeth.util.collections.HashMap;
import com.koch.ambeth.util.collections.IdentityHashMap;

public class AuditConfiguration implements IAuditConfiguration
{
	protected final boolean auditActive;

	protected final boolean reasonRequired;

	protected final HashMap<String, IAuditMemberConfiguration> memberNameToConfigurationMap;

	protected final IdentityHashMap<Member, IAuditMemberConfiguration> memberToConfigurationMap;

	public AuditConfiguration(boolean auditActive, boolean reasonRequired, IdentityHashMap<Member, IAuditMemberConfiguration> memberToConfigurationMap)
	{
		this.auditActive = auditActive;
		this.reasonRequired = reasonRequired;
		this.memberToConfigurationMap = memberToConfigurationMap;
		memberNameToConfigurationMap = HashMap.create(memberToConfigurationMap.size(), 0.5f);
		for (Entry<Member, IAuditMemberConfiguration> entry : memberToConfigurationMap)
		{
			memberNameToConfigurationMap.put(entry.getKey().getName(), entry.getValue());
		}
	}

	@Override
	public boolean isAuditActive()
	{
		return auditActive;
	}

	@Override
	public boolean isReasonRequired()
	{
		return reasonRequired;
	}

	@Override
	public IAuditMemberConfiguration getMemberConfiguration(String memberName)
	{
		return memberNameToConfigurationMap.get(memberName);
	}

	@Override
	public IAuditMemberConfiguration getMemberConfiguration(Member member)
	{
		return memberToConfigurationMap.get(member);
	}
}
