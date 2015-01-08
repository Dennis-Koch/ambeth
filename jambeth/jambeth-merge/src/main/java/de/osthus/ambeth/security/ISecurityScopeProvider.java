package de.osthus.ambeth.security;

import de.osthus.ambeth.model.ISecurityScope;
import de.osthus.ambeth.threading.IBackgroundWorkerDelegate;
import de.osthus.ambeth.threading.IBackgroundWorkerParamDelegate;
import de.osthus.ambeth.threading.IResultingBackgroundWorkerDelegate;
import de.osthus.ambeth.threading.IResultingBackgroundWorkerParamDelegate;

public interface ISecurityScopeProvider
{
	ISecurityScope[] getSecurityScopes();

	void setSecurityScopes(ISecurityScope[] securityScopes);

	<R> R executeWithSecurityScopes(IResultingBackgroundWorkerDelegate<R> runnable, ISecurityScope... securityScopes) throws Throwable;

	<R, V> R executeWithSecurityScopes(IResultingBackgroundWorkerParamDelegate<R, V> runnable, V state, ISecurityScope... securityScopes) throws Throwable;

	void executeWithSecurityScopes(IBackgroundWorkerDelegate runnable, ISecurityScope... securityScopes) throws Throwable;

	<V> void executeWithSecurityScopes(IBackgroundWorkerParamDelegate<V> runnable, V state, ISecurityScope... securityScopes) throws Throwable;
}