package de.osthus.ambeth.privilege;

import java.util.List;

import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.model.ISecurityScope;
import de.osthus.ambeth.util.IPrefetchConfig;

public interface IPrivilegeProvider
{
	void buildPrefetchConfig(Class<?> entityType, IPrefetchConfig prefetchConfig);

	boolean isReadAllowed(Object entity, ISecurityScope... securityScopes);

	boolean isCreateAllowed(Object entity, ISecurityScope... securityScopes);

	boolean isUpdateAllowed(Object entity, ISecurityScope... securityScopes);

	boolean isDeleteAllowed(Object entity, ISecurityScope... securityScopes);

	IPrivilegeItem getPrivileges(Object entity, ISecurityScope... securityScopes);

	IList<IPrivilegeItem> getPrivileges(List<IObjRef> objRefs, ISecurityScope... securityScopes);
}
