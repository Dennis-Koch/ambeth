package com.koch.ambeth.job.cron4j;

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.job.IJobContext;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;

import it.sauronsoftware.cron4j.TaskExecutionContext;

public class AmbethCron4jJobContext implements IJobContext
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected TaskExecutionContext taskExecutionContext;

	public TaskExecutionContext getTaskExecutionContext()
	{
		return taskExecutionContext;
	}

	@Override
	public void setStatusMessage(String message)
	{
		taskExecutionContext.setStatusMessage(message);
	}

	@Override
	public void setCompleteness(double completeness)
	{
		taskExecutionContext.setCompleteness(completeness);
	}

	@Override
	public void pauseIfRequested()
	{
		taskExecutionContext.pauseIfRequested();
	}

	@Override
	public boolean isStopped()
	{
		return taskExecutionContext.isStopped();
	}

}
