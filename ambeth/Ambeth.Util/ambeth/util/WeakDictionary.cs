using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Collections;

namespace De.Osthus.Ambeth.Util
{
    public class WeakDictionary<K, V> : IDictionary<K, V>
    {
        //MAP FROM HASH CODE TO LIST OF KEY/VALUE PAIRS
        private Dictionary<int, List<Pair>> dic = new Dictionary<int, List<Pair>>();

        protected int count;

        protected IEqualityComparer<K> equalityComparer;

        public WeakDictionary()
            : this(null)
        {
            // Intended blank
        }

        public WeakDictionary(IEqualityComparer<K> equalityComparer)
        {
            this.equalityComparer = equalityComparer;
        }

        public override String ToString()
        {
            return "Count = " + Count;
        }

        public void Add(K key, V value)
        {
            int hashKey = key.GetHashCode();
            List<Pair> list = DictionaryExtension.ValueOrDefault(dic, hashKey);
            if (list == null)
            {
                list = new List<Pair>(2);
                dic.Add(hashKey, list);
            }
            for (int a = list.Count; a-- > 0; )
            {
                Pair p = list[a];
                Object target = p.Key.Target;
                if (target == null)
                {
                    removeItemFromList(hashKey, list, p, a);
                    continue;
                }
                if (AreKeysEqual((K)target, key))
                {
                    p.Value = value;
                    return;
                }
            }
            Pair newP = new Pair();
            newP.Key = new WeakReference(key);
            newP.Value = value;
            list.Add(newP);
            count++;
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
            List<Pair> list = DictionaryExtension.ValueOrDefault(dic, hashKey);
            if (list == null)
            {
                return false;
            }
            for (int a = list.Count; a-- > 0; )
            {
                Pair p = list[a];
                Object target = p.Key.Target;
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

        public WeakReference GetWeakReferenceEntry(K key)
        {
            int hashKey = key.GetHashCode();
            List<Pair> list = DictionaryExtension.ValueOrDefault(dic, hashKey);
            if (list == null)
            {
                return null;
            }
            for (int a = list.Count; a-- > 0; )
            {
                Pair p = list[a];
                Object target = p.Key.Target;
                if (target == null)
                {
                    continue;
                }
                if (AreKeysEqual((K)target, key))
                {
                    return p.Key;
                }
            }
            return null;
        }

        protected void removeItemFromList(int hashKey, List<Pair> list, Pair pair, int index)
        {
            pair.Value = null;
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
            List<Pair> list = DictionaryExtension.ValueOrDefault(dic, hashKey);
            if (list == null)
            {
                return false;
            }
            for (int a = list.Count; a-- > 0; )
            {
                Pair p = list[a];
                Object target = p.Key.Target;
                if (target == null)
                {
                    removeItemFromList(hashKey, list, p, a);
                    continue;
                }
                if (AreKeysEqual((K)target, key))
                {
                    removeItemFromList(hashKey, list, p, a);
                    return true;
                }
            }
            return false;
        }

        public bool TryGetValue(K key, out V value)
        {
            value = this[key];
            return value != null || ContainsKey(key);
        }

        public V this[K key]
        {
            get
            {
                int hashKey = key.GetHashCode();
                List<Pair> list = DictionaryExtension.ValueOrDefault(dic, hashKey);
                if (list == null)
                {
                    return default(V);
                }
                for (int a = list.Count; a-- > 0; )
                {
                    Pair p = list[a];
                    Object target = p.Key.Target;
                    if (target == null)
                    {
                        continue;
                    }
                    if (AreKeysEqual((K)target, key))
                    {
                        return (V)p.Value;
                    }
                }
                return default(V);
            }
            set
            {
                Add(key, value);
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

        public ICollection<K> Keys
        {
            get
            {
                List<K> keys = new List<K>(count);
                DictionaryExtension.Loop(dic, delegate(int key, List<Pair> pairs)
                {
                    foreach (Pair pair in pairs)
                    {
                        K target = (K)pair.Key.Target;
                        if (target == null)
                        {
                            continue;
                        }
                        keys.Add(target);
                    }
                });
                return keys;
            }
        }

        public ICollection<V> Values
        {
            get
            {
                List<V> values = new List<V>(count);
                DictionaryExtension.Loop(dic, delegate(int key, List<Pair> pairs)
                {
                    foreach (Pair pair in pairs)
                    {
                        K target = (K)pair.Key.Target;
                        if (target == null)
                        {
                            continue;
                        }
                        values.Add((V)pair.Value);
                    }
                });
                return values;
            }
        }

        public ICollection<KeyValuePair<K, V>> KeyValuePairs
        {
            get
            {
                ICollection<KeyValuePair<K, V>> kvPairs = new List<KeyValuePair<K, V>>(count);
                DictionaryExtension.Loop(dic, delegate(int key, List<Pair> pairs)
                {
                    foreach (Pair pair in pairs)
                    {
                        K target = (K)pair.Key.Target;
                        if (target == null)
                        {
                            continue;
                        }
                        V value = (V)pair.Value;
                        kvPairs.Add(new KeyValuePair<K, V>(target, value));
                    }
                });
                return kvPairs;
            }
        }

        public IEnumerator<KeyValuePair<K, V>> GetEnumerator()
        {
            return KeyValuePairs.GetEnumerator();
        }

        IEnumerator IEnumerable.GetEnumerator()
        {
            return KeyValuePairs.GetEnumerator();
        }

        public void Add(KeyValuePair<K, V> item)
        {
            Add(item.Key, item.Value);
        }

        public bool Contains(KeyValuePair<K, V> item)
        {
            V value = DictionaryExtension.ValueOrDefault(this, item.Key);
            return Object.Equals(value, item.Value);
        }

        public void CopyTo(KeyValuePair<K, V>[] array, int arrayIndex)
        {
            foreach (KeyValuePair<K, V> entry in this)
            {
                array[arrayIndex++] = entry;
            }
        }

        public bool Remove(KeyValuePair<K, V> item)
        {
            V value = DictionaryExtension.ValueOrDefault(this, item.Key);
            if (Object.Equals(value, item.Value))
            {
                Remove(item.Key);
                return true;
            }
            return false;
        }
    }

    public class Pair
    {
        public WeakReference Key;
        public Object Value;
    }
}