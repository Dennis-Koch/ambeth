package de.osthus.ambeth.security;

import de.osthus.ambeth.threading.IBackgroundWorkerDelegate;
import de.osthus.ambeth.threading.IResultingBackgroundWorkerDelegate;

public interface ISecurityActivation
{
	boolean isSecured();

	boolean isFilterActivated();

	void executeWithoutSecurity(IBackgroundWorkerDelegate pausedSecurityRunnable) throws Throwable;

	<R> R executeWithoutSecurity(IResultingBackgroundWorkerDelegate<R> pausedSecurityRunnable) throws Throwable;

	void executeWithoutFiltering(IBackgroundWorkerDelegate noFilterRunnable) throws Throwable;

	<R> R executeWithoutFiltering(IResultingBackgroundWorkerDelegate<R> noFilterRunnable) throws Throwable;
}
