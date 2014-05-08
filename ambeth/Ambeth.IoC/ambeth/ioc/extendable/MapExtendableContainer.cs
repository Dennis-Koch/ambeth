using System;
using De.Osthus.Ambeth.Log;
using System.Collections.Generic;
using De.Osthus.Ambeth.Util;
using De.Osthus.Ambeth.Ioc.Exceptions;
using De.Osthus.Ambeth.Collections;

namespace De.Osthus.Ambeth.Ioc.Extendable
{
    public class MapExtendableContainer<K, V> : SmartCopyMap<K, Object>, IMapExtendableContainer<K, V>
    {
    	protected readonly bool multiValue;

        protected String message, keyMessage;

        public MapExtendableContainer(String message, String keyMessage) : this(message, keyMessage, false)
        {
            // Intended blank
        }

        public MapExtendableContainer(String message, String keyMessage, bool multiValue)
        {
            ParamChecker.AssertParamNotNull(message, "message");
            ParamChecker.AssertParamNotNull(keyMessage, "keyMessage");
            this.multiValue = multiValue;
            this.message = message;
            this.keyMessage = keyMessage;
        }

        public virtual void Register(V extension, K key)
        {
            ParamChecker.AssertParamNotNull(extension, message);
            ParamChecker.AssertParamNotNull(key, keyMessage);

            Object writeLock = GetWriteLock();
            lock (writeLock)
            {
			    bool putted = false;
			    if (!multiValue)
			    {
				    putted = PutIfNotExists(key, extension);
			    }
			    else
			    {
				    List<V> values = (List<V>) Get(key);
				    if (values == null)
				    {
					    values = new List<V>(1);
					    Put(key, values);
				    }
				    if (!values.Contains(extension))
				    {
					    values.Add(extension);
					    putted = true;
				    }
			    }
			    if (!putted)
			    {
				    throw new ExtendableException("Key '" + keyMessage + "' already added: " + key);
			    }
            }
        }

        public virtual void Unregister(V extension, K key)
        {
            ParamChecker.AssertParamNotNull(extension, message);
            ParamChecker.AssertParamNotNull(key, keyMessage);

            Object writeLock = GetWriteLock();
            lock (writeLock)
            {
                if (!multiValue)
                {
                    ParamChecker.AssertTrue(RemoveIfValue(key, extension), message);
                }
                else
                {
                    List<V> values = (List<V>)Get(key);
                    ParamChecker.AssertNotNull(values, message);
                    ParamChecker.AssertTrue(values.Remove(extension), message);
                    if (values.Count == 0)
                    {
                        Remove(key);
                    }
                }
            }
        }

        public IList<V> GetExtensions(K key)
        {
            ParamChecker.AssertParamNotNull(key, "key");
            Object item = Get(key);
            if (item == null)
            {
                return EmptyList.Empty<V>();
            }
            List<V> list = new List<V>();
            if (!multiValue)
            {
                list.Add((V)item);
            }
            else
            {
                list.AddRange((List<V>)item);
            }
            return list;
        }

        public virtual V GetExtension(K key)
        {
            ParamChecker.AssertParamNotNull(key, "key");
            Object item = Get(key);
            if (item == null)
            {
                return default(V);
            }
            if (!multiValue)
            {
                return (V)item;
            }
            else
            {
                List<V> values = (List<V>)item;
                // unregister removes empty value lists -> at least one entry
                return values[0];
            }
        }

        public ILinkedMap<K, V> GetExtensions()
        {
            LinkedHashMap<K, V> targetMap = new LinkedHashMap<K, V>();
            GetExtensions(targetMap);
            return targetMap;
        }

        public void GetExtensions(IMap<K, V> targetListenerMap)
        {
            foreach (Entry<K, Object> entry in this)
            {
                Object item = entry.Value;
                V extension;
                if (item is V)
                {
                    extension = (V)item;
                }
                else
                {
                    extension = ((List<V>)item)[0];
                }
                targetListenerMap.Put(entry.Key, extension);
            };
        }
    }
}
