package com.koch.ambeth.ioc.cancel;

import com.koch.ambeth.util.threading.IBackgroundWorkerDelegate;
import com.koch.ambeth.util.threading.IResultingBackgroundWorkerDelegate;
import com.koch.ambeth.util.threading.IResultingBackgroundWorkerParamDelegate;

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

	/**
	 * If - at the moment of calling the method - no {@link ICancellationHandle} is registered to the current thread this method is effectively a NO-OP and just
	 * executes the passed runnable.<br/>
	 * <br/>
	 * But more importantly: If a valid handle is available the current thread is registered to it, the passed runnable is executed and afterwards the current
	 * thread is unregistered again. During the execution the registered thread receives a Thread Interrupt if any thread calls
	 * {@link ICancellationHandle#cancel()} on the corresponding handle.
	 * 
	 * @param runnable
	 */
	void withCancellationAwareness(IBackgroundWorkerDelegate runnable);

	/**
	 * If - at the moment of calling the method - no {@link ICancellationHandle} is registered to the current thread this method is effectively a NO-OP and just
	 * executes the passed runnable.<br/>
	 * <br/>
	 * But more importantly: If a valid handle is available the current thread is registered to it, the passed runnable is executed and afterwards the current
	 * thread is unregistered again. During the execution the registered thread receives a Thread Interrupt if any thread calls
	 * {@link ICancellationHandle#cancel()} on the corresponding handle.
	 * 
	 * @param runnable
	 * @return
	 */
	<R> R withCancellationAwareness(IResultingBackgroundWorkerDelegate<R> runnable);

	/**
	 * If - at the moment of calling the method - no {@link ICancellationHandle} is registered to the current thread this method is effectively a NO-OP and just
	 * executes the passed runnable.<br/>
	 * <br/>
	 * But more importantly: If a valid handle is available the current thread is registered to it, the passed runnable is executed and afterwards the current
	 * thread is unregistered again. During the execution the registered thread receives a Thread Interrupt if any thread calls
	 * {@link ICancellationHandle#cancel()} on the corresponding handle.
	 * 
	 * @param runnable
	 * @param state
	 * @return
	 */
	<R, V> R withCancellationAwareness(IResultingBackgroundWorkerParamDelegate<R, V> runnable, V state);
}
