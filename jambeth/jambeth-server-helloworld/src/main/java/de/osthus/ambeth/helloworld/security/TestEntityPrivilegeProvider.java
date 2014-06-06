package de.osthus.ambeth.helloworld.security;

import de.osthus.ambeth.helloworld.transfer.TestEntity;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.model.ISecurityScope;
import de.osthus.ambeth.privilege.IPrivilegeProviderExtension;
import de.osthus.ambeth.privilege.evaluation.IPermissionEvaluation;
import de.osthus.ambeth.security.IUserHandle;
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
	public void evaluatePermission(IObjRef objRef, Object entity, IUserHandle userHandle, ISecurityScope[] securityScopes,
			IPermissionEvaluation permissionEvaluation)
	{
		permissionEvaluation.allowCreate().allowRead().allowUpdate().denyDelete();
	}
}
