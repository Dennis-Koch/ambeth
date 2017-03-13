package com.koch.ambeth.merge.cache;

import com.koch.ambeth.util.threading.IBackgroundWorkerDelegate;

public interface ICacheModification
{
	boolean isInternalUpdate();

	void setInternalUpdate(boolean internalUpdate);

	boolean isActive();

	void setActive(boolean active);

	boolean isActiveOrFlushing();

	boolean isActiveOrFlushingOrInternalUpdate();

	void queuePropertyChangeEvent(IBackgroundWorkerDelegate task);
}