package com.koch.ambeth.audit.server;

public interface IAuditConfigurationExtendable
{
	void registerAuditConfiguration(IAuditConfiguration auditConfiguration, Class<?> entityType);

	void unregisterAuditConfiguration(IAuditConfiguration auditConfiguration, Class<?> entityType);
}
