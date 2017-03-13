package com.koch.ambeth.service;

import com.koch.ambeth.ioc.DefaultExtendableContainer;
import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.ioc.extendable.IExtendableContainer;
import com.koch.ambeth.service.config.ServiceConfigurationConstants;

public abstract class AbstractOfflineServiceUrlProvider implements IServiceUrlProvider, IOfflineListenerExtendable, IInitializingBean
{
	protected boolean isOffline;

	@Override
	public boolean isOffline()
	{
		return isOffline;
	}

	@Override
	@Property(name = ServiceConfigurationConstants.OfflineMode, defaultValue = "false")
	public void setOffline(boolean isOffline)
	{
		if (this.isOffline == isOffline)
		{
			return;
		}
		this.isOffline = isOffline;

		isOfflineChanged();
	}

	protected final IExtendableContainer<IOfflineListener> offlineListeners = new DefaultExtendableContainer<IOfflineListener>(IOfflineListener.class,
			"offlineListener");

	@Override
	public void afterPropertiesSet()
	{
		// Intended blank
	}

	@Override
	public void lockForRestart(boolean offlineAfterRestart)
	{
		IOfflineListener[] listeners = offlineListeners.getExtensions();

		for (IOfflineListener offlineListener : listeners)
		{
			if (offlineAfterRestart)
			{
				offlineListener.beginOffline();
			}
			else
			{
				offlineListener.beginOnline();
			}
		}
		for (IOfflineListener offlineListener : listeners)
		{
			if (offlineAfterRestart)
			{
				offlineListener.handleOffline();
			}
			else
			{
				offlineListener.handleOnline();
			}
		}
	}

	protected void isOfflineChanged()
	{
		IOfflineListener[] listeners = offlineListeners.getExtensions();

		for (IOfflineListener offlineListener : listeners)
		{
			if (isOffline)
			{
				offlineListener.beginOffline();
			}
			else
			{
				offlineListener.beginOnline();
			}
		}
		for (IOfflineListener offlineListener : listeners)
		{
			if (isOffline)
			{
				offlineListener.handleOffline();
			}
			else
			{
				offlineListener.handleOnline();
			}
		}
		for (IOfflineListener offlineListener : listeners)
		{
			if (isOffline)
			{
				offlineListener.endOffline();
			}
			else
			{
				offlineListener.endOnline();
			}
		}
	}

	@Override
	public abstract String getServiceURL(Class<?> serviceInterface, String serviceName);

	@Override
	public void addOfflineListener(IOfflineListener offlineListener)
	{
		offlineListeners.register(offlineListener);
	}

	@Override
	public void removeOfflineListener(IOfflineListener offlineListener)
	{
		offlineListeners.unregister(offlineListener);
	}
}