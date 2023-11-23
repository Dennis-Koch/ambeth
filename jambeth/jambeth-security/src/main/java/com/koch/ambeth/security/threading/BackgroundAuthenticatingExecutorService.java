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
import com.koch.ambeth.util.function.CheckedRunnable;
import com.koch.ambeth.util.function.CheckedSupplier;
import com.koch.ambeth.util.state.StateRollback;
import lombok.SneakyThrows;

import java.util.concurrent.Exchanger;
import java.util.concurrent.Executor;

@SecurityContext(SecurityContextType.NOT_REQUIRED)
public class BackgroundAuthenticatingExecutorService implements IBackgroundAuthenticatingExecutorService, IBackgroundAuthenticatingExecution {
    public static final String P_THREAD_POOL = "ThreadPool";
    @Autowired
    protected Executor threadPool;
    @Autowired
    protected ISecurityContextHolder securityContextHolder;
    @Autowired
    protected IThreadLocalCleanupController threadLocalCleanupController;
    @Self
    protected IBackgroundAuthenticatingExecution self;
    @LogInstance
    private ILogger log;

    @Override
    public void startBackgroundWorkerWithAuthentication(CheckedRunnable runnable) {
        // get the current authentication
        var backgroundWorker = createRunnableWithAuthentication(runnable);
        // Using Ambeth Thread pool to get ThreadLocal support e.g. for authentication issues
        threadPool.execute(backgroundWorker);
    }

    @Override
    public <T> T startBackgroundWorkerWithAuthentication(CheckedSupplier<T> runnable) {
        // get the current authentication
        var exchanger = new Exchanger<T>();
        var backgroundWorker = createRunnableWithAuthentication(runnable, exchanger);
        // Using Ambeth Thread pool to get ThreadLocal support e.g. for authentication issues
        threadPool.execute(backgroundWorker);
        try {
            return exchanger.exchange(null);
        } catch (InterruptedException e) {
            Thread.interrupted(); // clear flag
            throw RuntimeExceptionUtil.mask(e);
        }
    }

    @Override
    @SecurityContext(SecurityContextType.AUTHENTICATED)
    public void execute(CheckedRunnable runnable) throws Exception {
        runnable.run();
    }

    @Override
    @SecurityContext(SecurityContextType.AUTHENTICATED)
    public <T> T execute(CheckedSupplier<T> runnable) throws Exception {
        return runnable.get();
    }

    private Runnable createRunnableWithAuthentication(final CheckedRunnable runnable) {
        var authentication = securityContextHolder.getContext().getAuthentication();
        return () -> {
            var rollback = StateRollback.chain(chain -> {
                chain.append(threadLocalCleanupController.pushThreadLocalState());
                chain.append(securityContextHolder.pushAuthentication(authentication));
                self.execute(runnable);
            });
            rollback.rollback();
        };
    }

    private <T> Runnable createRunnableWithAuthentication(final CheckedSupplier<T> runnable, final Exchanger<T> exchanger) {
        var authentication = securityContextHolder.getContext().getAuthentication();
        var backgroundWorker = new ExchangeResultRunnable<>(exchanger, authentication, runnable);
        return backgroundWorker;
    }

    private class ExchangeResultRunnable<T> implements Runnable {
        private final Exchanger<T> exchanger;
        private final IAuthentication authentication;
        private final CheckedSupplier<T> runnable;

        private ExchangeResultRunnable(Exchanger<T> exchanger, IAuthentication authentication, CheckedSupplier<T> runnable) {
            this.exchanger = exchanger;
            this.authentication = authentication;
            this.runnable = runnable;
        }

        @SneakyThrows
        @Override
        public void run() {
            T result = null;
            var rollback = StateRollback.chain(chain -> {
                chain.append(threadLocalCleanupController.pushThreadLocalState());
                chain.append(securityContextHolder.pushAuthentication(authentication));
            });
            try {
                result = runnable.get();
            } finally {
                rollback.rollback();
                try {
                    exchanger.exchange(result);
                } catch (InterruptedException e) {
                    // intended blank
                }
            }
        }
    }
}
