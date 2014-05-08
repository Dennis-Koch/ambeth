using De.Osthus.Ambeth.Collections;
using De.Osthus.Ambeth.Ioc.Extendable;
using De.Osthus.Ambeth.Util;
using System;
using System.Collections.Generic;

namespace De.Osthus.Ambeth.Ioc{

    public class DefaultExtendableContainer<V> : IExtendableContainer<V>
    {
        protected String message;

        protected IdentityHashSet<V> set;

        protected Type type;
        
        protected readonly Lock readLock, writeLock;

        public DefaultExtendableContainer(String message)
        {
            this.type = typeof(V);
            this.message = message;
            set = new IdentityHashSet<V>();
            ReadWriteLock rwLock = new ReadWriteLock();
            readLock = rwLock.ReadLock;
            writeLock = rwLock.WriteLock;
        }

        public void Register(V listener)
        {
            ParamChecker.AssertParamNotNull(listener, message);
            Lock writeLock = this.writeLock;
            writeLock.Lock();
            try
            {
                ParamChecker.AssertTrue(set.Add(listener), message);
            }
            finally
            {
                writeLock.Unlock();
            }
        }


        public void Unregister(V listener)
        {
            ParamChecker.AssertParamNotNull(listener, message);
            Lock writeLock = this.writeLock;
            writeLock.Lock();
            try
            {
                ParamChecker.AssertTrue(set.Remove(listener), message);
            }
            finally
            {
                writeLock.Unlock();
            }
        }


        public V[] GetExtensions()
        {
            Lock readLock = this.readLock;
            readLock.Lock();
            try
            {
                int size = set.Count;
                if (size == 0)
                {
                    return EmptyList.EmptyArray<V>();
                }
                V[] array = (V[])Array.CreateInstance(type, size);
                set.CopyTo(array, 0);
                return array;
            }
            finally
            {
                readLock.Unlock();
            }
        }

        public void GetExtensions(ICollection<V> targetExtensionList)
        {
            Lock readLock = this.readLock;
            readLock.Lock();
            try
            {
                foreach (V extension in set)
                {
                    targetExtensionList.Add(extension);
                }
            }
            finally
            {
                readLock.Unlock();
            }
        }
    }
}
