package com.koch.ambeth.job.cron4j.ioc;

import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.annotation.FrameworkModule;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.job.IJobExtendable;
import com.koch.ambeth.job.IJobScheduler;
import com.koch.ambeth.job.cron4j.AmbethCron4jScheduler;

@FrameworkModule
public class JobCron4jModule implements IInitializingModule
{
	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		beanContextFactory.registerBean("jobScheduler", AmbethCron4jScheduler.class).autowireable(IJobScheduler.class, IJobExtendable.class);
	}
}
