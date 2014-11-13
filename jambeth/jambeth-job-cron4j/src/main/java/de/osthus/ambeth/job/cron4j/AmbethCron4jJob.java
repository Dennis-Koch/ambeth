package de.osthus.ambeth.job.cron4j;

import it.sauronsoftware.cron4j.Task;
import it.sauronsoftware.cron4j.TaskExecutionContext;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.ioc.threadlocal.IThreadLocalCleanupController;
import de.osthus.ambeth.job.IJob;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.security.IAuthentication;
import de.osthus.ambeth.security.ISecurityContextHolder;
import de.osthus.ambeth.threading.IResultingBackgroundWorkerDelegate;

public class AmbethCron4jJob extends Task
{
	@LogInstance
	private ILogger log;

	@Autowired(optional = true)
	protected IAuthentication authentication;

	@Autowired
	protected IServiceContext beanContext;

	@Autowired
	protected ISecurityContextHolder securityContextHolder;

	@Autowired
	protected IJob job;

	@Property
	protected String jobName;

	protected Lock writeLock = new ReentrantLock();

	protected Lock waitingLock = new ReentrantLock();

	@Override
	public void execute(TaskExecutionContext context) throws RuntimeException
	{
		Lock waitingLock = this.waitingLock;
		if (!waitingLock.tryLock())
		{
			return;
		}
		Lock writeLock = this.writeLock;
		try
		{
			writeLock.lock();
		}
		finally
		{
			waitingLock.unlock();
		}
		try
		{
			Thread thread = Thread.currentThread();

			IThreadLocalCleanupController tlCleanupController = beanContext.getService(IThreadLocalCleanupController.class);
			String oldName = thread.getName();
			try
			{
				thread.setName("Job " + jobName);
				final AmbethCron4jJobContext jobContext = beanContext.registerBean(AmbethCron4jJobContext.class)
						.propertyValue("TaskExecutionContext", context).finish();

				if (log.isDebugEnabled())
				{
					log.debug("Executing job '" + jobName + "'");
				}
				try
				{
					if (authentication == null)
					{
						job.execute(jobContext);
					}
					else
					{
						securityContextHolder.setScopedAuthentication(authentication, new IResultingBackgroundWorkerDelegate<Object>()
						{
							@Override
							public Object invoke() throws Throwable
							{
								job.execute(jobContext);
								return null;
							}
						});
					}
					if (log.isDebugEnabled())
					{
						log.debug("Execution of job '" + jobName + "' finished");
					}
				}
				catch (Throwable e)
				{
					if (log.isErrorEnabled())
					{
						log.error("Error occured while executing job '" + jobName + "'", e);
					}
				}
			}
			finally
			{
				thread.setName(oldName);
				tlCleanupController.cleanupThreadLocal();
			}
		}
		finally
		{
			writeLock.unlock();
		}
	}
}
