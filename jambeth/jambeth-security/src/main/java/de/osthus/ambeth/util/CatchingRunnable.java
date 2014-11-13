package de.osthus.ambeth.util;

import java.util.concurrent.CountDownLatch;

import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.ioc.threadlocal.IThreadLocalCleanupController;
import de.osthus.ambeth.model.ISecurityScope;
import de.osthus.ambeth.security.IAuthentication;
import de.osthus.ambeth.security.IAuthorization;
import de.osthus.ambeth.security.ISecurityContext;
import de.osthus.ambeth.security.ISecurityContextHolder;
import de.osthus.ambeth.security.ISecurityScopeProvider;
import de.osthus.ambeth.threading.IResultingBackgroundWorkerDelegate;

public class CatchingRunnable implements Runnable, IInitializingBean
{
	@Property
	protected Runnable runnable;

	@Property
	protected CountDownLatch latch;

	@Property
	protected IParamHolder<Throwable> throwableHolder;

	@Autowired
	protected ISecurityContextHolder securityContextHolder;

	@Autowired
	protected ISecurityScopeProvider securityScopeProvider;

	@Autowired
	protected IThreadLocalCleanupController threadLocalCleanupController;

	protected IAuthentication authentication;

	protected IAuthorization authorization;

	protected ISecurityScope[] securityScopes;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		ISecurityContext securityContext = securityContextHolder.getContext();
		if (securityContext != null)
		{
			authentication = securityContext.getAuthentication();
			authorization = securityContext.getAuthorization();
		}
		securityScopes = securityScopeProvider.getSecurityScopes();
	}

	@Override
	public void run()
	{
		Thread currentThread = Thread.currentThread();
		String oldName = currentThread.getName();
		if (runnable instanceof INamedRunnable)
		{
			currentThread.setName(((INamedRunnable) runnable).getName());
		}
		try
		{
			if (authentication != null)
			{
				ISecurityContext contextOfThread = securityContextHolder.getCreateContext();
				contextOfThread.setAuthentication(authentication);
			}
			if (authorization != null)
			{
				ISecurityContext contextOfThread = securityContextHolder.getCreateContext();
				contextOfThread.setAuthorization(authorization);
			}
			try
			{
				securityScopeProvider.executeWithSecurityScopes(new IResultingBackgroundWorkerDelegate<Object>()
				{
					@Override
					public Object invoke() throws Throwable
					{
						runnable.run();
						return null;
					}
				}, securityScopes);
			}
			catch (Throwable e)
			{
				throwableHolder.setValue(e);
			}
			finally
			{
				if (threadLocalCleanupController != null)
				{
					threadLocalCleanupController.cleanupThreadLocal();
				}
				latch.countDown();
				securityContextHolder.clearContext();
			}
		}
		finally
		{
			currentThread.setName(oldName);
		}
	}
}
