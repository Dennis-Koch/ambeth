package com.koch.ambeth.security.privilege.transfer;

import com.koch.ambeth.util.annotation.XmlType;

@XmlType
public interface IPropertyPrivilegeOfService
{
	boolean isCreateAllowed();

	boolean isReadAllowed();

	boolean isUpdateAllowed();

	boolean isDeleteAllowed();
}
