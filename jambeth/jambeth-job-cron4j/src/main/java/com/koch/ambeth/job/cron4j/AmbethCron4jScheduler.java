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

import com.koch.ambeth.ioc.IDisposableBean;
import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.ioc.extendable.MapExtendableContainer;
import com.koch.ambeth.job.IJob;
import com.koch.ambeth.job.IJobDescheduleCommand;
import com.koch.ambeth.job.IJobExtendable;
import com.koch.ambeth.job.IJobScheduler;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.security.DefaultAuthentication;
import com.koch.ambeth.security.IAuthentication;
import com.koch.ambeth.security.ISecurityContext;
import com.koch.ambeth.security.ISecurityContextHolder;
import com.koch.ambeth.security.PasswordType;
import com.koch.ambeth.util.IClassLoaderProvider;
import com.koch.ambeth.util.ParamChecker;
import com.koch.ambeth.util.collections.IMapEntry;
import it.sauronsoftware.cron4j.Scheduler;
import it.sauronsoftware.cron4j.Task;

import java.util.Map;

public class AmbethCron4jScheduler implements IJobScheduler, IInitializingBean, IDisposableBean, IJobExtendable {
    protected final MapExtendableContainer<IJob, String> jobs = new MapExtendableContainer<IJob, String>("jobId", "job") {
        @Override
        protected int extractHash(IJob key) {
            return System.identityHashCode(key);
        }

        @Override
        protected boolean equalKeys(IJob key, IMapEntry<IJob, Object> entry) {
            return key == entry.getKey();
        }
    };
    @Autowired
    protected IServiceContext beanContext;

    @Autowired
    protected IClassLoaderProvider classLoaderProvider;

    @Autowired
    protected ISecurityContextHolder securityContextHolder;

    @Property(name = "user.name")
    protected String systemUserName;

    protected Scheduler scheduler;
    @LogInstance
    private ILogger log;
    private boolean destroyed;

    @Override
    public void afterPropertiesSet() throws Throwable {
        scheduler = new Scheduler();
    }

    @Override
    public void destroy() throws Throwable {
        destroyed = true;
        final Scheduler scheduler = this.scheduler;
        if (scheduler == null || !scheduler.isStarted()) {
            this.scheduler = null;
            return;
        }
        final Thread stopThread = new Thread(() -> {
            try {
                if (scheduler.isStarted()) {
                    scheduler.stop();
                }
            } catch (Throwable e) {
                log.error(e);
            }
        });
        Thread checkOfStopThread = new Thread(() -> {
            try {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    Thread.interrupted();
                }
                stopThread.interrupt();
            } catch (Throwable e) {
                log.error(e);
            }
        });
        stopThread.setName("Scheduler-StopThread");
        stopThread.setContextClassLoader(classLoaderProvider.getClassLoader());
        stopThread.setDaemon(true);
        checkOfStopThread.setName("Scheduler-StopThread-Check");
        checkOfStopThread.setContextClassLoader(classLoaderProvider.getClassLoader());
        checkOfStopThread.setDaemon(true);
        stopThread.start();
        checkOfStopThread.start();
    }

    @Override
    public void registerJob(IJob job, String jobName, String cronPattern) {
        if (destroyed) {
            throw new IllegalStateException("This bean is already destroyed");
        }
        ParamChecker.assertParamNotNull(job, "job");
        ParamChecker.assertParamNotNull(jobName, "jobName");
        ParamChecker.assertParamNotNull(cronPattern, "cronPattern");
        synchronized (this) {
            if (!scheduler.isStarted()) {
                scheduler.start();
            }
        }
        if (log.isInfoEnabled()) {
            log.info("Scheduling job '" + jobName + "' on '" + cronPattern + "' with type '" + job.getClass().getName() + "'");
        }
        IAuthentication authentication = new DefaultAuthentication(systemUserName, null, PasswordType.PLAIN);
        String jobId = scheduler.schedule(cronPattern, createTask(job, jobName, authentication));
        try {
            jobs.register(jobId, job);
        } catch (RuntimeException e) {
            scheduler.deschedule(jobId);
            throw e;
        }
    }

    @Override
    public void unregisterJob(IJob job, String jobName, String cronPattern) {
        ParamChecker.assertParamNotNull(job, "job");
        ParamChecker.assertParamNotNull(jobName, "jobName");
        ParamChecker.assertParamNotNull(cronPattern, "cronPattern");
        if (destroyed) {
            return;
        }
        if (log.isInfoEnabled()) {
            log.info("Unscheduling job '" + jobName + "' on '" + cronPattern + "' with type '" + job.getClass().getName() + "'");
        }
        String jobId = jobs.getExtension(job);
        scheduler.deschedule(jobId);
        jobs.unregister(jobId, job);
    }

    @Override
    public IJobDescheduleCommand scheduleJob(Class<?> jobType, String cronPattern, Map<Object, Object> properties) {
        return scheduleJob(jobType.getSimpleName(), jobType, cronPattern, properties);
    }

    @Override
    public IJobDescheduleCommand scheduleJob(String jobName, Class<?> jobType, String cronPattern, Map<Object, Object> properties) {
        if (destroyed) {
            throw new IllegalStateException("This bean is already destroyed");
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public IJobDescheduleCommand scheduleJob(String jobName, Object jobTask, String cronPattern, Map<Object, Object> properties) {
        if (destroyed) {
            throw new IllegalStateException("This bean is already destroyed");
        }
        ISecurityContext context = securityContextHolder.getContext();
        IAuthentication authentication = context != null ? context.getAuthentication() : null;
        return scheduleJobIntern(jobName, jobTask, cronPattern, authentication, properties);
    }

    @Override
    public IJobDescheduleCommand scheduleJob(String jobName, Object jobTask, String cronPattern, String username, char[] userpass, Map<Object, Object> properties) {
        ParamChecker.assertParamNotNull(username, "username");
        ParamChecker.assertParamNotNull(userpass, "userpass");
        return scheduleJobIntern(jobName, jobTask, cronPattern, new DefaultAuthentication(username, userpass, PasswordType.PLAIN), properties);
    }

    protected IJobDescheduleCommand scheduleJobIntern(String jobName, final Object jobTask, String cronPattern, IAuthentication authentication, Map<Object, Object> properties) {
        if (destroyed) {
            throw new IllegalStateException("This bean is already destroyed");
        }
        ParamChecker.assertParamNotNull(jobName, "jobName");
        ParamChecker.assertParamNotNull(jobTask, "jobTask");
        ParamChecker.assertParamNotNull(cronPattern, "cronPattern");

        synchronized (this) {
            if (!scheduler.isStarted()) {
                scheduler.start();
            }
        }
        if (log.isInfoEnabled()) {
            String impersonating = "";
            if (authentication != null) {
                impersonating = " impersonating user '" + authentication.getUserName() + "'";
            }
            log.info("Scheduling job '" + jobName + "' on '" + cronPattern + "' with type '" + jobTask.getClass().getName() + "'" + impersonating);
        }
        final String id;
        if (jobTask instanceof IJob) {
            id = scheduler.schedule(cronPattern, createTask((IJob) jobTask, jobName, authentication));
        } else if (jobTask instanceof Task) {
            Task task = (Task) jobTask;
            id = scheduler.schedule(cronPattern, createTask(new TaskJob(task), jobName, authentication));
        } else if (jobTask instanceof Runnable) {
            Runnable runnable = (Runnable) jobTask;
            id = scheduler.schedule(cronPattern, createTask(new RunnableJob(runnable), jobName, authentication));
        } else {
            throw new IllegalArgumentException("JobTask not recognized: " + jobTask);
        }
        return new IJobDescheduleCommand() {
            @Override
            public void execute() {
                scheduler.deschedule(id);
            }
        };
    }

    protected Task createTask(IJob job, String jobName, IAuthentication authentication) {
        return beanContext.registerBean(AmbethCron4jJob.class)//
                          .propertyValue("Job", job)//
                          .propertyValue("JobName", jobName)//
                          .propertyValue("Authentication", authentication)//
                          .finish();
    }
}
