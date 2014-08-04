package de.osthus.ambeth.privilege.model;

public interface ITypePrivilege
{
	Boolean isCreateAllowed();

	Boolean isReadAllowed();

	Boolean isUpdateAllowed();

	Boolean isDeleteAllowed();

	Boolean isExecuteAllowed();

	ITypePropertyPrivilege getDefaultPropertyPrivilegeIfValid();

	ITypePropertyPrivilege getPrimitivePropertyPrivilege(int primitiveIndex);

	ITypePropertyPrivilege getRelationPropertyPrivilege(int relationIndex);
}
