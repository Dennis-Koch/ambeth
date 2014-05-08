package de.osthus.ambeth.privilege;

public interface IPrivilegeItem
{
	boolean isCreateAllowed();

	boolean isUpdateAllowed();

	boolean isDeleteAllowed();

	boolean isReadAllowed();
}
