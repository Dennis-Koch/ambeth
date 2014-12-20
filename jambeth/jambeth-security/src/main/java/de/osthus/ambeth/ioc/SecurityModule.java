package de.osthus.ambeth.ioc;

import de.osthus.ambeth.ioc.annotation.FrameworkModule;
import de.osthus.ambeth.ioc.config.IBeanConfiguration;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.security.IAuthorizationChangeListenerExtendable;
import de.osthus.ambeth.security.ISecurityActivation;
import de.osthus.ambeth.security.ISecurityContextHolder;
import de.osthus.ambeth.security.SecurityActivation;
import de.osthus.ambeth.security.SecurityContextHolder;
import de.osthus.ambeth.threading.FastThreadPool;
import de.osthus.ambeth.util.IMultithreadingHelper;
import de.osthus.ambeth.util.MultithreadingHelper;

@FrameworkModule
public class SecurityModule implements IInitializingModule
{
	public static final String THREAD_POOL_NAME = "threadPool";

	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		beanContextFactory.registerBean(SecurityActivation.class).autowireable(ISecurityActivation.class);

		beanContextFactory.registerBean(SecurityContextHolder.class).autowireable(ISecurityContextHolder.class, IAuthorizationChangeListenerExtendable.class);

		FastThreadPool fastThreadPool = new FastThreadPool(0, Integer.MAX_VALUE, 60000)
		{
			@Override
			public void refreshThreadCount()
			{
				if (variableThreads)
				{
					int processors = Runtime.getRuntime().availableProcessors();
					setMaxThreadCount(processors * 2);
				}
			}
		};
		fastThreadPool.setName("MTH");

		IBeanConfiguration fastThreadPoolBean = beanContextFactory.registerExternalBean(THREAD_POOL_NAME, fastThreadPool);

		beanContextFactory.registerBean(MultithreadingHelper.class).autowireable(IMultithreadingHelper.class)//
				.propertyRef(fastThreadPoolBean);
	}
}
