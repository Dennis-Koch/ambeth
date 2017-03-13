package com.koch.ambeth.security.privilege;

import java.util.List;

import com.koch.ambeth.merge.transfer.ObjRef;
import com.koch.ambeth.security.privilege.model.IPrivilege;
import com.koch.ambeth.security.privilege.model.IPrivilegeResult;
import com.koch.ambeth.security.privilege.model.ITypePrivilege;
import com.koch.ambeth.security.privilege.model.ITypePrivilegeResult;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.service.model.ISecurityScope;

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