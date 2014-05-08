package de.osthus.ambeth.cache;

import de.osthus.ambeth.threading.IBackgroundWorkerDelegate;

public interface ICacheModification
{
	boolean isActive();

	void setActive(boolean active);

	boolean isActiveOrFlushing();

	void queuePropertyChangeEvent(IBackgroundWorkerDelegate task);
}