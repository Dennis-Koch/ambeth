package de.osthus.ambeth.privilege.transfer;

import de.osthus.ambeth.annotation.XmlType;
import de.osthus.ambeth.model.ISecurityScope;

@XmlType
public interface ITypePrivilegeOfService
{
	Class<?> getEntityType();

	ISecurityScope getSecurityScope();

	Boolean isCreateAllowed();

	Boolean isReadAllowed();

	Boolean isUpdateAllowed();

	Boolean isDeleteAllowed();

	Boolean isExecuteAllowed();

	String[] getPropertyPrivilegeNames();

	ITypePropertyPrivilegeOfService[] getPropertyPrivileges();
}
