package de.osthus.ambeth.merge;

import de.osthus.ambeth.threading.IBackgroundWorkerDelegate;
import de.osthus.ambeth.threading.IResultingBackgroundWorkerDelegate;

public interface ILightweightTransaction
{
	boolean isActive();

	void runInTransaction(IBackgroundWorkerDelegate runnable);

	<R> R runInTransaction(IResultingBackgroundWorkerDelegate<R> runnable);

	<R> R runInLazyTransaction(IResultingBackgroundWorkerDelegate<R> runnable);

	void runOnTransactionPreCommit(IBackgroundWorkerDelegate runnable);
}
