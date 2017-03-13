package com.koch.ambeth.example.audit;

import com.koch.ambeth.security.audit.model.Audited;

@Audited
public interface AuditEntityUsageExample {
	Integer getId();

	Integer getVersion();

	String getName();

	void setName(String name);

	@Audited(false)
	String getNotAuditedName();

	void setNotAuditedName(String notAuditedName);
}
