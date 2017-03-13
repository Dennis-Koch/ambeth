package com.koch.ambeth.job;

import com.koch.ambeth.ioc.IDisposableBean;
import com.koch.ambeth.ioc.IStartingBean;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;

public class JobScheduleConfiguration implements IStartingBean, IDisposableBean
{
	public static final String CRON_PATTERN = "CronPattern";

	public static final String JOB = "Job";

	public static final String JOB_NAME = "JobName";

	public static final String USER_NAME = "UserName";

	public static final String USER_PASS = "UserPass";

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired(optional = true)
	protected IJobScheduler jobScheduler;

	@Property
	protected IJob job;

	@Property
	protected String cronPattern;

	@Property(mandatory = false)
	protected String jobName;

	@Property
	protected String userName;

	@Property
	protected char[] userPass;

	protected IJobDescheduleCommand jobDescheduleCommand;

	@Override
	public void afterStarted() throws Throwable
	{
		if (jobScheduler == null)
		{
			return;
		}
		if (jobName == null)
		{
			jobName = job.getClass().getSimpleName();
		}
		jobDescheduleCommand = jobScheduler.scheduleJob(jobName, job, cronPattern, userName, userPass, null);
	}

	@Override
	public void destroy() throws Throwable
	{
		if (jobDescheduleCommand != null)
		{
			jobDescheduleCommand.execute();
			jobDescheduleCommand = null;
		}
	}
}
