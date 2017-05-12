using System;
using System.Collections;
using System.Collections.Generic;

namespace De.Osthus.Ambeth.Collections
{
    public interface Iterator<V> : IEnumerator<V>, Iterator
    {
        // Intended blank
    }

    public interface Iterator : IEnumerator
    {
        void Remove();
    }
}
