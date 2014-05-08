package de.osthus.ambeth.cache;

import de.osthus.ambeth.cache.model.IServiceResult;
import de.osthus.ambeth.ioc.threadlocal.IThreadLocalCleanupBean;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.threading.SensitiveThreadLocal;

public class ServiceResultHolder implements IServiceResultHolder, IThreadLocalCleanupBean
{
	@SuppressWarnings("unused")
	@LogInstance(ServiceResultHolder.class)
	private ILogger log;

	public static class ServiceResultHolderItem
	{
		public boolean expectORIResult;

		public IServiceResult serviceResult;
	}

	protected final ThreadLocal<ServiceResultHolderItem> valueTL = new SensitiveThreadLocal<ServiceResultHolderItem>();

	@Override
	public void cleanupThreadLocal()
	{
		valueTL.remove();
	}

	@Override
	public boolean isExpectServiceResult()
	{
		ServiceResultHolderItem item = valueTL.get();
		if (item == null)
		{
			return false;
		}
		return item.expectORIResult;
	}

	@Override
	public void setExpectServiceResult(boolean expectServiceResult)
	{
		ServiceResultHolderItem item = valueTL.get();
		if (item == null)
		{
			if (!expectServiceResult)
			{
				return;
			}
			item = new ServiceResultHolderItem();
			valueTL.set(item);
		}
		item.serviceResult = null;
		item.expectORIResult = expectServiceResult;
	}

	@Override
	public IServiceResult getServiceResult()
	{
		ServiceResultHolderItem item = valueTL.get();
		if (item == null)
		{
			return null;
		}
		return item.serviceResult;
	}

	@Override
	public void setServiceResult(IServiceResult serviceResult)
	{
		ServiceResultHolderItem item = valueTL.get();
		if (item == null)
		{
			item = new ServiceResultHolderItem();
			valueTL.set(item);
		}
		item.serviceResult = serviceResult;
	}

	@Override
	public void clearResult()
	{
		ServiceResultHolderItem item = valueTL.get();
		if (item == null)
		{
			return;
		}
		item.expectORIResult = false;
		item.serviceResult = null;
	}
}
