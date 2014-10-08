package de.osthus.ambeth.audit;

import java.util.Map.Entry;

import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.collections.IdentityHashMap;
import de.osthus.ambeth.metadata.Member;

public class AuditConfiguration implements IAuditConfiguration
{
	protected final boolean auditActive;

	protected final HashMap<String, IAuditMemberConfiguration> memberNameToConfigurationMap;

	protected final IdentityHashMap<Member, IAuditMemberConfiguration> memberToConfigurationMap;

	public AuditConfiguration(boolean auditActive, IdentityHashMap<Member, IAuditMemberConfiguration> memberToConfigurationMap)
	{
		this.auditActive = auditActive;
		this.memberToConfigurationMap = memberToConfigurationMap;
		this.memberNameToConfigurationMap = HashMap.create(memberToConfigurationMap.size(), 0.5f);
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
