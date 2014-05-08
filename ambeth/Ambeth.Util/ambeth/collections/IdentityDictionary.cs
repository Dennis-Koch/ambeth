using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using De.Osthus.Ambeth.Util;

namespace De.Osthus.Ambeth.Collections
{
    public class IdentityDictionary<K, V> : Dictionary<K, V>
    {
        public static IEqualityComparer<K> comparer = new IdentityEqualityComparer<K>();

        public IdentityDictionary()
            : base(comparer)
        {
            // Intended blank
        }

        public IdentityDictionary(IDictionary<K, V> dictionary)
            : base(dictionary, comparer)
        {
            // Intended blank
        }

        public IdentityDictionary(int capacity)
            : base(capacity, comparer)
        {
            // Intended blank
        }
    }
}
