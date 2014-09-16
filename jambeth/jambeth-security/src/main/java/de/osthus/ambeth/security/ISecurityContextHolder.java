package de.osthus.ambeth.security;

import de.osthus.ambeth.threading.IResultingBackgroundWorkerDelegate;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;

public interface ISecurityContextHolder
{

	public abstract ISecurityContext getContext();

	public abstract ISecurityContext getCreateContext();

	public abstract void clearContext();

	public abstract <R> R setScopedAuthentication(IAuthentication authentication, IResultingBackgroundWorkerDelegate<R> runnableScope) throws Throwable;

}