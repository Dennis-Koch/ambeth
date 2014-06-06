package de.osthus.ambeth.privilege.model;

import de.osthus.ambeth.annotation.XmlType;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.model.ISecurityScope;

@XmlType
public interface IPrivilegeResult
{
	IObjRef getReference();

	ISecurityScope getSecurityScope();

	PrivilegeEnum[] getPrivileges();
}
