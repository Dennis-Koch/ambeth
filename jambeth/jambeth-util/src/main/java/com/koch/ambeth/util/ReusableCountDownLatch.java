package com.koch.ambeth.util;

/*-
 * #%L
 * jambeth-util
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

import com.koch.ambeth.util.objectcollector.Collectable;
import com.koch.ambeth.util.objectcollector.IObjectCollector;

public class ReusableCountDownLatch extends Collectable {
	public static ReusableCountDownLatch create(IObjectCollector objectCollector, final int count) {
		if (count < 0) {
			throw new IllegalArgumentException("count < 0");
		}
		ReusableCountDownLatch latch = objectCollector.create(ReusableCountDownLatch.class);
		latch.count = count;
		return latch;
	}

	@Override
	public void disposeInternDoNotCall() {
		count = 0;
		super.disposeInternDoNotCall();
	}

	private final Object syncObject = new Object();

	private int count;

	public ReusableCountDownLatch() {
	}

	public void await() throws InterruptedException {
		synchronized (syncObject) {
			while (count != 0) {
				syncObject.wait();
			}
			return;
		}
	}

	public boolean countDown() {
		synchronized (syncObject) {
			if (count == 0) {
				throw new IllegalStateException("Latch already " + count);
			}
			count--;
			if (count == 0) {
				syncObject.notifyAll();
				return true;
			}
			return false;
		}
	}

	public int getCount() {
		return count;
	}
}
