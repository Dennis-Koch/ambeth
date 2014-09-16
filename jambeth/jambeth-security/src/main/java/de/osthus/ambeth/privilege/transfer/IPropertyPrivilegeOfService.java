package de.osthus.ambeth.privilege.transfer;

import de.osthus.ambeth.annotation.XmlType;

@XmlType
public interface IPropertyPrivilegeOfService
{
	boolean isCreateAllowed();

	boolean isReadAllowed();

	boolean isUpdateAllowed();

	boolean isDeleteAllowed();
}
