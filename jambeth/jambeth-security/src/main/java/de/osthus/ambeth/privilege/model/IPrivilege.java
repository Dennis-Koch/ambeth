package de.osthus.ambeth.privilege.model;

public interface IPrivilege
{
	boolean isCreateAllowed();

	boolean isReadAllowed();

	boolean isUpdateAllowed();

	boolean isDeleteAllowed();

	boolean isExecuteAllowed();

	IPropertyPrivilege getDefaultPropertyPrivilegeIfValid();

	IPropertyPrivilege getPrimitivePropertyPrivilege(int primitiveIndex);

	IPropertyPrivilege getRelationPropertyPrivilege(int relationIndex);
}
