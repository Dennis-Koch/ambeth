package com.koch.ambeth.security.job.ioc;

import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.IocModule;
import com.koch.ambeth.ioc.annotation.FrameworkModule;
import com.koch.ambeth.ioc.config.IBeanConfiguration;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.job.IJobExtendable;
import com.koch.ambeth.security.job.threading.ThreadPoolRefreshJob;

@FrameworkModule
public class SecurityJobModule implements IInitializingModule
{
	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		IBeanConfiguration threadPoolRefreshJob = beanContextFactory.registerBean(ThreadPoolRefreshJob.class)//
				.propertyRef("ThreadPool", IocModule.THREAD_POOL_NAME);
		beanContextFactory.link(threadPoolRefreshJob).to(IJobExtendable.class).with("threadPool-refresh", "* * * * * *").optional();
	}
}
