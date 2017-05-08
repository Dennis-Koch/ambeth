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

import com.koch.ambeth.util.IDisposable;

public class PausedEventTargetItem implements IDisposable {
	protected final Object eventTarget;

	protected final Thread thread = Thread.currentThread();

	protected int pauseCount;

	protected volatile CountDownLatch latch;

	public PausedEventTargetItem(Object eventTarget) {
		this.eventTarget = eventTarget;
	}

	@Override
	public void dispose() {
		if (latch != null) {
			latch.countDown();
			latch = null;
		}
	}

	public CountDownLatch addLatch() {
		if (latch == null) {
			latch = new CountDownLatch(1);
		}
		return latch;
	}

	public void setLatch(CountDownLatch latch) {
		if (this.latch != null) {
			throw new IllegalStateException();
		}
		this.latch = latch;
	}

	public Object getEventTarget() {
		return eventTarget;
	}

	public int getPauseCount() {
		return pauseCount;
	}

	public void setPauseCount(int pauseCount) {
		this.pauseCount = pauseCount;
	}

	public Thread getThread() {
		return thread;
	}

	@Override
	public int hashCode() {
		return 11 ^ System.identityHashCode(eventTarget);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof PausedEventTargetItem)) {
			return false;
		}
		PausedEventTargetItem other = (PausedEventTargetItem) obj;
		return eventTarget == other.eventTarget;
	}
}
