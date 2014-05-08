using System;

namespace De.Osthus.Ambeth.Collections
{
    public class ListElem<V> : IListElem<V>
    {
        public Object ListHandle { get; set; }

        public IListElem<V> Prev { get; set; }

        public IListElem<V> Next { get; set; }

        public V ElemValue { get; set; }

        public ListElem(V value)
        {
            this.ElemValue = value;
        }

        public void Init(V value)
        {
            this.ElemValue = value;
        }
    }
}