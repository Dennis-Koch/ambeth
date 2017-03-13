package com.koch.ambeth.merge.security;

import com.koch.ambeth.service.model.ISecurityScope;
import com.koch.ambeth.util.threading.IBackgroundWorkerDelegate;
import com.koch.ambeth.util.threading.IBackgroundWorkerParamDelegate;
import com.koch.ambeth.util.threading.IResultingBackgroundWorkerDelegate;
import com.koch.ambeth.util.threading.IResultingBackgroundWorkerParamDelegate;

public interface ISecurityScopeProvider
{
	ISecurityScope[] getSecurityScopes();

	void setSecurityScopes(ISecurityScope[] securityScopes);

	<R> R executeWithSecurityScopes(IResultingBackgroundWorkerDelegate<R> runnable, ISecurityScope... securityScopes) throws Throwable;

	<R, V> R executeWithSecurityScopes(IResultingBackgroundWorkerParamDelegate<R, V> runnable, V state, ISecurityScope... securityScopes) throws Throwable;

	void executeWithSecurityScopes(IBackgroundWorkerDelegate runnable, ISecurityScope... securityScopes) throws Throwable;

	<V> void executeWithSecurityScopes(IBackgroundWorkerParamDelegate<V> runnable, V state, ISecurityScope... securityScopes) throws Throwable;
}