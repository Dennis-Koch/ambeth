package de.osthus.ambeth.privilege.transfer;

import de.osthus.ambeth.annotation.XmlType;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.model.ISecurityScope;

@XmlType
public interface IPrivilegeOfService
{
	IObjRef getReference();

	ISecurityScope getSecurityScope();

	boolean isCreateAllowed();

	boolean isReadAllowed();

	boolean isUpdateAllowed();

	boolean isDeleteAllowed();

	boolean isExecutionAllowed();

	String[] getPropertyPrivilegeNames();

	IPropertyPrivilegeOfService[] getPropertyPrivileges();
}
