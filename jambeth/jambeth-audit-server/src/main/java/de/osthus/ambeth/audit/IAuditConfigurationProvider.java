package de.osthus.ambeth.audit;

public interface IAuditConfigurationProvider
{
	IAuditConfiguration getAuditConfiguration(Class<?> entityType);
}
