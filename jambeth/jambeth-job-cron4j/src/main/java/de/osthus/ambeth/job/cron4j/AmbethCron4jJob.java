package de.osthus.ambeth.job.cron4j;

import it.sauronsoftware.cron4j.Task;
import it.sauronsoftware.cron4j.TaskExecutionContext;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.ioc.threadlocal.IThreadLocalCleanupController;
import de.osthus.ambeth.job.IJob;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.security.DefaultAuthentication;
import de.osthus.ambeth.security.IAuthentication.PasswordType;
import de.osthus.ambeth.security.SecurityContextHolder;
import de.osthus.ambeth.threading.IResultingBackgroundWorkerDelegate;
import de.osthus.ambeth.util.ParamChecker;

public class AmbethCron4jJob extends Task implements IInitializingBean
{
	@LogInstance
	private ILogger log;

	@Autowired
	protected IServiceContext beanContext;

	@Autowired
	protected SecurityContextHolder securityContextHolder;

	@Autowired
	protected IJob job;

	@Property
	protected String jobName;

	@Property
	protected String userName;

	@Property
	protected char[] userPass;

	protected Lock writeLock = new ReentrantLock();

	protected Lock waitingLock = new ReentrantLock();

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		ParamChecker.assertNotNull(jobName, "jobName");
		ParamChecker.assertNotNull(userName, "userName");
	}

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
				final AmbethCron4jJobContext jobContext = beanContext.registerAnonymousBean(AmbethCron4jJobContext.class)
						.propertyValue("TaskExecutionContext", context).finish();

				if (log.isDebugEnabled())
				{
					log.debug("Executing job '" + jobName + "'");
				}
				try
				{
					securityContextHolder.setScopedAuthentication(new DefaultAuthentication(userName, userPass, PasswordType.PLAIN),
							new IResultingBackgroundWorkerDelegate<Object>()
							{
								@Override
								public Object invoke() throws Throwable
								{
									job.execute(jobContext);
									return null;
								}
							});

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
