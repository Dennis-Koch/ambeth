package com.koch.ambeth.job;

/*-
 * #%L
 * jambeth-job
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

public interface IJob
{
	/**
	 * <p>
	 * Checks whether this task supports pause requests.
	 * </p>
	 * <p>
	 * Default implementation returns <em>false</em>.
	 * </p>
	 * <p>
	 * Task developers can override this method to let it return a <em>true</em> value, and at the same time they have to implement the
	 * {@link IJob#execute(IJobContext)} method, so that pause requests are really handled. This can be done calling regularly the
	 * {@link IJobContext#pauseIfRequested()} method during the task execution.
	 * </p>
	 * 
	 * @return true if this task can be paused; false otherwise.
	 */
	boolean canBePaused();

	/**
	 * <p>
	 * Checks whether this task supports stop requests.
	 * </p>
	 * <p>
	 * Default implementation returns <em>false</em>.
	 * </p>
	 * <p>
	 * Task developers can override this method to let it return a <em>true</em> value, and at the same time they have to implement the
	 * {@link IJob#execute(IJobContext)} method, so that stop requests are really handled. This can be done checking regularly the
	 * {@link IJobContext#isStopped()} method during the task execution.
	 * </p>
	 * 
	 * @return true if this task can be stopped; false otherwise.
	 */
	boolean canBeStopped();

	/**
	 * <p>
	 * Tests whether this task supports status tracking.
	 * </p>
	 * <p>
	 * Default implementation returns <em>false</em>.
	 * </p>
	 * <p>
	 * The task developer can override this method and returns <em>true</em>, having care to regularly calling the {@link IJobContext#setStatusMessage(String)}
	 * method during the task execution.
	 * </p>
	 * 
	 * @return true if this task, during its execution, provides status message regularly.
	 */
	boolean supportsStatusTracking();

	/**
	 * <p>
	 * Tests whether this task supports completeness tracking.
	 * </p>
	 * <p>
	 * Default implementation returns <em>false</em>.
	 * </p>
	 * <p>
	 * The task developer can override this method and returns <em>true</em>, having care to regularly calling the {@link IJobContext#setCompleteness(double)}
	 * method during the task execution.
	 * </p>
	 * 
	 * @return true if this task, during its execution, provides a completeness value regularly.
	 */
	boolean supportsCompletenessTracking();

	/**
	 * <p>
	 * This method is called to require a task execution, and should contain the core routine of any scheduled task.
	 * </p>
	 * 
	 * <p>
	 * If the <em>execute()</em> method ends regularly the scheduler will consider the execution successfully completed, and this will be communicated to any
	 * {@link SchedulerListener} interested in it. If the <em>execute()</em> method dies throwing a {@link RuntimeException} the scheduler will consider it as a
	 * failure notification. Any {@link SchedulerListener} will be notified about the occurred exception.
	 * </p>
	 * 
	 * @param context
	 *            The execution context.
	 * @throws RuntimeException
	 *             Task execution has somehow failed.
	 */
	void execute(IJobContext context) throws Throwable;
}
