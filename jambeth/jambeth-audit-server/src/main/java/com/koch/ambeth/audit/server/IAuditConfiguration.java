package com.koch.ambeth.audit.server;

import com.koch.ambeth.service.metadata.Member;

public interface IAuditConfiguration
{
	boolean isAuditActive();

	boolean isReasonRequired();

	IAuditMemberConfiguration getMemberConfiguration(Member member);

	IAuditMemberConfiguration getMemberConfiguration(String memberName);
}
