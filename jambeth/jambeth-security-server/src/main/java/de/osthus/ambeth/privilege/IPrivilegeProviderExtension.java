package de.osthus.ambeth.privilege;

import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.model.ISecurityScope;
import de.osthus.ambeth.privilege.evaluation.IEntityPermissionEvaluation;
import de.osthus.ambeth.security.IAuthorization;
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

	/**
	 * Use this to implement per-entity-instance security (in SQL-terminology: row-level-security) and/or per-property-instance security (in SQL:
	 * cell-level-security)
	 * 
	 * @param objRef
	 * @param entity
	 * @param currentUser
	 * @param securityScopes
	 * @param pe
	 */
	void evaluatePermissionOnInstance(IObjRef objRef, T entity, IAuthorization currentUser, ISecurityScope[] securityScopes, IEntityPermissionEvaluation pe);

	/**
	 * Use this to implement per-entity-type security (in SQL-terminology: table-level-security) and/or per-property security (in SQL: column-level security)
	 * 
	 * @param entityType
	 * @param currentUser
	 * @param securityScopes
	 * @param pe
	 */
	void evaluatePermissionOnType(Class<? extends T> entityType, IAuthorization currentUser, ISecurityScope[] securityScopes, IEntityPermissionEvaluation pe);
}
