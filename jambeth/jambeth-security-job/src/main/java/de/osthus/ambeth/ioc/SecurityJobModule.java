package de.osthus.ambeth.ioc;

import de.osthus.ambeth.ioc.annotation.FrameworkModule;
import de.osthus.ambeth.ioc.config.IBeanConfiguration;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.job.IJobExtendable;
import de.osthus.ambeth.threading.ThreadPoolRefreshJob;

@FrameworkModule
public class SecurityJobModule implements IInitializingModule
{
	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		IBeanConfiguration threadPoolRefreshJob = beanContextFactory.registerBean(ThreadPoolRefreshJob.class)//
				.propertyRef("ThreadPool", SecurityModule.THREAD_POOL_NAME);
		beanContextFactory.link(threadPoolRefreshJob).to(IJobExtendable.class).with("threadPool-refresh", "* * * * * *").optional();
	}
}
