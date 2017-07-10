package com.koch.ambeth.job;

/*-
 * #%L
 * jambeth-job
 * %%
 * Copyright (C) 2017 Koch Softwaredevelopment
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 * #L%
 */

import com.koch.ambeth.ioc.IDisposableBean;
import com.koch.ambeth.ioc.IStartingBean;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.config.Property;

public class JobScheduleConfiguration implements IStartingBean, IDisposableBean {
	public static final String CRON_PATTERN = "CronPattern";

	public static final String JOB = "Job";

	public static final String JOB_NAME = "JobName";

	public static final String USER_NAME = "UserName";

	public static final String USER_PASS = "UserPass";

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
	public void afterStarted() throws Throwable {
		if (jobScheduler == null) {
			return;
		}
		if (jobName == null) {
			jobName = job.getClass().getSimpleName();
		}
		jobDescheduleCommand = jobScheduler.scheduleJob(jobName, job, cronPattern, userName, userPass,
				null);
	}

	@Override
	public void destroy() throws Throwable {
		if (jobDescheduleCommand != null) {
			jobDescheduleCommand.execute();
			jobDescheduleCommand = null;
		}
	}
}
