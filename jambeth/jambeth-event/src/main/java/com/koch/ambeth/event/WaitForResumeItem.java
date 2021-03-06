package com.koch.ambeth.event;

/*-
 * #%L
 * jambeth-event
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

import java.util.concurrent.CountDownLatch;

import com.koch.ambeth.util.collections.IdentityHashSet;

public class WaitForResumeItem implements IProcessResumeItem {
	protected final IdentityHashSet<Object> pendingPauses;

	protected final CountDownLatch latch = new CountDownLatch(1);

	protected final CountDownLatch resultLatch = new CountDownLatch(1);

	public WaitForResumeItem(IdentityHashSet<Object> pendingPauses) {
		this.pendingPauses = pendingPauses;
	}

	public IdentityHashSet<Object> getPendingPauses() {
		return pendingPauses;
	}

	public CountDownLatch getLatch() {
		return latch;
	}

	public CountDownLatch getResultLatch() {
		return resultLatch;
	}

	@Override
	public void resumeProcessingFinished() {
		resultLatch.countDown();
	}
}
