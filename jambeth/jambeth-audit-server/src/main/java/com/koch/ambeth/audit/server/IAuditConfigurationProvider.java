package com.koch.ambeth.audit.server;

public interface IAuditConfigurationProvider
{
	IAuditConfiguration getAuditConfiguration(Class<?> entityType);
}
