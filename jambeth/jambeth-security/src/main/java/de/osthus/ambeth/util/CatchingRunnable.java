package de.osthus.ambeth.util;

import java.util.concurrent.CountDownLatch;

import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.ioc.threadlocal.IForkState;
import de.osthus.ambeth.ioc.threadlocal.IThreadLocalCleanupController;
import de.osthus.ambeth.security.ISecurityContextHolder;

public class CatchingRunnable implements Runnable
{
	@Property
	protected IForkState forkState;

	@Property
	protected Runnable runnable;

	@Property
	protected CountDownLatch latch;

	@Property
	protected IParamHolder<Throwable> throwableHolder;

	@Autowired
	protected ISecurityContextHolder securityContextHolder;

	@Autowired
	protected IThreadLocalCleanupController threadLocalCleanupController;

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
			try
			{
				if (forkState != null)
				{
					forkState.use(runnable);
				}
				else
				{
					runnable.run();
				}
			}
			catch (Throwable e)
			{
				throwableHolder.setValue(e);
			}
			finally
			{
				threadLocalCleanupController.cleanupThreadLocal();
				latch.countDown();
				securityContextHolder.clearContext();
			}
		}
		finally
		{
			if (runnable instanceof INamedRunnable)
			{
				currentThread.setName(oldName);
			}
		}
	}
}
