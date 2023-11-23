package com.koch.ambeth.job.cron4j;

/*-
 * #%L
 * jambeth-job-cron4j
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

import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.ioc.threadlocal.IThreadLocalCleanupController;
import com.koch.ambeth.job.IJob;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.security.IAuthentication;
import com.koch.ambeth.security.ISecurityContextHolder;
import com.koch.ambeth.util.state.StateRollback;
import it.sauronsoftware.cron4j.Task;
import it.sauronsoftware.cron4j.TaskExecutionContext;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class AmbethCron4jJob extends Task {
    @Autowired(optional = true)
    protected IAuthentication authentication;
    @Autowired
    protected IServiceContext beanContext;
    @Autowired
    protected ISecurityContextHolder securityContextHolder;
    @Autowired
    protected IJob job;
    @Property
    protected String jobName;
    protected Lock writeLock = new ReentrantLock();
    protected Lock waitingLock = new ReentrantLock();
    @LogInstance
    private ILogger log;

    @Override
    public void execute(TaskExecutionContext context) throws RuntimeException {
        var waitingLock = this.waitingLock;
        if (!waitingLock.tryLock()) {
            return;
        }
        var writeLock = this.writeLock;
        try {
            writeLock.lock();
        } finally {
            waitingLock.unlock();
        }
        try {
            var thread = Thread.currentThread();

            var tlCleanupController = beanContext.getService(IThreadLocalCleanupController.class);
            var oldName = thread.getName();
            var rollback = tlCleanupController.pushThreadLocalState();
            try {
                thread.setName("Job " + jobName);
                var jobContext = beanContext.registerBean(AmbethCron4jJobContext.class)//
                                            .propertyValue("TaskExecutionContext", context)//
                                            .finish();

                var start = System.currentTimeMillis();
                if (log.isDebugEnabled()) {
                    log.debug("Executing job '" + jobName + "'");
                }
                try {
                    if (authentication != null) {
                        rollback = StateRollback.prepend(securityContextHolder.pushAuthentication(authentication), rollback);
                    }
                    job.execute(jobContext);
                    if (log.isDebugEnabled()) {
                        long end = System.currentTimeMillis();
                        log.debug("Execution of job '" + jobName + "' finished (" + (end - start) + " ms)");
                    }
                } catch (Throwable e) {
                    if (log.isErrorEnabled()) {
                        log.error("Error occured while executing job '" + jobName + "'", e);
                    }
                }
            } finally {
                thread.setName(oldName);
                rollback.rollback();
            }
        } finally {
            writeLock.unlock();
        }
    }
}
