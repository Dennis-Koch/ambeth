package de.osthus.ambeth.job.cron4j;

import de.osthus.ambeth.job.IJob;
import de.osthus.ambeth.job.IJobContext;

public class RunnableJob implements IJob
{
	private final Runnable runnable;

	public RunnableJob(Runnable runnable)
	{
		this.runnable = runnable;
	}

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
		runnable.run();
	}
}
