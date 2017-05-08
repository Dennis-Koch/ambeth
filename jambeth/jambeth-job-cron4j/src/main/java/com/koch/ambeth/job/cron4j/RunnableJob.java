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

public class RunnableJob implements IJob {
	private final Runnable runnable;

	public RunnableJob(Runnable runnable) {
		this.runnable = runnable;
	}

	@Override
	public boolean canBePaused() {
		return false;
	}

	@Override
	public boolean canBeStopped() {
		return false;
	}

	@Override
	public boolean supportsStatusTracking() {
		return false;
	}

	@Override
	public boolean supportsCompletenessTracking() {
		return false;
	}

	@Override
	public void execute(IJobContext context) throws Throwable {
		runnable.run();
	}
}
