package de.osthus.ambeth.security;

import java.util.Set;

import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.ioc.threadlocal.Forkable;
import de.osthus.ambeth.ioc.threadlocal.IThreadLocalCleanupBean;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.config.MergeConfigurationConstants;
import de.osthus.ambeth.threading.IBackgroundWorkerDelegate;
import de.osthus.ambeth.threading.IResultingBackgroundWorkerDelegate;

public class SecurityActivation implements ISecurityActivation, IThreadLocalCleanupBean
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Forkable
	protected final ThreadLocal<Boolean> serviceActiveTL = new ThreadLocal<Boolean>();

	@Forkable
	protected final ThreadLocal<Boolean> securityActiveTL = new ThreadLocal<Boolean>();

	@Forkable
	protected final ThreadLocal<Boolean> entityActiveTL = new ThreadLocal<Boolean>();

	@Property(name = MergeConfigurationConstants.SecurityActive, defaultValue = "false")
	protected boolean securityActive;

	@Override
	public void cleanupThreadLocal()
	{
		if (securityActiveTL.get() != null || entityActiveTL.get() != null || serviceActiveTL.get() != null)
		{
			throw new IllegalStateException("Must be null at this point");
		}
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
		return isEntitySecurityEnabled();
	}

	public boolean isEntitySecurityEnabled()
	{
		if (!securityActive)
		{
			return false;
		}
		Boolean value = securityActiveTL.get();
		if (Boolean.FALSE.equals(value))
		{
			return false;
		}
		value = entityActiveTL.get();
		if (value != null)
		{
			return value.booleanValue();
		}
		return true;
	}

	@Override
	public boolean isServiceSecurityEnabled()
	{
		if (!securityActive)
		{
			return false;
		}
		Boolean value = securityActiveTL.get();
		if (Boolean.FALSE.equals(value))
		{
			return false;
		}
		value = serviceActiveTL.get();
		if (value != null)
		{
			return value.booleanValue();
		}
		return true;
	}

	public boolean isServiceOrEntitySecurityEnabled()
	{
		return isEntitySecurityEnabled() || isServiceSecurityEnabled();
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
		Boolean oldFilterActive = entityActiveTL.get();
		entityActiveTL.set(Boolean.FALSE);
		try
		{
			noFilterRunnable.invoke();
		}
		finally
		{
			entityActiveTL.set(oldFilterActive);
		}
	}

	@Override
	public <R> R executeWithoutFiltering(IResultingBackgroundWorkerDelegate<R> noFilterRunnable) throws Throwable
	{
		Boolean oldFilterActive = entityActiveTL.get();
		entityActiveTL.set(Boolean.FALSE);
		try
		{
			return noFilterRunnable.invoke();
		}
		finally
		{
			entityActiveTL.set(oldFilterActive);
		}
	}

	@Override
	public void executeWithSecurityDirective(Set<SecurityDirective> securityDirective, IBackgroundWorkerDelegate runnable) throws Throwable
	{
		Boolean entityActive = securityDirective.contains(SecurityDirective.DISABLE_ENTITY_CHECK) ? Boolean.FALSE : securityDirective
				.contains(SecurityDirective.ENABLE_ENTITY_CHECK) ? Boolean.TRUE : null;
		Boolean serviceActive = securityDirective.contains(SecurityDirective.DISABLE_SERVICE_CHECK) ? Boolean.FALSE : securityDirective
				.contains(SecurityDirective.ENABLE_SERVICE_CHECK) ? Boolean.TRUE : null;
		Boolean oldEntityActive = null, oldServiceActive = null;
		if (entityActive != null)
		{
			oldEntityActive = entityActiveTL.get();
			entityActiveTL.set(entityActive);
		}
		try
		{
			if (serviceActive != null)
			{
				oldServiceActive = serviceActiveTL.get();
				serviceActiveTL.set(serviceActive);
			}
			try
			{
				runnable.invoke();
				return;
			}
			finally
			{
				if (serviceActive != null)
				{
					serviceActiveTL.set(oldServiceActive);
				}
			}
		}
		finally
		{
			if (entityActive != null)
			{
				entityActiveTL.set(oldEntityActive);
			}
		}
	}

	@Override
	public <R> R executeWithSecurityDirective(Set<SecurityDirective> securityDirective, IResultingBackgroundWorkerDelegate<R> runnable) throws Throwable
	{
		Boolean entityActive = securityDirective.contains(SecurityDirective.DISABLE_ENTITY_CHECK) ? Boolean.FALSE : securityDirective
				.contains(SecurityDirective.ENABLE_ENTITY_CHECK) ? Boolean.TRUE : null;
		Boolean serviceActive = securityDirective.contains(SecurityDirective.DISABLE_SERVICE_CHECK) ? Boolean.FALSE : securityDirective
				.contains(SecurityDirective.ENABLE_SERVICE_CHECK) ? Boolean.TRUE : null;
		Boolean oldEntityActive = null, oldServiceActive = null;
		if (entityActive != null)
		{
			oldEntityActive = entityActiveTL.get();
			entityActiveTL.set(entityActive);
		}
		try
		{
			if (serviceActive != null)
			{
				oldServiceActive = serviceActiveTL.get();
				serviceActiveTL.set(serviceActive);
			}
			try
			{
				return runnable.invoke();
			}
			finally
			{
				if (serviceActive != null)
				{
					serviceActiveTL.set(oldServiceActive);
				}
			}
		}
		finally
		{
			if (entityActive != null)
			{
				entityActiveTL.set(oldEntityActive);
			}
		}
	}

}
