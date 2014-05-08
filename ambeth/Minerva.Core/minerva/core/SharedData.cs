using System;
using System.Net;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Documents;
using System.Windows.Ink;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Animation;
using System.Windows.Shapes;
using System.Reflection;
using De.Osthus.Minerva.Extendable;
using De.Osthus.Ambeth.Ioc;
using System.Collections.Generic;
using De.Osthus.Ambeth.Util;
using De.Osthus.Ambeth.Ioc.Extendable;
using De.Osthus.Ambeth.Threading;
using System.Threading;
using De.Osthus.Ambeth.Collections;

namespace De.Osthus.Minerva.Core
{
    public class SharedData : IInitializingBean, IStartingBean, ISharedData, ISharedDataHandOnExtendable
    {
        // This must not be a struct!
        public class SharedDataItem
        {
            public readonly IDictionary<String, IModelContainer> data;

            public DateTime lastAccess;

            public SharedDataItem(IDictionary<String, IModelContainer> data)
            {
                this.data = data;
            }
        }

        protected readonly IDictionary<String, SharedDataItem> tokenToDataDict = new Dictionary<String, SharedDataItem>();

        protected readonly IDictionary<String, ISet<ISharedDataHandOn>> tokenToHandOnsDict = new Dictionary<String, ISet<ISharedDataHandOn>>();

        protected readonly Lock readLock, writeLock;

        public IThreadPool ThreadPool { get; set; }

        public TimeSpan CleanUpInterval { get; set; }

        public SharedData()
        {
            ReadWriteLock rwLock = new ReadWriteLock();
            readLock = rwLock.ReadLock;
            writeLock = rwLock.WriteLock;
            CleanUpInterval = TimeSpan.FromSeconds(60);
        }

        public virtual void AfterPropertiesSet()
        {
            ParamChecker.AssertNotNull(ThreadPool, "ThreadPool"); 
        }

        public virtual void AfterStarted()
        {
            ThreadPool.Queue(CleanUpRunnable);
        }

        protected void CleanUpRunnable()
        {
            try
            {
                while (true)
                {
                    writeLock.Lock();
                    try
                    {
                        DateTime currentTime = DateTime.Now;
                        DictionaryExtension.LoopModifiable(tokenToDataDict, delegate(String token, SharedDataItem data)
                        {
                            if (currentTime - data.lastAccess >= CleanUpInterval)
                            {
                                CleanUp(token);
                            }
                        });
                    }
                    finally
                    {
                        writeLock.Unlock();
                    }
                    Thread.Sleep(CleanUpInterval);
                }
            }
            catch (ThreadAbortException)
            {
                // This occurs if some 'bad guy' aborts our thread. We expect intentional behaviour here. No need to throw the exception uncaught
                // Intended blank
            }
        }

        protected void CleanUp(String token)
        {
            ISet<ISharedDataHandOn> registeredHandOns = tokenToHandOnsDict[token];
            if (registeredHandOns.Count == 0)
            {
                tokenToDataDict.Remove(token);
                tokenToHandOnsDict.Remove(token);
            }
        }

        public void RegisterSharedDataHandOn(ISharedDataHandOn sharedDataHandOn, String token)
        {
            writeLock.Lock();
            try
            {
                // Force token validation and refresh of LastAccess
                ReadIntern(token);
                ISet<ISharedDataHandOn> registeredHandOns = tokenToHandOnsDict[token];
                if (!registeredHandOns.Add(sharedDataHandOn))
                {
                    throw new Exception("Given handOn already registered. This is an invalid state");
                }
            }
            finally
            {
                writeLock.Unlock();
            }
        }

        public void UnregisterSharedDataHandOn(ISharedDataHandOn sharedDataHandOn, String token)
        {
            writeLock.Lock();
            try
            {
                // Force token validation and refresh of LastAccess
                ReadIntern(token);
                ISet<ISharedDataHandOn> registeredHandOns = tokenToHandOnsDict[token];
                if (!registeredHandOns.Remove(sharedDataHandOn))
                {
                    throw new Exception("Given handOn is not registered. This is an invalid state");
                }
            }
            finally
            {
                writeLock.Unlock();
            }
        }

        public String Put(IDictionary<String, IModelContainer> data)
        {
            String token = System.Guid.NewGuid().ToString();
            SharedDataItem sharedDataItem = new SharedDataItem(data);
            sharedDataItem.lastAccess = DateTime.Now;
            writeLock.Lock();
            try
            {
                tokenToDataDict.Add(token, sharedDataItem);
                tokenToHandOnsDict.Add(token, new IdentityHashSet<ISharedDataHandOn>());
            }
            finally
            {
                writeLock.Unlock();
            }
            return token;
        }

        public IDictionary<String, IModelContainer> Read(String token)
        {
            readLock.Lock();
            try
            {
                return ReadIntern(token);
            }
            finally
            {
                readLock.Unlock();
            }
        }

        protected IDictionary<String, IModelContainer> ReadIntern(String token)
        {
            SharedDataItem sharedDataItem = DictionaryExtension.ValueOrDefault(tokenToDataDict, token);
            if (sharedDataItem == null)
            {
                return null;
            }
            sharedDataItem.lastAccess = DateTime.Now;
            return sharedDataItem.data;
        }
    }
}
