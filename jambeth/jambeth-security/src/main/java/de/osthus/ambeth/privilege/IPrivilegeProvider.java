package de.osthus.ambeth.privilege;

import java.util.List;

import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.privilege.model.IPrivilege;
import de.osthus.ambeth.privilege.model.IPrivilegeResult;
import de.osthus.ambeth.privilege.model.ITypePrivilege;
import de.osthus.ambeth.privilege.model.ITypePrivilegeResult;

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
