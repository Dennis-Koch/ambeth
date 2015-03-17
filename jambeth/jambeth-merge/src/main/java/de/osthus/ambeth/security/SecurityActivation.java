package de.osthus.ambeth.security;

import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.ioc.threadlocal.Forkable;
import de.osthus.ambeth.ioc.threadlocal.IThreadLocalCleanupBean;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.config.MergeConfigurationConstants;
import de.osthus.ambeth.threading.IBackgroundWorkerDelegate;
import de.osthus.ambeth.threading.IResultingBackgroundWorkerDelegate;
import de.osthus.ambeth.threading.SensitiveThreadLocal;

public class SecurityActivation implements ISecurityActivation, IThreadLocalCleanupBean
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Forkable
	protected final ThreadLocal<Boolean> securityActiveTL = new SensitiveThreadLocal<Boolean>();

	@Forkable
	protected final ThreadLocal<Boolean> filterActiveTL = new SensitiveThreadLocal<Boolean>();

	@Property(name = MergeConfigurationConstants.SecurityActive, defaultValue = "false")
	protected boolean securityActive;

	@Override
	public void cleanupThreadLocal()
	{
		securityActiveTL.remove();
		filterActiveTL.remove();
	}

	@Override
	public boolean isSecured()
	{
		Boolean value = securityActiveTL.get();
		if (value == null)
		{
			return securityActive;
		}
		return value.booleanValue();
	}

	@Override
	public boolean isFilterActivated()
	{
		if (!isSecured())
		{
			return Boolean.FALSE;
		}
		Boolean value = filterActiveTL.get();
		if (value == null)
		{
			return true;
		}
		return value.booleanValue();
	}

	@Override
	public void executeWithoutSecurity(IBackgroundWorkerDelegate pausedSecurityRunnable) throws Throwable
	{
		Boolean oldSecurityActive = securityActiveTL.get();
		securityActiveTL.set(Boolean.FALSE);
		try
		{
			pausedSecurityRunnable.invoke();
		}
		finally
		{
			securityActiveTL.set(oldSecurityActive);
		}
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
	public void executeWithoutFiltering(IBackgroundWorkerDelegate noFilterRunnable) throws Throwable
	{
		Boolean oldFilterActive = filterActiveTL.get();
		filterActiveTL.set(Boolean.FALSE);
		try
		{
			noFilterRunnable.invoke();
		}
		finally
		{
			filterActiveTL.set(oldFilterActive);
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
