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
    public class SetEntry<K> : ISetEntry<K>, IPrintable
    {
        protected readonly int hash;

        public ISetEntry<K> NextEntry { get; set; }

        protected readonly K key;

        public SetEntry(int hash, SetEntry<K> nextEntry, K key)
        {
            this.hash = hash;
            this.NextEntry = nextEntry;
            this.key = key;
        }

        public K Key
        {
            get
            {
                return key;
            }
        }

        public override bool Equals(Object obj)
        {
            if (obj == this)
            {
                return true;
            }
            if (!(obj is SetEntry<K>))
            {
                return false;
            }
            SetEntry<K> other = (SetEntry<K>)obj;
            return Object.Equals(Key, other.Key);
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

        public override String ToString()
        {
            StringBuilder sb = new StringBuilder();
            ToString(sb);
            return sb.ToString();
        }

        public void ToString(StringBuilder sb)
        {
            StringBuilderUtil.AppendPrintable(sb, Key);
        }
    }
}