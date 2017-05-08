package com.koch.ambeth.util.threading;

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

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.List;

import com.koch.ambeth.util.collections.ArrayList;

public class SensitiveThreadLocal<T> extends ThreadLocal<T> {
	public static enum SensitityLevel {
		IGNORE, CHECK_CLEANUP, HARD_LINER
	}

	private static SensitityLevel sensitivityLevel = SensitityLevel.IGNORE;

	private static volatile int sensitiveThreadLocalID = 0;

	private static final ThreadLocal<Boolean> sensitivityActivatedTL = new ThreadLocal<>();

	private static final List<Reference<SensitiveThreadLocal<?>>> threadLocalInstances =
			new ArrayList<>();

	public static void activateSensitivity() {
		sensitivityLevel = SensitityLevel.HARD_LINER;
	}

	public static void activateCleanupSensitivity() {
		sensitivityLevel = SensitityLevel.CHECK_CLEANUP;
	}

	// public static boolean allowThreadLocalUsage()
	// {
	// if (sensitivityLevel == SensitityLevel.IGNORE)
	// {
	// return false;
	// }
	// Boolean active = sensitivityActivatedTL.get();
	// if (active != null)
	// {
	// return false;
	// }
	// sensitivityActivatedTL.set(Boolean.TRUE);
	// return true;
	// }
	//
	// public static void denyThreadLocalUsage(boolean acquiredThreadLocalUsage, boolean
	// allowValidationException)
	// {
	// if (sensitivityLevel == SensitityLevel.IGNORE || !acquiredThreadLocalUsage)
	// {
	// return;
	// }
	// Boolean active = sensitivityActivatedTL.get();
	// if (active == null)
	// {
	// throw new IllegalStateException("No ThreadLocal usage currently allowed - So nothing to deny");
	// }
	// sensitivityActivatedTL.remove();
	// checkThreadLocalStates(allowValidationException);
	// }

	protected static void checkThreadLocalStates(boolean allowValidationException) {
		synchronized (threadLocalInstances) {
			for (int a = threadLocalInstances.size(); a-- > 0;) {
				SensitiveThreadLocal<?> sensitiveThreadLocal = threadLocalInstances.get(a).get();
				if (sensitiveThreadLocal == null) {
					threadLocalInstances.remove(a);
					continue;
				}
				if (allowValidationException && sensitiveThreadLocal.get() != null) {
					RuntimeException cause = new RuntimeException();
					cause.setStackTrace(sensitiveThreadLocal.allocationTrace);
					throw new IllegalStateException(
							"Memory leak occuring: ThreadLocal with id '" + sensitiveThreadLocal.id
									+ "' contains a value but usage of TLs is forbidden at this point: "
									+ sensitiveThreadLocal,
							cause);
				}
			}
		}
	}

	private final int id;

	private final StackTraceElement[] allocationTrace;

	public SensitiveThreadLocal() {
		super();
		if (sensitivityLevel != SensitityLevel.IGNORE) {
			allocationTrace = Thread.currentThread().getStackTrace();
			synchronized (threadLocalInstances) {
				id = ++sensitiveThreadLocalID;
				threadLocalInstances.add(new WeakReference<SensitiveThreadLocal<?>>(this));
			}
		}
		else {
			id = 0;
			allocationTrace = null;
		}
	}

	@Override
	public void set(T value) {
		if (value != null && sensitivityLevel == SensitityLevel.HARD_LINER) {
			if (sensitivityActivatedTL.get() == null) {
				throw new IllegalStateException(
						"It is not permitted to write to a ThreadLocal without using mandatory Ambeth ThreadLocal Cleanup for robustness");
			}
		}
		super.set(value);
	}
}
