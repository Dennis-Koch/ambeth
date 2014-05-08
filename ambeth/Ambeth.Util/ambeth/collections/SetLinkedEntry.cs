using De.Osthus.Ambeth.Util;
using System;
using System.Text;

namespace De.Osthus.Ambeth.Collections
{
    public class SetLinkedEntry<K> : IListElem<SetLinkedEntry<K>>, IPrintable, ISetEntry<K>
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

        public IListElem<SetLinkedEntry<K>> Prev { get; set; }

        public IListElem<SetLinkedEntry<K>> Next { get; set; }

        public SetLinkedEntry<K> ElemValue { get { return this; } set { throw new NotSupportedException(); } }

        protected readonly int hash;

        public ISetEntry<K> NextEntry { get; set; }

        protected readonly K key;

        public SetLinkedEntry()
        {
            // For GenericFastList
            hash = 0;
            key = default(K);
        }

        public SetLinkedEntry(int hash, K key)
        {
            this.hash = hash;
            this.key = key;
        }

        public K Key
        {
            get
            {
                return key;
            }
        }

        public int Hash
        {
            get
            {
                return hash;
            }
        }

        public override bool Equals(Object obj)
        {
            if (obj == this)
            {
                return true;
            }
            if (!(obj is SetLinkedEntry<K>))
            {
                return false;
            }
            SetLinkedEntry<K> other = (SetLinkedEntry<K>)obj;
            return Object.Equals(Key, other.Key) && Object.Equals(ElemValue, other.ElemValue);
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
            StringBuilderUtil.AppendPrintable(sb, Key);
        }
    }
}