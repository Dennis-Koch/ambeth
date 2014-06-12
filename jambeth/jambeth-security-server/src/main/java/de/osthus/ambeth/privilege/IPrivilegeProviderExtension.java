package de.osthus.ambeth.privilege;

import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.model.ISecurityScope;
import de.osthus.ambeth.privilege.evaluation.IPermissionEvaluation;
import de.osthus.ambeth.privilege.evaluation.IPermissionEvaluationResult;
import de.osthus.ambeth.security.IUserHandle;
import de.osthus.ambeth.util.IPrefetchConfig;

public interface IPrivilegeProviderExtension<T>
{
	/**
	 * This greatly increases security processing with lists of entities, because all necessary valueholders can be initialized with the least possible database
	 * roundtrips. Use this feature carefully: Mention exactly what you will need later, nothing more or less.
	 * 
	 * @param entityType
	 * @param prefetchConfig
	 */
	void buildPrefetchConfig(Class<? extends T> entityType, IPrefetchConfig prefetchConfig);

	IPermissionEvaluationResult evaluatePermission(IObjRef objRef, T entity, IUserHandle userHandle, ISecurityScope[] securityScopes,
			IPermissionEvaluation permissionEvaluation);
}
