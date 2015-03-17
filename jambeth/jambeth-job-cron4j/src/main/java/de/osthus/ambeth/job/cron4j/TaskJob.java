package de.osthus.ambeth.job.cron4j;

import it.sauronsoftware.cron4j.Task;
import de.osthus.ambeth.job.IJob;
import de.osthus.ambeth.job.IJobContext;

public class TaskJob implements IJob
{
	private final Task task;

	public TaskJob(Task task)
	{
		this.task = task;
	}

	@Override
	public boolean canBePaused()
	{
		return task.canBePaused();
	}

	@Override
	public boolean canBeStopped()
	{
		return task.canBeStopped();
	}

	@Override
	public boolean supportsStatusTracking()
	{
		return task.supportsStatusTracking();
	}

	@Override
	public boolean supportsCompletenessTracking()
	{
		return task.supportsCompletenessTracking();
	}

	@Override
	public void execute(IJobContext context) throws Throwable
	{
		task.execute(((AmbethCron4jJobContext) context).getTaskExecutionContext());
	}
}
