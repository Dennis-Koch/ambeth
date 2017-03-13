package com.koch.ambeth.security.privilege.model;

public interface IPropertyPrivilege
{
	boolean isCreateAllowed();

	boolean isReadAllowed();

	boolean isUpdateAllowed();

	boolean isDeleteAllowed();
}
