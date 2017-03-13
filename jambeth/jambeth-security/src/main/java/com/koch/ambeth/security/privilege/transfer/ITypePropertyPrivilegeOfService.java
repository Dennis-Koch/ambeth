package com.koch.ambeth.security.privilege.transfer;

public interface ITypePropertyPrivilegeOfService
{
	Boolean isCreateAllowed();

	Boolean isReadAllowed();

	Boolean isUpdateAllowed();

	Boolean isDeleteAllowed();
}
