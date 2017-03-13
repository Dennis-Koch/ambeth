package com.koch.ambeth.cache.server;

import com.koch.ambeth.ioc.threadlocal.IThreadLocalCleanupBean;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.service.cache.IServiceResultHolder;
import com.koch.ambeth.service.cache.model.IServiceResult;
import com.koch.ambeth.util.threading.SensitiveThreadLocal;

public class ServiceResultHolder implements IServiceResultHolder, IThreadLocalCleanupBean
{
	@SuppressWarnings("unused")
	@LogInstance
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
