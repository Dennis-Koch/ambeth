package de.osthus.ambeth.security;

import de.osthus.ambeth.model.ISecurityScope;
import de.osthus.ambeth.threading.IResultingBackgroundWorkerDelegate;

public interface ISecurityScopeProvider
{
	ISecurityScope[] getSecurityScopes();

	void setSecurityScopes(ISecurityScope[] securityScopes);

	IUserHandle getUserHandle();

	void setUserHandle(IUserHandle userHandle);

	<R> R executeWithSecurityScopes(IResultingBackgroundWorkerDelegate<R> runnable, ISecurityScope... securityScopes) throws Throwable;
}