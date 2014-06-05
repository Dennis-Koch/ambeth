package de.osthus.ambeth.privilege;

import de.osthus.ambeth.model.ISecurityScope;
import de.osthus.ambeth.util.IPrefetchConfig;

public interface IPrivilegeProviderExtension
{
	/**
	 * This greatly increases security processing with lists of entities, because all necessary valueholders can be initialized with the least possible database
	 * roundtrips. Use this feature carefully: Mention exactly what you will need later, nothing more or less.
	 * 
	 * @param entityType
	 * @param prefetchConfig
	 */
	void buildPrefetchConfig(Class<?> entityType, IPrefetchConfig prefetchConfig);

	boolean isReadAllowed(Object entity, ISecurityScope securityScope);

	boolean isCreateAllowed(Object entity, ISecurityScope securityScope);

	boolean isUpdateAllowed(Object entity, ISecurityScope securityScope);

	boolean isDeleteAllowed(Object entity, ISecurityScope securityScope);
}
