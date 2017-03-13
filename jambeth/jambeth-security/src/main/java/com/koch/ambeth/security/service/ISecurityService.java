package com.koch.ambeth.security.service;

import com.koch.ambeth.service.model.ISecurityScope;
import com.koch.ambeth.service.model.IServiceDescription;
import com.koch.ambeth.util.annotation.XmlType;

@XmlType
public interface ISecurityService
{
	Object callServiceInSecurityScope(ISecurityScope[] securityScopes, IServiceDescription serviceDescription);
}
