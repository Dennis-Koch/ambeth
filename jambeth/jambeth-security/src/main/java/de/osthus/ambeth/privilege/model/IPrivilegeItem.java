package de.osthus.ambeth.privilege.model;

public interface IPrivilegeItem
{
	boolean isCreateAllowed();

	boolean isReadAllowed();

	boolean isUpdateAllowed();

	boolean isDeleteAllowed();
}
