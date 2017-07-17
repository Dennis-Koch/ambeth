package com.koch.ambeth.security.threading;

/*-
 * #%L
 * jambeth-security
 * %%
 * Copyright (C) 2017 Koch Softwaredevelopment
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 * #L%
 */

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
import com.koch.ambeth.util.state.IStateRollback;
import com.koch.ambeth.util.threading.IBackgroundWorkerDelegate;
import com.koch.ambeth.util.threading.IFastThreadPool;
import com.koch.ambeth.util.threading.IResultingBackgroundWorkerDelegate;

@SecurityContext(SecurityContextType.NOT_REQUIRED)
public class BackgroundAuthenticatingExecutorService
		implements IBackgroundAuthenticatingExecutorService, IBackgroundAuthenticatingExecution {
	private class ExchangeResultRunnable<T> implements Runnable {
		private final Exchanger<T> exchanger;
		private final IAuthentication authentication;
		private final IResultingBackgroundWorkerDelegate<T> runnable;

		private ExchangeResultRunnable(Exchanger<T> exchanger, IAuthentication authentication,
				IResultingBackgroundWorkerDelegate<T> runnable) {
			this.exchanger = exchanger;
			this.authentication = authentication;
			this.runnable = runnable;
		}

		@Override
		public void run() {
			T result = null;
			IStateRollback rollback = securityContextHolder.pushAuthentication(authentication);
			try {
				result = runnable.invoke();
			}
			catch (Exception e) {
				throw RuntimeExceptionUtil.mask(e);
			}
			finally {
				rollback.rollback();
				threadLocalCleanupController.cleanupThreadLocal();
				try {
					exchanger.exchange(result);
				}
				catch (InterruptedException e) {
					// intended blank
				}
			}
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
	public void startBackgroundWorkerWithAuthentication(IBackgroundWorkerDelegate runnable) {
		// get the current authentication
		Runnable backgroundWorker = createRunnableWithAuthentication(runnable);
		// Using Ambeth Thread pool to get ThreadLocal support e.g. for authentication issues
		threadPool.execute(backgroundWorker);
	}

	@Override
	public <T> T startBackgroundWorkerWithAuthentication(
			IResultingBackgroundWorkerDelegate<T> runnable) {
		// get the current authentication
		Exchanger<T> exchanger = new Exchanger<>();
		Runnable backgroundWorker = createRunnableWithAuthentication(runnable, exchanger);
		// Using Ambeth Thread pool to get ThreadLocal support e.g. for authentication issues
		threadPool.execute(backgroundWorker);
		try {
			return exchanger.exchange(null);
		}
		catch (InterruptedException e) {
			Thread.interrupted(); // clear flag
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Override
	@SecurityContext(SecurityContextType.AUTHENTICATED)
	public void execute(IBackgroundWorkerDelegate runnable) throws Exception {
		runnable.invoke();
	}

	@Override
	@SecurityContext(SecurityContextType.AUTHENTICATED)
	public <T> T execute(IResultingBackgroundWorkerDelegate<T> runnable) throws Exception {
		return runnable.invoke();
	}

	private Runnable createRunnableWithAuthentication(final IBackgroundWorkerDelegate runnable) {
		final IAuthentication authentication = securityContextHolder.getContext().getAuthentication();

		Runnable backgroundWorker = new Runnable() {
			@Override
			public void run() {
				IStateRollback rollback = securityContextHolder.pushAuthentication(authentication);
				try {
					self.execute(runnable);
				}
				catch (Exception e) {
					throw RuntimeExceptionUtil.mask(e);
				}
				finally {
					rollback.rollback();
					threadLocalCleanupController.cleanupThreadLocal();
				}
			}
		};
		return backgroundWorker;
	}

	private <T> Runnable createRunnableWithAuthentication(
			final IResultingBackgroundWorkerDelegate<T> runnable, final Exchanger<T> exchanger) {
		IAuthentication authentication = securityContextHolder.getContext().getAuthentication();

		Runnable backgroundWorker = new ExchangeResultRunnable<>(exchanger, authentication, runnable);
		return backgroundWorker;
	}
}
