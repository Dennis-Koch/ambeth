package de.osthus.ambeth.job.cron4j;

import it.sauronsoftware.cron4j.Scheduler;
import it.sauronsoftware.cron4j.Task;

import java.util.Map;

import de.osthus.ambeth.collections.IMapEntry;
import de.osthus.ambeth.ioc.IDisposableBean;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.ioc.IStartingBean;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.ioc.extendable.MapExtendableContainer;
import de.osthus.ambeth.job.IJob;
import de.osthus.ambeth.job.IJobDescheduleCommand;
import de.osthus.ambeth.job.IJobExtendable;
import de.osthus.ambeth.job.IJobScheduler;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.security.DefaultAuthentication;
import de.osthus.ambeth.security.IAuthentication;
import de.osthus.ambeth.security.ISecurityContext;
import de.osthus.ambeth.security.ISecurityContextHolder;
import de.osthus.ambeth.security.PasswordType;
import de.osthus.ambeth.util.ParamChecker;

public class AmbethCron4jScheduler implements IJobScheduler, IInitializingBean, IDisposableBean, IStartingBean, IJobExtendable
{
	@LogInstance
	private ILogger log;

	@Autowired
	protected IServiceContext beanContext;

	@Autowired
	protected ISecurityContextHolder securityContextHolder;

	protected Scheduler scheduler;

	protected final MapExtendableContainer<IJob, String> jobs = new MapExtendableContainer<IJob, String>("jobId", "job")
	{
		@Override
		protected int extractHash(IJob key)
		{
			return System.identityHashCode(key);
		}

		@Override
		protected boolean equalKeys(IJob key, IMapEntry<IJob, Object> entry)
		{
			return key == entry.getKey();
		}
	};

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		scheduler = new Scheduler();
	}

	@Override
	public void afterStarted() throws Throwable
	{
		scheduler.start();
	}

	@Override
	public void destroy() throws Throwable
	{
		final Scheduler scheduler = this.scheduler;
		final Thread stopThread = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					if (scheduler.isStarted())
					{
						scheduler.stop();
					}
				}
				catch (Throwable e)
				{
					log.error(e);
				}
			}
		});
		Thread checkOfStopThread = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					try
					{
						Thread.sleep(5000);
					}
					catch (InterruptedException e)
					{
						// Intended blank
					}
					stopThread.interrupt();
				}
				catch (Throwable e)
				{
					log.error(e);
				}
			}
		});
		stopThread.setName("Scheduler-StopThread");
		stopThread.setContextClassLoader(Thread.currentThread().getContextClassLoader());
		stopThread.setDaemon(true);
		checkOfStopThread.setName("Scheduler-StopThread-Check");
		checkOfStopThread.setContextClassLoader(Thread.currentThread().getContextClassLoader());
		checkOfStopThread.setDaemon(true);
		stopThread.start();
		checkOfStopThread.start();
	}

	@Override
	public void registerJob(IJob job, String jobName, String cronPattern)
	{
		ParamChecker.assertParamNotNull(job, "job");
		ParamChecker.assertParamNotNull(jobName, "jobName");
		ParamChecker.assertParamNotNull(cronPattern, "cronPattern");
		String username = System.getProperty("user.name");
		if (log.isInfoEnabled())
		{
			log.info("Scheduling job '" + jobName + "' on '" + cronPattern + "' with type '" + job.getClass().getName() + "'");
		}
		IAuthentication authentication = new DefaultAuthentication(username, null, PasswordType.PLAIN);
		String jobId = scheduler.schedule(cronPattern, createTask(job, jobName, authentication));
		try
		{
			jobs.register(jobId, job);
		}
		catch (RuntimeException e)
		{
			scheduler.deschedule(jobId);
			throw e;
		}
	}

	@Override
	public void unregisterJob(IJob job, String jobName, String cronPattern)
	{
		ParamChecker.assertParamNotNull(job, "job");
		ParamChecker.assertParamNotNull(jobName, "jobName");
		ParamChecker.assertParamNotNull(cronPattern, "cronPattern");
		if (log.isInfoEnabled())
		{
			log.info("Unscheduling job '" + jobName + "' on '" + cronPattern + "' with type '" + job.getClass().getName() + "'");
		}
		String jobId = jobs.getExtension(job);
		scheduler.deschedule(jobId);
		jobs.unregister(jobId, job);
	}

	@Override
	public IJobDescheduleCommand scheduleJob(Class<?> jobType, String cronPattern, Map<Object, Object> properties)
	{
		return scheduleJob(jobType.getSimpleName(), jobType, cronPattern, properties);
	}

	@Override
	public IJobDescheduleCommand scheduleJob(String jobName, Class<?> jobType, String cronPattern, Map<Object, Object> properties)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public IJobDescheduleCommand scheduleJob(String jobName, Object jobTask, String cronPattern, Map<Object, Object> properties)
	{
		ISecurityContext context = securityContextHolder.getContext();
		IAuthentication authentication = context != null ? context.getAuthentication() : null;
		return scheduleJobIntern(jobName, jobTask, cronPattern, authentication, properties);
	}

	@Override
	public IJobDescheduleCommand scheduleJob(String jobName, Object jobTask, String cronPattern, String username, char[] userpass,
			Map<Object, Object> properties)
	{
		ParamChecker.assertParamNotNull(username, "username");
		ParamChecker.assertParamNotNull(userpass, "userpass");
		return scheduleJobIntern(jobName, jobTask, cronPattern, new DefaultAuthentication(username, userpass, PasswordType.PLAIN), properties);
	}

	protected IJobDescheduleCommand scheduleJobIntern(String jobName, final Object jobTask, String cronPattern, IAuthentication authentication,
			Map<Object, Object> properties)
	{
		ParamChecker.assertParamNotNull(jobName, "jobName");
		ParamChecker.assertParamNotNull(jobTask, "jobTask");
		ParamChecker.assertParamNotNull(cronPattern, "cronPattern");

		if (log.isInfoEnabled())
		{
			String impersonating = "";
			if (authentication != null)
			{
				impersonating = " impersonating user '" + authentication.getUserName() + "'";
			}
			log.info("Scheduling job '" + jobName + "' on '" + cronPattern + "' with type '" + jobTask.getClass().getName() + "'" + impersonating);
		}
		final String id;
		if (jobTask instanceof IJob)
		{
			id = scheduler.schedule(cronPattern, createTask((IJob) jobTask, jobName, authentication));
		}
		else if (jobTask instanceof Task)
		{
			Task task = (Task) jobTask;
			id = scheduler.schedule(cronPattern, createTask(new TaskJob(task), jobName, authentication));
		}
		else if (jobTask instanceof Runnable)
		{
			Runnable runnable = (Runnable) jobTask;
			id = scheduler.schedule(cronPattern, createTask(new RunnableJob(runnable), jobName, authentication));
		}
		else
		{
			throw new IllegalArgumentException("JobTask not recognized: " + jobTask);
		}
		return new IJobDescheduleCommand()
		{
			@Override
			public void execute()
			{
				scheduler.deschedule(id);
			}
		};
	}

	protected Task createTask(IJob job, String jobName, IAuthentication authentication)
	{
		return beanContext.registerBean(AmbethCron4jJob.class)//
				.propertyValue("Job", job)//
				.propertyValue("JobName", jobName)//
				.propertyValue("Authentication", authentication)//
				.finish();
	}
}
