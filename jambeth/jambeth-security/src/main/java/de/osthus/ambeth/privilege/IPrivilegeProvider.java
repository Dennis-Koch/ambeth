package de.osthus.ambeth.privilege;

import java.util.Collection;

import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.model.ISecurityScope;
import de.osthus.ambeth.privilege.model.IPrivilegeItem;

public interface IPrivilegeProvider
{
	boolean isReadAllowed(Object entity, ISecurityScope... securityScopes);

	boolean isCreateAllowed(Object entity, ISecurityScope... securityScopes);

	boolean isUpdateAllowed(Object entity, ISecurityScope... securityScopes);

	boolean isDeleteAllowed(Object entity, ISecurityScope... securityScopes);

	boolean isExecutionAllowed(Object entity, ISecurityScope... securityScopes);

	IPrivilegeItem getPrivilege(Object entity, ISecurityScope... securityScopes);

	IPrivilegeItem getPrivilegeByObjRef(IObjRef objRef, ISecurityScope... securityScopes);

	/**
	 * Result correlates by-index with the given objRefs
	 * 
	 * @param objRefs
	 * @param securityScopes
	 * @return
	 */
	IList<IPrivilegeItem> getPrivileges(Collection<?> entities, ISecurityScope... securityScopes);

	/**
	 * Result correlates by-index with the given objRefs
	 * 
	 * @param objRefs
	 * @param securityScopes
	 * @return
	 */
	IList<IPrivilegeItem> getPrivilegesByObjRef(Collection<? extends IObjRef> objRefs, ISecurityScope... securityScopes);
}
