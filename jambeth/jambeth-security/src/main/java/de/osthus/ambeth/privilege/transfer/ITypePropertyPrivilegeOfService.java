package de.osthus.ambeth.privilege.transfer;

public interface ITypePropertyPrivilegeOfService
{
	Boolean isCreateAllowed();

	Boolean isReadAllowed();

	Boolean isUpdateAllowed();

	Boolean isDeleteAllowed();
}
