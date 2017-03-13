package com.koch.ambeth.merge;

import com.koch.ambeth.util.threading.IBackgroundWorkerDelegate;
import com.koch.ambeth.util.threading.IResultingBackgroundWorkerDelegate;

public interface ILightweightTransaction
{
	boolean isActive();

	void runInTransaction(IBackgroundWorkerDelegate runnable);

	<R> R runInTransaction(IResultingBackgroundWorkerDelegate<R> runnable);

	<R> R runInLazyTransaction(IResultingBackgroundWorkerDelegate<R> runnable);

	void runOnTransactionPreCommit(IBackgroundWorkerDelegate runnable);

	void runOnTransactionPostCommit(IBackgroundWorkerDelegate runnable);
}
