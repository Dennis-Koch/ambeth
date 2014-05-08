package de.osthus.ambeth.service;

import de.osthus.ambeth.model.ISecurityScope;

public interface IServiceFactory
{
	<I> I getService(Class<I> type, ISecurityScope... securityScopes);
}
