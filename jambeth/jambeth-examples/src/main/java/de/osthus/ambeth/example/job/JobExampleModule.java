package de.osthus.ambeth.example.job;

import de.osthus.ambeth.ioc.IInitializingModule;
import de.osthus.ambeth.ioc.config.IBeanConfiguration;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.job.IJobExtendable;

public class JobExampleModule implements IInitializingModule {
	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable {
		IBeanConfiguration job1 = beanContextFactory.registerBean(JobExample.class);
		beanContextFactory.link(job1).to(IJobExtendable.class).with("job1-hourly", "0 * * * *");
	}
}