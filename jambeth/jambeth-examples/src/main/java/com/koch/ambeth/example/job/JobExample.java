package com.koch.ambeth.example.job;

/*-
 * #%L
 * jambeth-examples
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

public class JobExample implements IJob {
	@Override
	public boolean canBePaused() {
		return true;
	}

	@Override
	public boolean canBeStopped() {
		return true;
	}

	@Override
	public boolean supportsCompletenessTracking() {
		return true;
	}

	@Override
	public boolean supportsStatusTracking() {
		return true;
	}

	@Override
	public void execute(IJobContext context) throws Exception {
		int count = 100;
		for (int a = 0; a < count; a++) {
			if (context.isStopped()) {
				return;
			}
			context.pauseIfRequested();
			context.setStatusMessage("Step " + a + " of " + count);
			// do something here
			context.setCompleteness((a + 1) / (double) count);
		}
	}
}
