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

import com.koch.ambeth.job.IJob;
import com.koch.ambeth.job.IJobContext;

import it.sauronsoftware.cron4j.Task;

public class TaskJob implements IJob {
	private final Task task;

	public TaskJob(Task task) {
		this.task = task;
	}

	@Override
	public boolean canBePaused() {
		return task.canBePaused();
	}

	@Override
	public boolean canBeStopped() {
		return task.canBeStopped();
	}

	@Override
	public boolean supportsStatusTracking() {
		return task.supportsStatusTracking();
	}

	@Override
	public boolean supportsCompletenessTracking() {
		return task.supportsCompletenessTracking();
	}

	@Override
	public void execute(IJobContext context) throws Exception {
		task.execute(((AmbethCron4jJobContext) context).getTaskExecutionContext());
	}
}
