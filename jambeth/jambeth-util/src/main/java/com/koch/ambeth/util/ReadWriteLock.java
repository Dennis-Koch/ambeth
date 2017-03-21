package com.koch.ambeth.util;

/*-
 * #%L
 * jambeth-util
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

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.koch.ambeth.util.exception.RuntimeExceptionUtil;

public class ReadWriteLock {
	protected final Lock readLock;

	protected final Lock writeLock;

	protected final ReentrantReadWriteLock rwlSlim;

	public ReadWriteLock() {
		this(false, DebugMode.FALSE);
	}

	public ReadWriteLock(DebugMode debugMode) {
		this(false, debugMode);
	}

	public ReadWriteLock(boolean fair) {
		this(fair, DebugMode.FALSE);
	}

	public ReadWriteLock(boolean fair, DebugMode debugMode) {
		rwlSlim = new ReentrantReadWriteLock(fair);
		readLock = new ReadLock(this, rwlSlim);
		writeLock = new WriteLock(this, rwlSlim, debugMode);
	}

	public Lock getReadLock() {
		return readLock;
	}

	public Lock getWriteLock() {
		return writeLock;
	}

	public LockState releaseAllLocks() {
		ReentrantReadWriteLock rwlSlim = this.rwlSlim;
		int readLockCounter = 0;
		while (rwlSlim.getReadHoldCount() > 0) {
			rwlSlim.readLock().unlock();
			readLockCounter++;
		}
		int writeLockCounter = 0;
		while (rwlSlim.isWriteLockedByCurrentThread()) {
			rwlSlim.writeLock().unlock();
			writeLockCounter++;
		}
		if (readLockCounter == 0 && writeLockCounter == 0) {
			return null;
		}
		LockState lockState = new LockState();
		lockState.readLockCount = readLockCounter;
		lockState.writeLockCount = writeLockCounter;
		return lockState;
	}

	public void reacquireLocks(LockState lockState) {
		if (lockState == null) {
			return;
		}
		int writeLockCounter = lockState.writeLockCount;
		int readLockCounter = lockState.readLockCount;
		while (writeLockCounter-- > 0) {
			rwlSlim.writeLock().lock();
		}
		while (readLockCounter-- > 0) {
			rwlSlim.readLock().lock();
		}
	}

	public static class ReadLock implements Lock {

		protected final ReentrantReadWriteLock rwlSlim;

		protected final ReadWriteLock rwLock;

		protected final java.util.concurrent.locks.Lock lock;

		public ReadLock(ReadWriteLock rwLock, ReentrantReadWriteLock rwlSlim) {
			this.rwLock = rwLock;
			this.rwlSlim = rwlSlim;
			lock = rwlSlim.readLock();
		}

		@Override
		public LockState releaseAllLocks() {
			return rwLock.releaseAllLocks();
		}

		@Override
		public void reacquireLocks(LockState lockState) {
			rwLock.reacquireLocks(lockState);
		}

		@Override
		public void lock() {
			lock.lock();
		}

		@Override
		public void lock(Thread lockOwningThread) {
			throw new UnsupportedOperationException("Not implemented");
		}

		@Override
		public void unlock() {
			lock.unlock();
		}

		@Override
		public void unlock(Thread lockOwningThread) {
			throw new UnsupportedOperationException("Not implemented");
		}

		@Override
		public boolean tryLock() {
			return lock.tryLock();
		}

		@Override
		public boolean tryLock(long millis) {
			try {
				return lock.tryLock(millis, TimeUnit.MILLISECONDS);
			}
			catch (InterruptedException e) {
				throw RuntimeExceptionUtil.mask(e);
			}
		}

		@Override
		public boolean isReadLockHeld() {
			return rwlSlim.getReadHoldCount() > 0;
		}

		@Override
		public boolean isWriteLockHeld() {
			return rwlSlim.getWriteHoldCount() > 0;
		}
	}

	public static class WriteLock implements Lock {
		protected final ReadWriteLock rwLock;

		protected final ReentrantReadWriteLock rwlSlim;

		protected final java.util.concurrent.locks.Lock lock;

		protected final DebugMode debugMode;

		public WriteLock(ReadWriteLock rwLock, ReentrantReadWriteLock rwlSlim, DebugMode debugMode) {
			this.rwLock = rwLock;
			this.rwlSlim = rwlSlim;
			this.debugMode = debugMode;
			lock = rwlSlim.writeLock();
		}

		@Override
		public LockState releaseAllLocks() {
			return rwLock.releaseAllLocks();
		}

		@Override
		public void reacquireLocks(LockState lockState) {
			rwLock.reacquireLocks(lockState);
		}

		@Override
		public void lock() {
			if (debugMode == DebugMode.TRUE) {
				if (isReadLockHeld()) {
					throw new IllegalStateException("Can not upgrade to writeLock if readLock already held");
				}
			}
			lock.lock();
		}

		@Override
		public void lock(Thread lockOwningThread) {
			throw new UnsupportedOperationException("Not implemented");
		}

		@Override
		public void unlock() {
			lock.unlock();
		}

		@Override
		public void unlock(Thread lockOwningThread) {
			throw new UnsupportedOperationException("Not implemented");
		}

		@Override
		public boolean tryLock() {
			return lock.tryLock();
		}

		@Override
		public boolean tryLock(long millis) {
			try {
				return lock.tryLock(millis, TimeUnit.MILLISECONDS);
			}
			catch (InterruptedException e) {
				throw RuntimeExceptionUtil.mask(e);
			}
		}

		@Override
		public boolean isReadLockHeld() {
			return rwlSlim.getReadHoldCount() > 0;
		}

		@Override
		public boolean isWriteLockHeld() {
			return rwlSlim.getWriteHoldCount() > 0;
		}
	}
}
