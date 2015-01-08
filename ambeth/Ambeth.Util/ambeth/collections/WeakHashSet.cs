using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Collections;
using De.Osthus.Ambeth.Util;

namespace De.Osthus.Ambeth.Collections
{
    public class WeakHashSet<K>
    {
        //MAP FROM HASH CODE TO LIST OF KEY/VALUE PAIRS
        private Dictionary<int, List<WeakReference>> dic = new Dictionary<int, List<WeakReference>>();

        protected int count;

        protected IEqualityComparer<K> equalityComparer;

        public WeakHashSet()
            : this(null)
        {
            // Intended blank
        }

        public WeakHashSet(IEqualityComparer<K> equalityComparer)
        {
            this.equalityComparer = equalityComparer;
        }

        public override String ToString()
        {
            return "Count = " + Count;
        }
        
        public bool Add(K key)
        {
            int hashKey = key.GetHashCode();
            List<WeakReference> list = DictionaryExtension.ValueOrDefault(dic, hashKey);
            if (list == null)
            {
                list = new List<WeakReference>(2);
                dic.Add(hashKey, list);
            }
            for (int a = list.Count; a-- > 0;)
            {
                WeakReference p = list[a];
                Object target = p.Target;
                if (target == null)
                {
                    RemoveItemFromList(hashKey, list, a);
                    continue;
                }
                if (AreKeysEqual((K)target, key))
                {
                    return false;
                }
            }
            WeakReference newP = new WeakReference(key);
            list.Add(newP);
            count++;
            return true;
        }

        protected bool AreKeysEqual(K leftKey, K rightKey)
        {
            if (equalityComparer == null)
            {
                return Object.ReferenceEquals(leftKey, rightKey);
            }
            return equalityComparer.Equals(leftKey, rightKey);
        }

        public bool ContainsKey(K key)
        {
            int hashKey = key.GetHashCode();
            List<WeakReference> list = DictionaryExtension.ValueOrDefault(dic, hashKey);
            if (list == null)
            {
                return false;
            }
            for (int a = list.Count; a-- > 0;)
            {
                WeakReference p = list[a];
                Object target = p.Target;
                if (target == null)
                {
                    continue;
                }
                if (AreKeysEqual((K)target, key))
                {
                    return true;
                }
            }            
            return false;
        }

        protected void RemoveItemFromList(int hashKey, List<WeakReference> list, int index)
        {
            list.RemoveAt(index);
            if (list.Count == 0)
            {
                dic.Remove(hashKey);
            }
            count--;
        }

        public bool Remove(K key)
        {
            int hashKey = key.GetHashCode();
            List<WeakReference> list = DictionaryExtension.ValueOrDefault(dic, hashKey);
            if (list == null)
            {
                return false;
            }
            for (int a = list.Count; a-- > 0;)
            {
                WeakReference p = list[a];
                Object target = p.Target;
                if (target == null)
                {
                    RemoveItemFromList(hashKey, list, a);
                    continue;
                }
                if (AreKeysEqual((K)target, key))
                {
                    RemoveItemFromList(hashKey, list, a);
                    return true;
                }
            }
            return false;
        }

        public bool TryGetValue(K key, out K value)
        {
            value = this[key];
            return value != null || ContainsKey(key);
        }

        public K this[K key]
        {
            get
            {
                int hashKey = key.GetHashCode();
                List<WeakReference> list = DictionaryExtension.ValueOrDefault(dic, hashKey);
                if (list == null)
                {
                    return default(K);
                }
                for (int a = list.Count; a-- > 0; )
                {
                    WeakReference p = list[a];
                    Object target = p.Target;
                    if (target == null)
                    {
                        continue;
                    }
                    if (AreKeysEqual((K)target, key))
                    {
                        return (K)target;
                    }
                }
                return default(K);
            }
            set
            {
                Add(key);
            }
        }
        
        public void Clear()
        {
            dic.Clear();
            count = 0;
        }

        public int Count
        {
            get
            {
                return count;
            }
        }

        public bool IsReadOnly
        {
            get { return false; }
        }
    }
}
