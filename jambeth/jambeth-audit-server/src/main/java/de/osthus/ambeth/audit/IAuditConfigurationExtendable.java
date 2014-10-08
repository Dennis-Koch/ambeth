package de.osthus.ambeth.audit;

public interface IAuditConfigurationExtendable
{
	void registerAuditConfiguration(IAuditConfiguration auditConfiguration, Class<?> entityType);

	void unregisterAuditConfiguration(IAuditConfiguration auditConfiguration, Class<?> entityType);
}
