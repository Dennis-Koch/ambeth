package de.osthus.ambeth.cancel;

import de.osthus.ambeth.state.IStateRollback;

public interface ICancellationWritable
{
	/**
	 * Creates a new {@link ICancellationHandle} without assigning it to the current thread. This makes sense together with calling later
	 * {@link #pushCancellationHandle(ICancellationHandle)} for other threads.
	 * 
	 * @return A new {@link ICancellationHandle} not assigned to any thread, yet
	 */
	ICancellationHandle createUnassignedCancellationHandle();

	/**
	 * Assigns a custom cancellation handle to the current thread. A later call to {@link #getEnsureCancellationHandle()} will return this instance.
	 * 
	 * @param cancellationHandle
	 *            a valid {@link ICancellationHandle}
	 * @return The rollback handle to restore the state of the {@link ICancellationWritable} before calling this method.
	 */
	IStateRollback pushCancellationHandle(ICancellationHandle cancellationHandle);

	/**
	 * Retrieves the {@link ICancellationHandle} currently assigned to this thread or creates a new one if this thread does not yet have a
	 * {@link ICancellationHandle} assigned.
	 * 
	 * @return A valid instance of a {@link ICancellationHandle}. Never returns null
	 */
	ICancellationHandle getEnsureCancellationHandle();
}
