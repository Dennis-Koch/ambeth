package de.osthus.ambeth.privilege.model;

public interface IPrivilege
{
	boolean isCreateAllowed();

	boolean isReadAllowed();

	boolean isUpdateAllowed();

	boolean isDeleteAllowed();

	boolean isExecutionAllowed();

	String[] getConfiguredPropertyNames();

	IPropertyPrivilege getPropertyPrivilege(String propertyName);
}
