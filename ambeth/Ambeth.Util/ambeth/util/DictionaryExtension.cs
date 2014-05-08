using De.Osthus.Ambeth.Collections;
using System;
using System.Collections.Generic;

namespace De.Osthus.Ambeth.Util
{
    public sealed class DictionaryExtension
    {
        public delegate void KeyValueCallback<TKey, TValue>(TKey key, TValue value);

        public delegate bool BreakableKeyValueCallback<TKey, TValue>(TKey key, TValue value);

        public static TValue ValueOrDefault<TKey, TValue>(IDictionary<TKey, TValue> dictionary, TKey key)
        {
            if (dictionary == null)
            {
                return default(TValue);
            }
            TValue value = default(TValue);
            dictionary.TryGetValue(key, out value);
            return value;
        }

        public static TValue ValueOrAddNew<TKey, TValue>(IDictionary<TKey, TValue> dictionary, TKey key, Type valueType)
        {
            TValue value = ValueOrDefault(dictionary, key);
            if (value == null)
            {
                value = (TValue)Activator.CreateInstance(valueType);
                dictionary.Add(key, value);
            }
            return value;
        }

        public static void Loop<TKey, TValue>(IDictionary<TKey, TValue> dictionary, KeyValueCallback<TKey, TValue> keyValueCallback)
        {
            if (dictionary == null)
            {
                return;
            }
            using (IEnumerator<KeyValuePair<TKey, TValue>> dictIter = dictionary.GetEnumerator())
            {
                while (dictIter.MoveNext())
                {
                    KeyValuePair<TKey, TValue> entry = dictIter.Current;
                    keyValueCallback.Invoke(entry.Key, entry.Value);
                }
            }
        }

        public static void Loop<TKey, TValue>(IDictionary<TKey, TValue> dictionary, BreakableKeyValueCallback<TKey, TValue> keyValueCallback)
        {
            if (dictionary == null || dictionary.Count == 0)
            {
                return;
            }
            using (IEnumerator<KeyValuePair<TKey, TValue>> dictIter = dictionary.GetEnumerator())
            {
                while (dictIter.MoveNext())
                {
                    KeyValuePair<TKey, TValue> entry = dictIter.Current;
                    if (!keyValueCallback.Invoke(entry.Key, entry.Value))
                    {
                        return;
                    }
                }
            }
        }

        public static void LoopModifiable<TKey, TValue>(IDictionary<TKey, TValue> dictionary, KeyValueCallback<TKey, TValue> keyValueCallback)
        {
            if (dictionary == null || dictionary.Count == 0)
            {
                return;
            }
            Dictionary<TKey, TValue> cloneDict;
            if (dictionary is IdentityDictionary<TKey, TValue>)
            {
                cloneDict = new IdentityDictionary<TKey, TValue>(dictionary);
            }
            else
            {
                cloneDict = new Dictionary<TKey, TValue>(dictionary);
            }
            using (IEnumerator<KeyValuePair<TKey, TValue>> dictIter = cloneDict.GetEnumerator())
            {
                while (dictIter.MoveNext())
                {
                    KeyValuePair<TKey, TValue> entry = dictIter.Current;
                    keyValueCallback.Invoke(entry.Key, entry.Value);
                }
            }
        }

        public static void LoopModifiable<TKey, TValue>(IDictionary<TKey, TValue> dictionary, BreakableKeyValueCallback<TKey, TValue> keyValueCallback)
        {
            if (dictionary == null || dictionary.Count == 0)
            {
                return;
            }
            Dictionary<TKey, TValue> cloneDict;
            if (dictionary is IdentityDictionary<TKey, TValue>)
            {
                cloneDict = new IdentityDictionary<TKey, TValue>(dictionary);
            }
            else
            {
                cloneDict = new Dictionary<TKey, TValue>(dictionary);
            }
            using (IEnumerator<KeyValuePair<TKey, TValue>> dictIter = cloneDict.GetEnumerator())
            {
                while (dictIter.MoveNext())
                {
                    KeyValuePair<TKey, TValue> entry = dictIter.Current;
                    if (!keyValueCallback.Invoke(entry.Key, entry.Value))
                    {
                        return;
                    }
                }
            }
        }

        public static void Loop<TKey, TValue>(IEnumerator<KeyValuePair<TKey, TValue>> dictIter, KeyValueCallback<TKey, TValue> keyValueCallback)
        {
            try
            {
                while (dictIter.MoveNext())
                {
                    KeyValuePair<TKey, TValue> entry = dictIter.Current;
                    keyValueCallback.Invoke(entry.Key, entry.Value);
                }
            }
            finally
            {
                dictIter.Reset();
            }
        }

        public static void Loop<TKey, TValue>(IEnumerator<KeyValuePair<TKey, TValue>> dictIter, BreakableKeyValueCallback<TKey, TValue> keyValueCallback)
        {
            try
            {
                while (dictIter.MoveNext())
                {
                    KeyValuePair<TKey, TValue> entry = dictIter.Current;
                    if (!keyValueCallback.Invoke(entry.Key, entry.Value))
                    {
                        return;
                    }
                }
            }
            finally
            {
                dictIter.Reset();
            }
        }

        public static IList<TValue> GetList<TKey, TValue>(IDictionary<TKey, IList<TValue>> dictionary, TKey key)
        {
            IList<TValue> list = DictionaryExtension.ValueOrDefault(dictionary, key);
            if (list == null)
            {
                list = new List<TValue>();
                dictionary.Add(key, list);
            }
            return list;
        }

        private DictionaryExtension()
        {
            // intended blank
        }
    }
}
