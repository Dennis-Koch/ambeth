package com.koch.ambeth.security.privilege;

import java.util.List;

import com.koch.ambeth.security.privilege.model.IPrivilege;
import com.koch.ambeth.security.privilege.model.IPrivilegeResult;
import com.koch.ambeth.security.privilege.model.ITypePrivilege;
import com.koch.ambeth.security.privilege.model.ITypePrivilegeResult;
import com.koch.ambeth.service.merge.model.IObjRef;

public interface IPrivilegeProvider
{
	IPrivilege getPrivilege(Object entity);

	IPrivilege getPrivilegeByObjRef(IObjRef objRef);

	/**
	 * Result correlates by-index with the given objRefs
	 * 
	 * @param objRefs
	 * @param securityScopes
	 * @return
	 */
	IPrivilegeResult getPrivileges(List<?> entities);

	/**
	 * Result correlates by-index with the given objRefs
	 * 
	 * @param objRefs
	 * @param securityScopes
	 * @return
	 */
	IPrivilegeResult getPrivilegesByObjRef(List<? extends IObjRef> objRefs);

	ITypePrivilege getPrivilegeByType(Class<?> entityType);

	ITypePrivilegeResult getPrivilegesByType(List<Class<?>> entityTypes);
}
