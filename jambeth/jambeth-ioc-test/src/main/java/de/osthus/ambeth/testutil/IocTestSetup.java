package de.osthus.ambeth.testutil;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.ioc.threadlocal.IThreadLocalCleanupController;
import de.osthus.ambeth.util.IDisposable;

public class IocTestSetup implements IDisposable
{
	public final HashMap<String, Object> baseProps = new HashMap<String, Object>();

	public final ArrayList<Class<?>> frameworkModules = new ArrayList<Class<?>>();

	public final ArrayList<Class<?>> applicationModules = new ArrayList<Class<?>>();

	public IServiceContext testClassLevelContext;

	public IServiceContext beanContext;

	public IocTestSetup(IServiceContext testClassLevelContext, IServiceContext beanContext)
	{
		this.testClassLevelContext = testClassLevelContext;
		this.beanContext = beanContext;
	}

	@Override
	public void dispose()
	{
		IThreadLocalCleanupController tlCleanupController = testClassLevelContext != null && !testClassLevelContext.isDisposed() ? testClassLevelContext
				.getService(IThreadLocalCleanupController.class) : null;
		try
		{
			if (testClassLevelContext != null)
			{
				testClassLevelContext.getRoot().dispose();
				baseProps.clear();
				frameworkModules.clear();
				applicationModules.clear();
				testClassLevelContext = null;
			}
			if (!beanContext.isDisposed())
			{
				throw new IllegalStateException();
			}
			beanContext = null;
		}
		finally
		{
			if (tlCleanupController != null)
			{
				tlCleanupController.cleanupThreadLocal();
			}
		}

	}
}
