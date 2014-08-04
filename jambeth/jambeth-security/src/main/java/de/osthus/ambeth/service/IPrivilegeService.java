package de.osthus.ambeth.service;

import java.util.List;

import de.osthus.ambeth.annotation.XmlType;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.model.ISecurityScope;
import de.osthus.ambeth.privilege.transfer.IPrivilegeOfService;
import de.osthus.ambeth.privilege.transfer.ITypePrivilegeOfService;

@XmlType
public interface IPrivilegeService
{
	List<IPrivilegeOfService> getPrivileges(IObjRef[] oris, ISecurityScope[] securityScopes);

	List<ITypePrivilegeOfService> getPrivilegesOfTypes(Class<?>[] entityTypes, ISecurityScope[] securityScopes);
}
