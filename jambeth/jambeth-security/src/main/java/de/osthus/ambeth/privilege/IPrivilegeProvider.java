package de.osthus.ambeth.privilege;

import java.util.Collection;

import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.privilege.model.IPrivilege;
import de.osthus.ambeth.privilege.model.ITypePrivilege;
import de.osthus.ambeth.privilege.model.ITypePrivilege;

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
	IList<IPrivilege> getPrivileges(Collection<?> entities);

	/**
	 * Result correlates by-index with the given objRefs
	 * 
	 * @param objRefs
	 * @param securityScopes
	 * @return
	 */
	IList<IPrivilege> getPrivilegesByObjRef(Collection<? extends IObjRef> objRefs);

	ITypePrivilege getPrivilegeByType(Class<?> entityType);

	IList<ITypePrivilege> getPrivilegesByType(Collection<Class<?>> entityTypes);
}
