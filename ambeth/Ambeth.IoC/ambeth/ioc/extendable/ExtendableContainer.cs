using System;
using System.Collections.Generic;
using De.Osthus.Ambeth.Util;

namespace De.Osthus.Ambeth.Ioc.Extendable
{
    public class ExtendableContainer<V> : IExtendableContainer<V>
    {
        protected String message;

        protected ISet<V> set;

        protected V[] emptyArray;

        public ExtendableContainer(String message)
        {
            ParamChecker.AssertParamNotNull(message, "message");
            this.message = message;
            set = new HashSet<V>(new IdentityEqualityComparer<V>());
            emptyArray = (V[])Array.CreateInstance(typeof(V), 0);
        }

        public void Register(V listener)
        {
            ParamChecker.AssertParamNotNull(listener, message);
            ParamChecker.AssertTrue(set.Add(listener), message);
        }

        public void Unregister(V listener)
        {
            ParamChecker.AssertParamNotNull(listener, message);
            ParamChecker.AssertTrue(set.Remove(listener), message);
        }
        
        public V[] GetExtensions()
        {
            int size = set.Count;
            if (size == 0)
            {
                return emptyArray;
            }
            V[] array = (V[])Array.CreateInstance(typeof(V), size);
            set.CopyTo(array, 0);
            return array;
        }
        
        public void GetExtensions(ICollection<V> targetListenerList)
        {
            foreach (V listener in set)
            {
                targetListenerList.Add(listener);
            }
        }
    }
}
