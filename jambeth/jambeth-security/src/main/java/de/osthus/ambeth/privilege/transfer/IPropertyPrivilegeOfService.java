package de.osthus.ambeth.privilege.transfer;

public interface IPropertyPrivilegeOfService
{
	boolean isCreateAllowed();

	boolean isReadAllowed();

	boolean isUpdateAllowed();

	boolean isDeleteAllowed();
}
