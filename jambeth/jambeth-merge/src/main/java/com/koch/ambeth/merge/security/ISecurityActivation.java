package com.koch.ambeth.merge.security;

import java.util.Set;

import com.koch.ambeth.util.threading.IBackgroundWorkerDelegate;
import com.koch.ambeth.util.threading.IResultingBackgroundWorkerDelegate;

public interface ISecurityActivation
{
	boolean isSecured();

	boolean isServiceSecurityEnabled();

	boolean isFilterActivated();

	void executeWithSecurityDirective(Set<SecurityDirective> securityDirective, IBackgroundWorkerDelegate runnable) throws Throwable;

	<R> R executeWithSecurityDirective(Set<SecurityDirective> securityDirective, IResultingBackgroundWorkerDelegate<R> runnable) throws Throwable;

	void executeWithoutSecurity(IBackgroundWorkerDelegate pausedSecurityRunnable) throws Throwable;

	<R> R executeWithoutSecurity(IResultingBackgroundWorkerDelegate<R> pausedSecurityRunnable) throws Throwable;

	void executeWithoutFiltering(IBackgroundWorkerDelegate noFilterRunnable) throws Throwable;

	<R> R executeWithoutFiltering(IResultingBackgroundWorkerDelegate<R> noFilterRunnable) throws Throwable;
}
