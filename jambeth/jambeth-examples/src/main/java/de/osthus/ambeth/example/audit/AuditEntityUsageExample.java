package de.osthus.ambeth.example.audit;

import de.osthus.ambeth.audit.Audited;

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
