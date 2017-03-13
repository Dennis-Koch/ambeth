package com.koch.ambeth.example.job;

import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.config.IBeanConfiguration;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.job.IJobExtendable;

public class JobExampleModule implements IInitializingModule {
	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable {
		IBeanConfiguration job1 = beanContextFactory.registerBean(JobExample.class);
		beanContextFactory.link(job1).to(IJobExtendable.class).with("job1-hourly", "0 * * * *");
	}
}