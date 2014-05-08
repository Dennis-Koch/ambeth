using System;

namespace De.Osthus.Ambeth.Util
{
    public interface Lock
    {
        LockState ReleaseAllLocks();

        void ReacquireLocks(LockState lockState);

        void Lock();

        void Unlock();

        bool TryLock();

        bool TryLock(int millis);

        bool TryLock(TimeSpan timeSpan);

        bool IsReadLockHeld { get; }

        bool IsWriteLockHeld { get; }
    }
}
