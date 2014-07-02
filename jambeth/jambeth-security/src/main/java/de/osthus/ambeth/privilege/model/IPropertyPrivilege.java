package de.osthus.ambeth.privilege.model;

public interface IPropertyPrivilege
{
	boolean isCreateAllowed();

	boolean isReadAllowed();

	boolean isUpdateAllowed();

	boolean isDeleteAllowed();
}
