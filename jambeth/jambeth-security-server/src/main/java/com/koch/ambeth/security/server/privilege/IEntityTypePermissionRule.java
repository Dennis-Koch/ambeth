package com.koch.ambeth.security.server.privilege;

import com.koch.ambeth.security.IAuthorization;
import com.koch.ambeth.security.server.privilege.evaluation.IEntityPermissionEvaluation;
import com.koch.ambeth.service.model.ISecurityScope;

public interface IEntityTypePermissionRule extends IPermissionRule
{
	/**
	 * Use this to implement per-entity-type security (in SQL-terminology: table-level-security) and/or per-property security (in SQL: column-level security)
	 * 
	 * @param entityType
	 * @param currentUser
	 * @param securityScopes
	 * @param pe
	 */
	void evaluatePermissionOnType(Class<?> entityType, IAuthorization currentUser, ISecurityScope[] securityScopes, IEntityPermissionEvaluation pe);
}
