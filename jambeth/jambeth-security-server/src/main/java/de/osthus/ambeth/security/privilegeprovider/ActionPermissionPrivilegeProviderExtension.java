package de.osthus.ambeth.security.privilegeprovider;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.model.ISecurityScope;
import de.osthus.ambeth.privilege.IPrivilegeProviderExtension;
import de.osthus.ambeth.privilege.evaluation.IEntityPermissionEvaluation;
import de.osthus.ambeth.security.IActionPermission;
import de.osthus.ambeth.security.IAuthorization;
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

	@Override
	public void evaluatePermissionOnType(Class<? extends IActionPermission> entityType, IAuthorization currentUser, ISecurityScope[] securityScopes,
			IEntityPermissionEvaluation pe)
	{
		pe.allowRead();
	}
}
