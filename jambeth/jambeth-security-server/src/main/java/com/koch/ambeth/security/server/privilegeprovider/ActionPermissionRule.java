package com.koch.ambeth.security.server.privilegeprovider;

import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.merge.util.IPrefetchConfig;
import com.koch.ambeth.security.IActionPermission;
import com.koch.ambeth.security.IAuthorization;
import com.koch.ambeth.security.server.privilege.IEntityPermissionRule;
import com.koch.ambeth.security.server.privilege.evaluation.IEntityPermissionEvaluation;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.service.model.ISecurityScope;

public class ActionPermissionRule implements IEntityPermissionRule<IActionPermission>
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	public void buildPrefetchConfig(Class<? extends IActionPermission> entityType, IPrefetchConfig prefetchConfig)
	{
		// intended blank
	}

	@Override
	public void evaluatePermissionOnInstance(IObjRef objRef, IActionPermission entity, IAuthorization authorization, ISecurityScope[] securityScopes,
			IEntityPermissionEvaluation pe)
	{
		if (!authorization.hasActionPermission(entity.getName(), securityScopes))
		{
			// this extension only handles the specific case where the user has the corresponding actionPermission associated
			pe.allowRead().skipCUD().denyExecute();
			return;
		}
		// the association implies execution permission (no CUD operations) - these have to be handled by another extension
		pe.allowRead().skipCUD().allowExecute();
		return;
	}
}
