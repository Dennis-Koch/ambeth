package de.osthus.ambeth.job.cron4j;

import it.sauronsoftware.cron4j.TaskExecutionContext;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.job.IJobContext;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.util.ParamChecker;

public class AmbethCron4jJobContext implements IInitializingBean, IJobContext
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	protected TaskExecutionContext taskExecutionContext;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		ParamChecker.assertNotNull(taskExecutionContext, "taskExecutionContext");
	}

	public void setTaskExecutionContext(TaskExecutionContext taskExecutionContext)
	{
		this.taskExecutionContext = taskExecutionContext;
	}

	@Override
	public void setStatusMessage(String message)
	{
		this.taskExecutionContext.setStatusMessage(message);
	}

	@Override
	public void setCompleteness(double completeness)
	{
		this.taskExecutionContext.setCompleteness(completeness);
	}

	@Override
	public void pauseIfRequested()
	{
		this.taskExecutionContext.pauseIfRequested();
	}

	@Override
	public boolean isStopped()
	{
		return this.taskExecutionContext.isStopped();
	}

}
