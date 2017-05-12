using System;

namespace De.Osthus.Ambeth.Collections
{
    public class InterfaceListElem<V> : IListElem<V>
    {
        public Object ListHandle { get; set; }

        public IListElem<V> Prev { get; set; }

        public IListElem<V> Next { get; set; }

        public V ElemValue { get; set; }

        public InterfaceListElem()
	    {
	    }

	    public InterfaceListElem(V value)
	    {
            ElemValue = value;
	    }
    }
}