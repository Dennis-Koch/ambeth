using System;

namespace De.Osthus.Ambeth.Collections
{
    public interface IListElem<V>
    {
        Object ListHandle { get; set; }

        IListElem<V> Prev { get; set; }

        IListElem<V> Next { get; set; }

        V ElemValue { get; set; }
    }
}
