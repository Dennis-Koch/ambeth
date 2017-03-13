package com.koch.ambeth.security.service;

import java.util.List;

import com.koch.ambeth.security.privilege.transfer.IPrivilegeOfService;
import com.koch.ambeth.security.privilege.transfer.ITypePrivilegeOfService;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.service.model.ISecurityScope;
import com.koch.ambeth.util.annotation.XmlType;

@XmlType
public interface IPrivilegeService
{
	List<IPrivilegeOfService> getPrivileges(IObjRef[] oris, ISecurityScope[] securityScopes);

	List<ITypePrivilegeOfService> getPrivilegesOfTypes(Class<?>[] entityTypes, ISecurityScope[] securityScopes);
}
