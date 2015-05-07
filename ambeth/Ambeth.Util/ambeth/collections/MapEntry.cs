using De.Osthus.Ambeth.Util;
using System;
using System.Text;

namespace De.Osthus.Ambeth.Collections
{
    /**
     * Wird von verschiedenen Map-Implementierungen als Entry f�r die Key-Value Mappings ben�tigt
     * 
     * @author kochd
     * 
     * @param <K>
     *            Der Typ des Keys
     * @param <V>
     *            Der Typ des Values
     */
    public class MapEntry<K, V> : IMapEntry<K, V>, IPrintable
    {
        protected readonly int hash;

        public IMapEntry<K, V> NextEntry
        {
            get
            {
                return NextEntryReal;
            }
        }

        public MapEntry<K, V> NextEntryReal { get; set; }

        protected readonly K key;

        protected V value;

        public MapEntry(int hash, MapEntry<K, V> nextEntry, K key, V value)
        {
            this.hash = hash;
            this.NextEntryReal = nextEntry;
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