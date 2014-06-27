package de.osthus.ambeth.security;

import de.osthus.ambeth.threading.IResultingBackgroundWorkerDelegate;

public interface ISecurityActivation
{
	boolean isSecured();

	boolean isFilterActivated();

	boolean setPrivilegedMode(boolean privilegedModeActive);

	void restorePrivilegedMode(boolean previousPrivilegedMode);

	<R> R executeWithoutSecurity(IResultingBackgroundWorkerDelegate<R> pausedSecurityRunnable) throws Throwable;

	<R> R executeWithoutFiltering(IResultingBackgroundWorkerDelegate<R> noFilterRunnable) throws Throwable;
}
