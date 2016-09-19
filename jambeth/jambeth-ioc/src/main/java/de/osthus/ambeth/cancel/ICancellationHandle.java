package de.osthus.ambeth.cancel;

public interface ICancellationHandle extends AutoCloseable
{
	/**
	 * Evaluates whether the current handle has been cancelled.
	 * 
	 * @return true if any thread called {@link #cancel()} before
	 */
	boolean isCancelled();

	/**
	 * Call this method from any thread with a valid {@link ICancellationHandle} created from the to-be-cancelled thread. So a to-be-cancelled thread first has
	 * to create and assign to him a {@link ICancellationHandle} by calling {@link #getEnsureCancellationHandle()}. This handle has to be picked up by
	 * application code from any other thread to call {@link #cancel()} from there.
	 */
	void cancel();
}
