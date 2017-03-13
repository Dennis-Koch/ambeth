package com.koch.ambeth.security.privilege.transfer;

import com.koch.ambeth.service.model.ISecurityScope;
import com.koch.ambeth.util.annotation.XmlType;

@XmlType
public interface ITypePrivilegeOfService
{
	Class<?> getEntityType();

	ISecurityScope getSecurityScope();

	Boolean isCreateAllowed();

	Boolean isReadAllowed();

	Boolean isUpdateAllowed();

	Boolean isDeleteAllowed();

	Boolean isExecuteAllowed();

	String[] getPropertyPrivilegeNames();

	ITypePropertyPrivilegeOfService[] getPropertyPrivileges();
}
