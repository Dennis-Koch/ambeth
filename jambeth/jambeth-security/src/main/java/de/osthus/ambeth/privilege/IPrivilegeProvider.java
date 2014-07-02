package de.osthus.ambeth.privilege;

import java.util.Collection;

import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.model.ISecurityScope;
import de.osthus.ambeth.privilege.model.IPrivilege;

public interface IPrivilegeProvider
{
	boolean isReadAllowed(Object entity, ISecurityScope... securityScopes);

	boolean isCreateAllowed(Object entity, ISecurityScope... securityScopes);

	boolean isUpdateAllowed(Object entity, ISecurityScope... securityScopes);

	boolean isDeleteAllowed(Object entity, ISecurityScope... securityScopes);

	boolean isExecutionAllowed(Object entity, ISecurityScope... securityScopes);

	IPrivilege getPrivilege(Object entity, ISecurityScope... securityScopes);

	IPrivilege getPrivilegeByObjRef(IObjRef objRef, ISecurityScope... securityScopes);

	/**
	 * Result correlates by-index with the given objRefs
	 * 
	 * @param objRefs
	 * @param securityScopes
	 * @return
	 */
	IList<IPrivilege> getPrivileges(Collection<?> entities, ISecurityScope... securityScopes);

	/**
	 * Result correlates by-index with the given objRefs
	 * 
	 * @param objRefs
	 * @param securityScopes
	 * @return
	 */
	IList<IPrivilege> getPrivilegesByObjRef(Collection<? extends IObjRef> objRefs, ISecurityScope... securityScopes);

	IPrivilege getPrivilegeByType(Class<?> entityType, ISecurityScope... securityScopes);

	IList<IPrivilege> getPrivilegesByType(Collection<Class<?>> entityTypes, ISecurityScope... securityScopes);
}
