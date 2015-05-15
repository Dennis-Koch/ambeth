using De.Osthus.Ambeth.Util;
using System;
using System.Text;

namespace De.Osthus.Ambeth.Collections
{
    public class MapLinkedEntry<K, V> : IListElem<MapLinkedEntry<K, V>>, IMapEntry<K, V>, IPrintable
    {
        protected Object listHandle;

        public Object ListHandle
        {
            get
            {
                return listHandle;
            }
            set
            {
                if (this.listHandle != null && value != null)
                {
                    throw new NotSupportedException();
                }
                this.listHandle = value;
            }
        }

        public IListElem<MapLinkedEntry<K, V>> Prev { get; set; }

        public IListElem<MapLinkedEntry<K, V>> Next { get; set; }

        public MapLinkedEntry<K, V> ElemValue { get { return this; } set { throw new NotSupportedException(); } }

        protected readonly int hash;

        public IMapEntry<K, V> NextEntry { get; set; }

        protected readonly K key;

        protected V value;

        public MapLinkedEntry()
        {
            // For GenericFastList
            hash = 0;
            key = default(K);
        }

        public MapLinkedEntry(int hash, K key, V value)
        {
            this.hash = hash;
            this.key = key;
            this.value = value;
        }

        public K Key
        {
            get
            {
                return key;
            }
        }

        public V Value
        {
            get
            {
                return value;
            }
			set
			{
				this.value = value;
			}
        }

        public int Hash
        {
            get
            {
                return hash;
            }
        }

        public V SetValue(V value)
        {
            V oldValue = this.value;
            this.value = value;
            return oldValue;
        }

        public override bool Equals(Object obj)
        {
            if (obj == this)
            {
                return true;
            }
            if (!(obj is IMapEntry<K, V>))
            {
                return false;
            }
            IMapEntry<K, V> other = (IMapEntry<K, V>)obj;
            return Object.Equals(Key, other.Key) && Object.Equals(Value, other.Value);
        }

        public override int GetHashCode()
        {
            // Key is enough for hashcoding
            K key = Key;
            if (key == null)
            {
                // Any prime number
                return 97;
            }
            return key.GetHashCode();
        }

        public override String ToString()
        {
            StringBuilder sb = new StringBuilder();
            ToString(sb);
            return sb.ToString();
        }

        public void ToString(StringBuilder sb)
        {
            sb.Append('(');
            StringBuilderUtil.AppendPrintable(sb, Key);
            sb.Append(',');
            StringBuilderUtil.AppendPrintable(sb, Value);
            sb.Append(')');
        }
    }
}