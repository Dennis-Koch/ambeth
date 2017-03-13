package com.koch.ambeth.util;

public interface Lock
{
	LockState releaseAllLocks();

	void reacquireLocks(LockState lockState);

	void lock();

	void unlock();

	boolean tryLock();

	boolean tryLock(long millis);

	boolean isReadLockHeld();

	boolean isWriteLockHeld();

	void lock(Thread lockOwningThread);

	void unlock(Thread lockOwningThread);
}
