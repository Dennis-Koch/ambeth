package de.osthus.ambeth.security.threading;

import java.util.concurrent.Exchanger;

import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.ioc.proxy.Self;
import de.osthus.ambeth.ioc.threadlocal.IThreadLocalCleanupController;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.security.IAuthentication;
import de.osthus.ambeth.security.ISecurityContextHolder;
import de.osthus.ambeth.security.SecurityContext;
import de.osthus.ambeth.security.SecurityContextType;
import de.osthus.ambeth.threading.IBackgroundWorkerDelegate;
import de.osthus.ambeth.threading.IFastThreadPool;
import de.osthus.ambeth.threading.IResultingBackgroundWorkerDelegate;

@SecurityContext(SecurityContextType.NOT_REQUIRED)
public class BackgroundAuthenticatingExecutorService implements IBackgroundAuthenticatingExecutorService, IBackgroundAuthenticatingExecution
{
	@LogInstance
	private ILogger log;

	@Autowired
	protected IFastThreadPool threadPool;

	@Autowired
	protected ISecurityContextHolder securityContextHolder;

	@Autowired
	protected IThreadLocalCleanupController threadLocalCleanupController;

	@Self
	protected IBackgroundAuthenticatingExecution self;

	@Override
	public void startBackgroundWorkerWithAuthentication(IBackgroundWorkerDelegate runnable)
	{
		// get the current authentication
		Runnable backgroundWorker = createRunnableWithAuthentication(runnable);
		// Using Ambeth Thread pool to get ThreadLocal support e.g. for authentication issues
		threadPool.execute(backgroundWorker);
	}

	@Override
	public <T> T startBackgroundWorkerWithAuthentication(IResultingBackgroundWorkerDelegate<T> runnable)
	{
		// get the current authentication
		Exchanger<T> exchanger = new Exchanger<T>();
		Runnable backgroundWorker = createRunnableWithAuthentication(runnable, exchanger);
		// Using Ambeth Thread pool to get ThreadLocal support e.g. for authentication issues
		threadPool.execute(backgroundWorker);
		try
		{
			return exchanger.exchange(null);
		}
		catch (InterruptedException e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Override
	@SecurityContext(SecurityContextType.AUTHENTICATED)
	public void execute(IBackgroundWorkerDelegate runnable) throws Throwable
	{
		runnable.invoke();
	}

	@Override
	@SecurityContext(SecurityContextType.AUTHENTICATED)
	public <T> T execute(IResultingBackgroundWorkerDelegate<T> runnable) throws Throwable
	{
		return runnable.invoke();
	}

	private Runnable createRunnableWithAuthentication(final IBackgroundWorkerDelegate runnable)
	{
		final IAuthentication authentication = securityContextHolder.getContext().getAuthentication();

		Runnable backgroundWorker = new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					securityContextHolder.setScopedAuthentication(authentication, new IResultingBackgroundWorkerDelegate<Object>()
					{
						@Override
						public Object invoke() throws Throwable
						{
							self.execute(runnable);
							return null;
						}
					});
				}
				catch (Throwable e)
				{
					throw RuntimeExceptionUtil.mask(e);
				}
				finally
				{
					threadLocalCleanupController.cleanupThreadLocal();
				}
			}
		};
		return backgroundWorker;
	}

	private <T> Runnable createRunnableWithAuthentication(final IResultingBackgroundWorkerDelegate<T> runnable, final Exchanger<T> exchanger)
	{
		final IAuthentication authentication = securityContextHolder.getContext().getAuthentication();

		Runnable backgroundWorker = new Runnable()
		{
			@Override
			public void run()
			{
				T result = null;
				try
				{
					result = securityContextHolder.setScopedAuthentication(authentication, new IResultingBackgroundWorkerDelegate<T>()
					{
						@Override
						public T invoke() throws Throwable
						{
							return self.execute(runnable);
						}
					});
				}
				catch (Throwable e)
				{
					throw RuntimeExceptionUtil.mask(e);
				}
				finally
				{
					threadLocalCleanupController.cleanupThreadLocal();
					try
					{
						exchanger.exchange(result);
					}
					catch (InterruptedException e)
					{
						// intended blank
					}
				}
			}
		};
		return backgroundWorker;
	}
}
