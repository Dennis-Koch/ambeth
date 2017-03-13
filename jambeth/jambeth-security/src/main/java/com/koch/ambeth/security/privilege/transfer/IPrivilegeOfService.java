package com.koch.ambeth.security.privilege.transfer;

import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.service.model.ISecurityScope;
import com.koch.ambeth.util.annotation.XmlType;

@XmlType
public interface IPrivilegeOfService
{
	IObjRef getReference();

	ISecurityScope getSecurityScope();

	boolean isCreateAllowed();

	boolean isReadAllowed();

	boolean isUpdateAllowed();

	boolean isDeleteAllowed();

	boolean isExecuteAllowed();

	String[] getPropertyPrivilegeNames();

	IPropertyPrivilegeOfService[] getPropertyPrivileges();
}
