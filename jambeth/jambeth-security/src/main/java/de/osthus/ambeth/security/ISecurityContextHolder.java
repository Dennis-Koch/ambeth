package de.osthus.ambeth.security;

import de.osthus.ambeth.threading.IResultingBackgroundWorkerDelegate;

public interface ISecurityContextHolder
{
	ISecurityContext getContext();

	ISecurityContext getCreateContext();

	void clearContext();

	<R> R setScopedAuthentication(IAuthentication authentication, IResultingBackgroundWorkerDelegate<R> runnableScope) throws Throwable;
}