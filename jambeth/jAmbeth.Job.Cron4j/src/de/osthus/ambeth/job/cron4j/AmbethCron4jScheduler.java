package de.osthus.ambeth.job.cron4j;

import it.sauronsoftware.cron4j.Scheduler;
import it.sauronsoftware.cron4j.Task;

import java.util.Map;

import de.osthus.ambeth.collections.IMapEntry;
import de.osthus.ambeth.ioc.IBeanRuntime;
import de.osthus.ambeth.ioc.IDisposableBean;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.ioc.IStartingBean;
import de.osthus.ambeth.ioc.extendable.MapExtendableContainer;
import de.osthus.ambeth.job.IJob;
import de.osthus.ambeth.job.IJobExtendable;
import de.osthus.ambeth.job.IJobScheduler;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.typeinfo.ITypeInfoProvider;
import de.osthus.ambeth.util.ParamChecker;

public class AmbethCron4jScheduler implements IJobScheduler, IInitializingBean, IDisposableBean, IStartingBean, IJobExtendable
{
	@LogInstance
	private ILogger log;

	protected IServiceContext beanContext;

	protected ITypeInfoProvider typeInfoProvider;

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
		ParamChecker.assertNotNull(beanContext, "BeanContext");
		ParamChecker.assertNotNull(typeInfoProvider, "TypeInfoProvider");
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
				scheduler.stop();
			}
		});
		Thread checkOfStopThread = new Thread(new Runnable()
		{
			@Override
			public void run()
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
		});
		stopThread.setDaemon(true);
		checkOfStopThread.setDaemon(true);
		stopThread.start();
		checkOfStopThread.start();
	}

	public void setBeanContext(IServiceContext beanContext)
	{
		this.beanContext = beanContext;
	}

	public void setTypeInfoProvider(ITypeInfoProvider typeInfoProvider)
	{
		this.typeInfoProvider = typeInfoProvider;
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
		String jobId = scheduler.schedule(cronPattern, createTask(job, jobName, username, null));
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
	public void scheduleJob(Class<?> jobType, String cronPattern, Map<Object, Object> properties)
	{
		scheduleJob(typeInfoProvider.getTypeInfo(jobType).getSimpleName(), jobType, cronPattern, properties);
	}

	@Override
	public void scheduleJob(String jobName, Class<?> jobType, String cronPattern, Map<Object, Object> properties)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void scheduleJob(String jobName, Object jobTask, String cronPattern, Map<Object, Object> properties)
	{
		String username = System.getProperty("user.name");
		scheduleJob(jobName, jobTask, cronPattern, username, null, properties);
	}

	@Override
	public void scheduleJob(String jobName, Object jobTask, String cronPattern, String username, byte[] userpass, Map<Object, Object> properties)
	{
		ParamChecker.assertParamNotNull(jobName, "jobName");
		ParamChecker.assertParamNotNull(jobTask, "jobTask");
		ParamChecker.assertParamNotNull(cronPattern, "cronPattern");
		ParamChecker.assertParamNotNull(username, "username");

		if (log.isInfoEnabled())
		{
			log.info("Scheduling job '" + jobName + "' on '" + cronPattern + "' with type '" + jobTask.getClass().getName() + "'");
		}
		if (jobTask instanceof IJob)
		{
			scheduler.schedule(cronPattern, createTask((IJob) jobTask, jobName, username, userpass));
		}
		else if (jobTask instanceof Task)
		{
			scheduler.schedule(cronPattern, (Task) jobTask);
		}
		else if (jobTask instanceof Runnable)
		{
			scheduler.schedule(cronPattern, (Runnable) jobTask);
		}
	}

	protected Task createTask(IJob job, String jobName, String username, byte[] userpass)
	{
		IBeanRuntime<AmbethCron4jJob> jobConf = beanContext.registerAnonymousBean(AmbethCron4jJob.class).propertyValue("Job", job)
				.propertyValue("JobName", jobName).propertyValue("UserName", username).propertyValue("UserPass", userpass);
		return jobConf.finish();
	}
}
