package de.osthus.ambeth.privilege;

import de.osthus.ambeth.model.ISecurityScope;
import de.osthus.ambeth.privilege.evaluation.IEntityPermissionEvaluation;
import de.osthus.ambeth.security.IAuthorization;

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
