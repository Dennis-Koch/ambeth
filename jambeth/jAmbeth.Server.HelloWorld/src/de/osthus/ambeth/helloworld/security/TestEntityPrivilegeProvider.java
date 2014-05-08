package de.osthus.ambeth.helloworld.security;

import java.util.List;

import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.helloworld.transfer.TestEntity;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.model.ISecurityScope;
import de.osthus.ambeth.privilege.IPrivilegeItem;
import de.osthus.ambeth.privilege.IPrivilegeProvider;
import de.osthus.ambeth.util.IPrefetchConfig;

public class TestEntityPrivilegeProvider implements IPrivilegeProvider, IInitializingBean
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
	}

	@Override
	public IList<IPrivilegeItem> getPrivileges(List<IObjRef> objRefs, ISecurityScope... securityScopes)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public IPrivilegeItem getPrivileges(Object entity, ISecurityScope... securityScopes)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void buildPrefetchConfig(Class<?> entityType, IPrefetchConfig prefetchConfig)
	{
		// TestEntity.Relation is needed for security checks, this greatly increased security processing with
		// lists of entities, because all necessary valueholders can be initialized with the least possible database
		// roundtrips. Use this feature carefully: Mention exactly what you will need later, nothing more or less.
		prefetchConfig.add(TestEntity.class, "Relation");
	}

	@Override
	public boolean isCreateAllowed(Object entity, ISecurityScope... securityScopes)
	{
		return true;
	}

	@Override
	public boolean isDeleteAllowed(Object entity, ISecurityScope... securityScopes)
	{
		return false;
	}

	@Override
	public boolean isReadAllowed(Object entity, ISecurityScope... securityScopes)
	{
		return true;
	}

	@Override
	public boolean isUpdateAllowed(Object entity, ISecurityScope... securityScopes)
	{
		return true;
	}
}
