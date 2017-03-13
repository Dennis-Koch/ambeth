package com.koch.ambeth.security.threading;

import java.util.concurrent.Exchanger;

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.proxy.Self;
import com.koch.ambeth.ioc.threadlocal.IThreadLocalCleanupController;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.security.IAuthentication;
import com.koch.ambeth.security.ISecurityContextHolder;
import com.koch.ambeth.security.SecurityContext;
import com.koch.ambeth.security.SecurityContextType;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
import com.koch.ambeth.util.threading.IBackgroundWorkerDelegate;
import com.koch.ambeth.util.threading.IFastThreadPool;
import com.koch.ambeth.util.threading.IResultingBackgroundWorkerDelegate;

@SecurityContext(SecurityContextType.NOT_REQUIRED)
public class BackgroundAuthenticatingExecutorService implements IBackgroundAuthenticatingExecutorService, IBackgroundAuthenticatingExecution
{
	private class ExchangeResultRunnable<T> implements Runnable
	{
		private final Exchanger<T> exchanger;
		private final IAuthentication authentication;
		private final IResultingBackgroundWorkerDelegate<T> runnable;

		private ExchangeResultRunnable(Exchanger<T> exchanger, IAuthentication authentication, IResultingBackgroundWorkerDelegate<T> runnable)
		{
			this.exchanger = exchanger;
			this.authentication = authentication;
			this.runnable = runnable;
		}

		@Override
		public void run()
		{
			T result = null;
			try
			{
				result = securityContextHolder.setScopedAuthentication(authentication, new SelfExecuteRunnable<T>(runnable));
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
	}

	private class SelfExecuteRunnable<T> implements IResultingBackgroundWorkerDelegate<T>
	{
		private final IResultingBackgroundWorkerDelegate<T> runnable;

		private SelfExecuteRunnable(IResultingBackgroundWorkerDelegate<T> runnable)
		{
			this.runnable = runnable;
		}

		@Override
		public T invoke() throws Throwable
		{
			return self.execute(runnable);
		}
	}

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
		IAuthentication authentication = securityContextHolder.getContext().getAuthentication();

		Runnable backgroundWorker = new ExchangeResultRunnable<T>(exchanger, authentication, runnable);
		return backgroundWorker;
	}
}
