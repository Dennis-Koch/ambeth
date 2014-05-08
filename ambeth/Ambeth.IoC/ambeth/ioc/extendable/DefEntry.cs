using De.Osthus.Ambeth.Collections;
using System;

namespace De.Osthus.Ambeth.Ioc.Extendable
{
    public class DefEntry<V> : IListElem<DefEntry<V>>, IComparable<DefEntry<V>>
    {
        public IListElem<DefEntry<V>> Prev { get; set; }

        public IListElem<DefEntry<V>> Next { get; set; }

        public Object ListHandle { get; set; }

        public readonly V extension;

        public readonly Type type;

        public readonly int distance;

        public DefEntry(V extension, Type type, int distance)
        {
            this.extension = extension;
            this.type = type;
            this.distance = distance;
        }

        public DefEntry<V> ElemValue
        {
            get
            {
                return this;
            }
            set
            {
                throw new NotSupportedException();
            }
        }

        public int CompareTo(DefEntry<V> o)
        {
            if (o.distance > distance)
            {
                return 1;
            }
            if (o.distance == distance)
            {
                return 0;
            }
            return -1;
        }
    }
}