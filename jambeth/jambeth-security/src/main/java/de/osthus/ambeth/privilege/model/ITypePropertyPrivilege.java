package de.osthus.ambeth.privilege.model;

public interface ITypePropertyPrivilege
{
	Boolean isCreateAllowed();

	Boolean isReadAllowed();

	Boolean isUpdateAllowed();

	Boolean isDeleteAllowed();
}
