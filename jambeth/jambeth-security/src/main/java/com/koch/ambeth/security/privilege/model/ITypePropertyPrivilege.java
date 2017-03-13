package com.koch.ambeth.security.privilege.model;

public interface ITypePropertyPrivilege
{
	Boolean isCreateAllowed();

	Boolean isReadAllowed();

	Boolean isUpdateAllowed();

	Boolean isDeleteAllowed();
}
