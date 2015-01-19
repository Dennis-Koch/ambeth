package de.osthus.ambeth.audit;

import de.osthus.ambeth.metadata.Member;

public interface IAuditConfiguration
{
	boolean isAuditActive();

	boolean isReasonRequired();

	IAuditMemberConfiguration getMemberConfiguration(Member member);

	IAuditMemberConfiguration getMemberConfiguration(String memberName);
}
