using System;
using System.Collections.Generic;

namespace De.Osthus.Ambeth.Collections
{
    public interface Entry<K, V>
    {
        K Key { get; }

		V Value { get; set; }
    }
}