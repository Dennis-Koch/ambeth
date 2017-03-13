package com.koch.ambeth.security.job.threading;

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.job.IJob;
import com.koch.ambeth.job.IJobContext;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.util.threading.FastThreadPool;

public class ThreadPoolRefreshJob implements IJob
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected FastThreadPool threadPool;

	@Override
	public boolean canBePaused()
	{
		return false;
	}

	@Override
	public boolean canBeStopped()
	{
		return false;
	}

	@Override
	public boolean supportsStatusTracking()
	{
		return false;
	}

	@Override
	public boolean supportsCompletenessTracking()
	{
		return false;
	}

	@Override
	public void execute(IJobContext context) throws Throwable
	{
		threadPool.refreshThreadCount();
	}
}
