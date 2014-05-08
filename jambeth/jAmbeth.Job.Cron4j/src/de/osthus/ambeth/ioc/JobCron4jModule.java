package de.osthus.ambeth.ioc;

import de.osthus.ambeth.ioc.annotation.FrameworkModule;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.job.IJobExtendable;
import de.osthus.ambeth.job.IJobScheduler;
import de.osthus.ambeth.job.cron4j.AmbethCron4jScheduler;

@FrameworkModule
public class JobCron4jModule implements IInitializingModule
{
	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		beanContextFactory.registerBean("jobScheduler", AmbethCron4jScheduler.class).autowireable(IJobScheduler.class, IJobExtendable.class);
	}
}
