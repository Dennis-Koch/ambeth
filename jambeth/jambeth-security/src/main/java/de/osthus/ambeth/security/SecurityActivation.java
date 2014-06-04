package de.osthus.ambeth.security;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.threading.IResultingBackgroundWorkerDelegate;
import de.osthus.ambeth.threading.SensitiveThreadLocal;

public class SecurityActivation implements ISecurityActivation
{
	@SuppressWarnings("unused")
	@LogInstance(SecurityActivation.class)
	private ILogger log;

	protected final ThreadLocal<Boolean> securityActiveTL = new SensitiveThreadLocal<Boolean>();

	protected final ThreadLocal<Boolean> filterActiveTL = new SensitiveThreadLocal<Boolean>();

	@Override
	public boolean isSecured()
	{
		Boolean value = securityActiveTL.get();
		if (value == null)
		{
			return Boolean.TRUE;
		}
		return value.booleanValue();
	}

	@Override
	public boolean isFilterActivated()
	{
		Boolean value = filterActiveTL.get();
		if (value == null)
		{
			return Boolean.TRUE;
		}
		return value.booleanValue();
	}

	@Override
	public <R> R executeWithoutSecurity(IResultingBackgroundWorkerDelegate<R> pausedSecurityRunnable) throws Throwable
	{
		Boolean oldSecurityActive = securityActiveTL.get();
		securityActiveTL.set(Boolean.FALSE);
		try
		{
			return pausedSecurityRunnable.invoke();
		}
		finally
		{
			securityActiveTL.set(oldSecurityActive);
		}
	}

	@Override
	public <R> R executeWithoutFiltering(IResultingBackgroundWorkerDelegate<R> noFilterRunnable) throws Throwable
	{
		Boolean oldFilterActive = filterActiveTL.get();
		filterActiveTL.set(Boolean.FALSE);
		try
		{
			return noFilterRunnable.invoke();
		}
		finally
		{
			filterActiveTL.set(oldFilterActive);
		}
	}
}