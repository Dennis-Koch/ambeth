package de.osthus.ambeth.testutil;

import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.ioc.threadlocal.IThreadLocalCleanupController;

public class CleanupAfterIoc implements ICleanupAfter
{
	@Autowired
	protected IThreadLocalCleanupController threadLocalCleanupController;

	@Override
	public void cleanup()
	{
		threadLocalCleanupController.cleanupThreadLocal();
	}
}
