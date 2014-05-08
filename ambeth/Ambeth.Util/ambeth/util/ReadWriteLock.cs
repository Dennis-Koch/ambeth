using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading;

namespace De.Osthus.Ambeth.Util
{
    internal class ReadLock : Lock
    {
#if !SILVERLIGHT
        protected readonly ReaderWriterLockSlim rwlSlim;

        protected readonly ReadWriteLock rwLock;

        public ReadLock(ReadWriteLock rwLock)
        {
            this.rwLock = rwLock;
            this.rwlSlim = rwLock.rwlSlim;
        }

        public void Lock()
        {
            rwlSlim.EnterReadLock();
        }

        public void Unlock()
        {
            rwlSlim.ExitReadLock();
        }

        public bool TryLock()
        {
            return rwlSlim.TryEnterReadLock(0);
        }

        public bool TryLock(int millis)
        {
            return rwlSlim.TryEnterReadLock(millis);
        }

        public bool TryLock(TimeSpan timeSpan)
        {
            return rwlSlim.TryEnterReadLock(timeSpan);
        }

        public bool IsReadLockHeld
        {
            get
            {
                return rwlSlim.IsReadLockHeld;
            }
        }

        public bool IsWriteLockHeld
        {
            get
            {
                return rwlSlim.IsWriteLockHeld;
            }
        }
#else
        protected ReadWriteLock rwLock;

        public ReadLock(ReadWriteLock rwLock)
        {
            this.rwLock = rwLock;
        }

        public void Lock()
        {
            lock (rwLock.internalLock)
            {
                if (rwLock.IsCounted(rwLock.threadToReadCountDict))
                {
                    rwLock.IncrementCounter(rwLock.threadToReadCountDict);
                    return;
                }
                if (rwLock.IsCounted(rwLock.threadToWriteCountDict))
                {
                    rwLock.IncrementCounter(rwLock.threadToReadCountDict);
                    return;
                }
                while (rwLock.threadToWriteCountDict.Count > 0)
                {
                    Monitor.Wait(rwLock.internalLock);
                }
                rwLock.IncrementCounter(rwLock.threadToReadCountDict);
            }
        }

        public void Unlock()
        {
            lock (rwLock.internalLock)
            {
                rwLock.DecrementCounter(rwLock.threadToReadCountDict);
                Monitor.PulseAll(rwLock.internalLock);
            }
        }

        public bool TryLock()
        {
            Lock();
            return true;
        }

        public bool TryLock(int millis)
        {
            return TryLock();
        }

        public bool TryLock(TimeSpan timeSpan)
        {
            return TryLock();
        }

        public bool IsReadLockHeld
        {
            get
            {
                return rwLock.IsReadLockHeld;
            }
        }

        public bool IsWriteLockHeld
        {
            get
            {
                return rwLock.IsWriteLockHeld;
            }
        }
        
#endif
        public LockState ReleaseAllLocks()
        {
            return rwLock.ReleaseAllLocks();
        }

        public void ReacquireLocks(LockState lockState)
        {
            rwLock.ReacquireLocks(lockState);
        }
    }

    internal class WriteLock : Lock
    {
#if !SILVERLIGHT
         protected readonly ReaderWriterLockSlim rwlSlim;

        protected readonly ReadWriteLock rwLock;

        public WriteLock(ReadWriteLock rwLock)
        {
            this.rwLock = rwLock;
            this.rwlSlim = rwLock.rwlSlim;
        }

        public void Lock()
        {
            rwlSlim.EnterWriteLock();
        }

        public void Unlock()
        {
            rwlSlim.ExitWriteLock();
        }

        public bool TryLock()
        {
            return rwlSlim.TryEnterWriteLock(0);
        }

        public bool TryLock(int millis)
        {
            return rwlSlim.TryEnterWriteLock(millis);
        }

        public bool TryLock(TimeSpan timeSpan)
        {
            return rwlSlim.TryEnterWriteLock(timeSpan);
        }

        public bool IsReadLockHeld
        {
            get
            {
                return rwlSlim.IsReadLockHeld;
            }
        }

        public bool IsWriteLockHeld
        {
            get
            {
                return rwlSlim.IsWriteLockHeld;
            }
        }
#else
        protected ReadWriteLock rwLock;

        public WriteLock(ReadWriteLock rwLock)
        {
            this.rwLock = rwLock;
        }

        public void Lock()
        {
            lock (rwLock.internalLock)
            {
                if (rwLock.IsCounted(rwLock.threadToWriteCountDict))
                {
                    rwLock.IncrementCounter(rwLock.threadToWriteCountDict);
                    return;
                }
                if (rwLock.IsCounted(rwLock.threadToReadCountDict))
                {
                    throw new NotSupportedException("It is not allowed to aquire a write lock while maintaining a readlock");
                }
                while (rwLock.threadToWriteCountDict.Count > 0)
                {
                    Monitor.Wait(rwLock.internalLock);
                }
                rwLock.IncrementCounter(rwLock.threadToWriteCountDict);
                while (rwLock.threadToReadCountDict.Count > 0)
                {
                    Monitor.Wait(rwLock.internalLock);
                }
            }
        }

        public void Unlock()
        {
            lock (rwLock.internalLock)
            {
                rwLock.DecrementCounter(rwLock.threadToWriteCountDict);
                Monitor.PulseAll(rwLock.internalLock);
            }
        }

        public bool TryLock()
        {
            Lock();
            return true;
        }

        public bool TryLock(int millis)
        {
            return TryLock();
        }

        public bool TryLock(TimeSpan timeSpan)
        {
            return TryLock();
        }

        public bool IsReadLockHeld
        {
            get
            {
                return rwLock.IsReadLockHeld;
            }
        }

        public bool IsWriteLockHeld
        {
            get
            {
                return rwLock.IsWriteLockHeld;
            }
        }
#endif
        public LockState ReleaseAllLocks()
        {
            return rwLock.ReleaseAllLocks();
        }

        public void ReacquireLocks(LockState lockState)
        {
            rwLock.ReacquireLocks(lockState);
        }
    }

    public class ReadWriteLock
    {
#if !SILVERLIGHT
        internal readonly ReaderWriterLockSlim rwlSlim = new ReaderWriterLockSlim(LockRecursionPolicy.SupportsRecursion);
#else
        internal readonly Dictionary<Thread, int> threadToReadCountDict = new Dictionary<Thread,int>();

        internal readonly Dictionary<Thread, int> threadToWriteCountDict = new Dictionary<Thread, int>();

        internal readonly Object internalLock = new Object();

#endif

        public ReadWriteLock()
        {
            ReadLock = new ReadLock(this);
            WriteLock = new WriteLock(this);
        }

#if !SILVERLIGHT
        public LockState ReleaseAllLocks()
        {
            int readLockCounter = 0;
            while (rwlSlim.IsReadLockHeld)
            {
                rwlSlim.ExitReadLock();
                readLockCounter++;
            }
            int writeLockCounter = 0;
            while (rwlSlim.IsWriteLockHeld)
            {
                rwlSlim.ExitWriteLock();
                writeLockCounter++;
            }            
            LockState lockState = new LockState();
            lockState.readLockCount = readLockCounter;
            lockState.writeLockCount = writeLockCounter;
            return lockState;
        }

        public void ReacquireLocks(LockState lockState)
        {
            int writeLockCounter = lockState.writeLockCount;
            int readLockCounter = lockState.readLockCount;
            while (writeLockCounter-- > 0)
            {
                rwlSlim.EnterWriteLock();
            }
            while (readLockCounter-- > 0)
            {
                rwlSlim.EnterReadLock();
            }
        }
#else
        internal String CurrentReadLockThreadIds()
        {
            StringBuilder sb = new StringBuilder();
            bool first = true;
            DictionaryExtension.Loop(threadToReadCountDict, delegate(Thread thread, int count)
            {
                if (!first)
                {
                    sb.Append(',');
                }
                else
                {
                    first = false;
                }
                sb.Append(thread.ManagedThreadId).Append('(').Append(count).Append(')');
            });
            return sb.ToString();
        }

        internal String CurrentWriteLockThreadIds()
        {
            StringBuilder sb = new StringBuilder();
            bool first = true;
            DictionaryExtension.Loop(threadToWriteCountDict, delegate(Thread thread, int count)
            {
                if (!first)
                {
                    sb.Append(',');
                }
                else
                {
                    first = false;
                }
                sb.Append(thread.ManagedThreadId).Append('(').Append(count).Append(')');
            });
            return sb.ToString();
        }

        internal void IncrementCounter(Dictionary<Thread, int> threadToCountDict)
        {
            Thread thread = Thread.CurrentThread;
            int counter = DictionaryExtension.ValueOrDefault(threadToCountDict, thread);
            counter++;
            threadToCountDict[thread] = counter;
        }

        internal void DecrementCounter(Dictionary<Thread, int> threadToCountDict)
        {
            Thread thread = Thread.CurrentThread;
            int counter = DictionaryExtension.ValueOrDefault(threadToCountDict, thread);
            counter--;
            if (counter > 0)
            {
                threadToCountDict[thread] = counter;
            }
            else
            {
                threadToCountDict.Remove(thread);
            }
        }

        internal bool IsCounted(Dictionary<Thread, int> threadToCountDict)
        {
            Thread thread = Thread.CurrentThread;
            int counter = DictionaryExtension.ValueOrDefault(threadToCountDict, thread);
            return (counter > 0);
        }

        internal bool IsReadLockHeld
        {
            get
            {
                return IsCounted(threadToReadCountDict);
            }
        }

        internal bool IsWriteLockHeld
        {
            get
            {
                return IsCounted(threadToWriteCountDict);
            }
        }

        public LockState ReleaseAllLocks()
        {
            int readLockCounter = 0;
            int writeLockCounter = 0;
            lock (internalLock)
            {
                while (IsReadLockHeld)
                {
                    DecrementCounter(threadToReadCountDict);
                    readLockCounter++;
                }
                while (IsWriteLockHeld)
                {
                    DecrementCounter(threadToWriteCountDict);
                    writeLockCounter++;
                }
                Monitor.PulseAll(internalLock);
            }
            LockState lockState = new LockState();
            lockState.readLockCount = readLockCounter;
            lockState.writeLockCount = writeLockCounter;
            return lockState;
        }

        public void ReacquireLocks(LockState lockState)
        {
            int writeLockCounter = lockState.writeLockCount;
            int readLockCounter = lockState.readLockCount;
            while (writeLockCounter-- > 0)
            {
                WriteLock.Lock();
            }
            while (readLockCounter-- > 0)
            {
                ReadLock.Lock();
            }
        }
#endif

        public Lock ReadLock { get; protected set; }

        public Lock WriteLock { get; protected set; }
        
    }
}
