using System;
using System.Collections;
using System.Collections.Generic;

namespace De.Osthus.Ambeth.Collections
{
    public interface Iterable<V> : IEnumerable<V>, Iterable
    {
        new Iterator<V> Iterator();

        new Iterator<V> Iterator(bool removeAllowed);
    }

    public interface Iterable : IEnumerable
    {
        Iterator Iterator();

        Iterator Iterator(bool removeAllowed);
    }
}