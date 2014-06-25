package de.osthus.ambeth.security;

import de.osthus.ambeth.model.ISecurityScope;
import de.osthus.ambeth.threading.IResultingBackgroundWorkerDelegate;

public interface ISecurityScopeProvider
{
	ISecurityScope[] getSecurityScopes();

	void setSecurityScopes(ISecurityScope[] securityScopes);

	IAuthorization getAuthorization();

	void setAuthorization(IAuthorization authorization);

	<R> R executeWithSecurityScopes(IResultingBackgroundWorkerDelegate<R> runnable, ISecurityScope... securityScopes) throws Throwable;
}