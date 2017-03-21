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

import com.koch.ambeth.util.state.IStateRollback;

public interface ICancellationWritable {
	/**
	 * Creates a new {@link ICancellationHandle} without assigning it to the current thread. This
	 * makes sense together with calling later {@link #pushCancellationHandle(ICancellationHandle)}
	 * for other threads.
	 *
	 * @return A new {@link ICancellationHandle} not assigned to any thread, yet
	 */
	ICancellationHandle createUnassignedCancellationHandle();

	/**
	 * Assigns a custom cancellation handle to the current thread. A later call to
	 * {@link #getEnsureCancellationHandle()} will return this instance.
	 *
	 * @param cancellationHandle a valid {@link ICancellationHandle}
	 * @return The rollback handle to restore the state of the {@link ICancellationWritable} before
	 *         calling this method.
	 */
	IStateRollback pushCancellationHandle(ICancellationHandle cancellationHandle);

	/**
	 * Retrieves the {@link ICancellationHandle} currently assigned to this thread or creates a new
	 * one if this thread does not yet have a {@link ICancellationHandle} assigned.
	 *
	 * @return A valid instance of a {@link ICancellationHandle}. Never returns null
	 */
	ICancellationHandle getEnsureCancellationHandle();
}
