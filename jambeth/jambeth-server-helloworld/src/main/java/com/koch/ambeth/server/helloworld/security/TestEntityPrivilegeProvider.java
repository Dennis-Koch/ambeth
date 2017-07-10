package com.koch.ambeth.server.helloworld.security;

import com.koch.ambeth.merge.util.IPrefetchConfig;
import com.koch.ambeth.security.IAuthorization;
import com.koch.ambeth.security.server.privilege.IEntityPermissionRule;
import com.koch.ambeth.security.server.privilege.evaluation.IEntityPermissionEvaluation;
import com.koch.ambeth.server.helloworld.transfer.TestEntity;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.service.model.ISecurityScope;

public class TestEntityPrivilegeProvider implements IEntityPermissionRule<TestEntity> {
	@Override
	public void buildPrefetchConfig(Class<? extends TestEntity> entityType,
			IPrefetchConfig prefetchConfig) {
		// TestEntity.Relation is needed for security checks, this greatly increased security processing
		// with
		// lists of entities, because all necessary valueholders can be initialized with the least
		// possible database
		// roundtrips. Use this feature carefully: Mention exactly what you will need later, nothing
		// more or less.
		prefetchConfig.add(TestEntity.class, "Relation");
	}

	@Override
	public void evaluatePermissionOnInstance(IObjRef objRef, TestEntity entity,
			IAuthorization authorizationManager, ISecurityScope[] securityScopes,
			IEntityPermissionEvaluation permissionEvaluation) {
		permissionEvaluation.allowRead().allowCreate().allowUpdate().denyDelete().denyExecute();
	}
}
