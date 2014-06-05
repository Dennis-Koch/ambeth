package de.osthus.ambeth.helloworld.security;

import de.osthus.ambeth.helloworld.transfer.TestEntity;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.model.ISecurityScope;
import de.osthus.ambeth.privilege.IPrivilegeProviderExtension;
import de.osthus.ambeth.util.IPrefetchConfig;

public class TestEntityPrivilegeProvider implements IPrivilegeProviderExtension
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	public void buildPrefetchConfig(Class<?> entityType, IPrefetchConfig prefetchConfig)
	{
		// TestEntity.Relation is needed for security checks, this greatly increased security processing with
		// lists of entities, because all necessary valueholders can be initialized with the least possible database
		// roundtrips. Use this feature carefully: Mention exactly what you will need later, nothing more or less.
		prefetchConfig.add(TestEntity.class, "Relation");
	}

	@Override
	public boolean isCreateAllowed(Object entity, ISecurityScope securityScopes)
	{
		return true;
	}

	@Override
	public boolean isDeleteAllowed(Object entity, ISecurityScope securityScopes)
	{
		return false;
	}

	@Override
	public boolean isReadAllowed(Object entity, ISecurityScope securityScopes)
	{
		return true;
	}

	@Override
	public boolean isUpdateAllowed(Object entity, ISecurityScope securityScopes)
	{
		return true;
	}
}
