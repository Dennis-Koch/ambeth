package de.osthus.ambeth.privilege;

import java.util.Collection;

import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.merge.transfer.ObjRef;
import de.osthus.ambeth.model.ISecurityScope;
import de.osthus.ambeth.privilege.model.IPrivilege;
import de.osthus.ambeth.privilege.model.ITypePrivilege;

public interface IPrivilegeProviderIntern extends IPrivilegeProvider
{
	IPrivilegeCache createPrivilegeCache();

	IPrivilege getPrivilege(Object entity, ISecurityScope[] securityScopes);

	IPrivilege getPrivilegeByObjRef(IObjRef objRef, ISecurityScope[] securityScopes);

	/**
	 * Result correlates by-index with the given objRefs
	 * 
	 * @param objRefs
	 * @param securityScopes
	 * @return
	 */
	IList<IPrivilege> getPrivileges(Collection<?> entities, ISecurityScope[] securityScopes);

	/**
	 * Result correlates by-index with the given objRefs
	 * 
	 * @param objRefs
	 * @param securityScopes
	 * @return
	 */
	IList<IPrivilege> getPrivilegesByObjRef(Collection<? extends IObjRef> objRefs, ISecurityScope[] securityScopes);

	ITypePrivilege getPrivilegeByType(Class<?> entityType, ISecurityScope[] securityScopes);

	IList<ITypePrivilege> getPrivilegesByType(Collection<Class<?>> entityTypes, ISecurityScope[] securityScopes);

	IPrivilege getPrivilegeByObjRef(ObjRef objRef, IPrivilegeCache privilegeCache);

	IList<IPrivilege> getPrivilegesByObjRef(Collection<? extends IObjRef> objRefs, IPrivilegeCache privilegeCache);
}