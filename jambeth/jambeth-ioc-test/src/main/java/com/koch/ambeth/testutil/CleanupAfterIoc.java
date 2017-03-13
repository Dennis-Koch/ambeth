package com.koch.ambeth.testutil;

import com.koch.ambeth.ioc.IDisposableBean;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.threadlocal.IThreadLocalCleanupController;

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
