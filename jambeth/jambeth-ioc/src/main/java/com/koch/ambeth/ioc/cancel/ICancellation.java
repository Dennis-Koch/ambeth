package com.koch.ambeth.ioc.cancel;

/*-
 * #%L
 * jambeth-ioc
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

import com.koch.ambeth.util.threading.IBackgroundWorkerDelegate;
import com.koch.ambeth.util.threading.IResultingBackgroundWorkerDelegate;
import com.koch.ambeth.util.threading.IResultingBackgroundWorkerParamDelegate;

public interface ICancellation {
	/**
	 * Checks if the current thread is marked to cancel its work. The flag itself will be set if any
	 * other thread called {@link #cancel(ICancellationHandle) previously passing the handle
	 * corresponding to the current thread. If the current thread never called
	 * {@link #getEnsureCancellationHandle()} there is no way to get this handle and no way to cancel
	 * the current thread. In those cases {@link #isCancelled()} will always return false.
	 *
	 * @return true, if the current thread is marked to cancel its work
	 */
	boolean isCancelled();

	/**
	 * Calls internally {@link #isCancelled()} . If that returns true a {@link CancelledException} is
	 * thrown. Call this method in regular intervals to make sure the process terminates gracefully
	 * when the current threads is marked to cancel its work.
	 *
	 * @throws CancelledException if {@link #isCancelled()} returns true
	 */
	void ensureNotCancelled() throws CancelledException;

	/**
	 * Retrieves the {@link ICancellationHandle} currently assigned to this thread or creates a new
	 * one if this thread does not yet have a {@link ICancellationHandle} assigned.
	 *
	 * @return A valid instance of a {@link ICancellationHandle}. Never returns null
	 */
	ICancellationHandle getEnsureCancellationHandle();

	/**
	 * If - at the moment of calling the method - no {@link ICancellationHandle} is registered to the
	 * current thread this method is effectively a NO-OP and just executes the passed runnable.<br>
	 * <br>
	 * But more importantly: If a valid handle is available the current thread is registered to it,
	 * the passed runnable is executed and afterwards the current thread is unregistered again. During
	 * the execution the registered thread receives a Thread Interrupt if any thread calls
	 * {@link ICancellationHandle#cancel()} on the corresponding handle.
	 *
	 * @param runnable
	 */
	void withCancellationAwareness(IBackgroundWorkerDelegate runnable);

	/**
	 * If - at the moment of calling the method - no {@link ICancellationHandle} is registered to the
	 * current thread this method is effectively a NO-OP and just executes the passed runnable.<br>
	 * <br>
	 * But more importantly: If a valid handle is available the current thread is registered to it,
	 * the passed runnable is executed and afterwards the current thread is unregistered again. During
	 * the execution the registered thread receives a Thread Interrupt if any thread calls
	 * {@link ICancellationHandle#cancel()} on the corresponding handle.
	 *
	 * @param runnable
	 * @return
	 */
	<R> R withCancellationAwareness(IResultingBackgroundWorkerDelegate<R> runnable);

	/**
	 * If - at the moment of calling the method - no {@link ICancellationHandle} is registered to the
	 * current thread this method is effectively a NO-OP and just executes the passed runnable.<br>
	 * <br>
	 * But more importantly: If a valid handle is available the current thread is registered to it,
	 * the passed runnable is executed and afterwards the current thread is unregistered again. During
	 * the execution the registered thread receives a Thread Interrupt if any thread calls
	 * {@link ICancellationHandle#cancel()} on the corresponding handle.
	 *
	 * @param runnable
	 * @param state
	 * @return
	 */
	<R, V> R withCancellationAwareness(IResultingBackgroundWorkerParamDelegate<R, V> runnable,
			V state);
}
