package de.osthus.ambeth.testutil;

import de.osthus.ambeth.ioc.IDisposableBean;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.ioc.threadlocal.IThreadLocalCleanupController;

public class CleanupAfterIoc implements ICleanupAfter, IDisposableBean
{
	@Autowired
	protected IThreadLocalCleanupController threadLocalCleanupController;

	@Override
	public void destroy() throws Throwable
	{
		cleanup();
	}

	@Override
	public void cleanup()
	{
		threadLocalCleanupController.cleanupThreadLocal();
	}
}
