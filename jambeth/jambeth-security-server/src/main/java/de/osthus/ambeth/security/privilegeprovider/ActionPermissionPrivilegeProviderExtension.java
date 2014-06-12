package de.osthus.ambeth.security.privilegeprovider;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.model.ISecurityScope;
import de.osthus.ambeth.privilege.IPrivilegeProviderExtension;
import de.osthus.ambeth.privilege.evaluation.IPermissionEvaluation;
import de.osthus.ambeth.privilege.evaluation.IPermissionEvaluationResult;
import de.osthus.ambeth.security.IActionPermission;
import de.osthus.ambeth.security.IUserHandle;
import de.osthus.ambeth.util.IPrefetchConfig;

public class ActionPermissionPrivilegeProviderExtension implements IPrivilegeProviderExtension<IActionPermission>
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
	public IPermissionEvaluationResult evaluatePermission(IObjRef objRef, IActionPermission entity, IUserHandle userHandle, ISecurityScope[] securityScopes,
			IPermissionEvaluation permissionEvaluation)
	{
		if (!userHandle.hasActionPermission(entity.getName(), securityScopes))
		{
			// this extension only handles the specific case where the user has the corresponding actionPermission associated
			return permissionEvaluation.skipEach();
		}
		// the association implies execution permission (no CUD operations) - these have to be handled by another extension
		return permissionEvaluation.allowRead().skipCreate().skipUpdate().skipDelete().allowExecute();
	}
}
