package de.osthus.ambeth.privilege.service;

import java.util.List;

import de.osthus.ambeth.annotation.XmlType;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.model.ISecurityScope;
import de.osthus.ambeth.privilege.transfer.PrivilegeResult;

@XmlType
public interface IPrivilegeService
{
	List<PrivilegeResult> getPrivileges(IObjRef[] oris, ISecurityScope[] securityScopes);
}
