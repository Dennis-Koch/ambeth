using System;
using System.Collections;
using System.Collections.Generic;

namespace De.Osthus.Ambeth.Collections
{
    public interface IICollection<V> : Iterable<V>, IICollection, ICollection<V>
    {
        bool AddAll<S>(S[] array) where S : V;

        bool AddAll<S>(IEnumerable<S> coll) where S : V;

        bool AddAll(IEnumerable coll);

        V[] ToArray();
    }

    public interface IICollection : ICollection, Iterable
    {
        bool Contains(Object item);
    }
}