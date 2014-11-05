package de.osthus.ambeth.job.cron4j;

import it.sauronsoftware.cron4j.TaskExecutionContext;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.job.IJobContext;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;

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
