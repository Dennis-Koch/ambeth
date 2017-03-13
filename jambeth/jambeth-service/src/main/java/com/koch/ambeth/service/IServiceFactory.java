package com.koch.ambeth.service;

import com.koch.ambeth.service.model.ISecurityScope;

public interface IServiceFactory
{
	<I> I getService(Class<I> type, ISecurityScope... securityScopes);
}
