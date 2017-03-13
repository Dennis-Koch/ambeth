package com.koch.ambeth.security;

import com.koch.ambeth.util.threading.IResultingBackgroundWorkerDelegate;

public interface ISecurityContextHolder
{
	ISecurityContext getContext();

	ISecurityContext getCreateContext();

	void clearContext();

	<R> R setScopedAuthentication(IAuthentication authentication, IResultingBackgroundWorkerDelegate<R> runnableScope) throws Throwable;
}