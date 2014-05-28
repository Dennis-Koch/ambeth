package de.osthus.ambeth.service;

import de.osthus.ambeth.annotation.XmlType;
import de.osthus.ambeth.model.ISecurityScope;
import de.osthus.ambeth.model.IServiceDescription;

@XmlType
public interface ISecurityService
{
	Object callServiceInSecurityScope(ISecurityScope[] securityScopes, IServiceDescription serviceDescription);
}
