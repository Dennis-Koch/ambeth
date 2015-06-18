package de.osthus.ambeth.privilege;

import java.util.List;

import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.merge.transfer.ObjRef;
import de.osthus.ambeth.model.ISecurityScope;
import de.osthus.ambeth.privilege.model.IPrivilege;
import de.osthus.ambeth.privilege.model.IPrivilegeResult;
import de.osthus.ambeth.privilege.model.ITypePrivilege;
import de.osthus.ambeth.privilege.model.ITypePrivilegeResult;

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
	IPrivilegeResult getPrivileges(List<?> entities, ISecurityScope[] securityScopes);

	/**
	 * Result correlates by-index with the given objRefs
	 * 
	 * @param objRefs
	 * @param securityScopes
	 * @return
	 */
	IPrivilegeResult getPrivilegesByObjRef(List<? extends IObjRef> objRefs, ISecurityScope[] securityScopes);

	ITypePrivilege getPrivilegeByType(Class<?> entityType, ISecurityScope[] securityScopes);

	ITypePrivilegeResult getPrivilegesByType(List<Class<?>> entityTypes, ISecurityScope[] securityScopes);

	IPrivilege getPrivilegeByObjRef(ObjRef objRef, IPrivilegeCache privilegeCache);

	IPrivilegeResult getPrivilegesByObjRef(List<? extends IObjRef> objRefs, IPrivilegeCache privilegeCache);
}