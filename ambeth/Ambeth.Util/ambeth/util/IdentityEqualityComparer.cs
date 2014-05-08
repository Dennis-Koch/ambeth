using System;
using System.Collections.Generic;
using System.Collections;
using System.Runtime.CompilerServices;

namespace De.Osthus.Ambeth.Util
{
    public class IdentityEqualityComparer<T> : IdentityEqualityComparer, IEqualityComparer<T>
    {
        public bool Equals(T xKey, T yKey)
        {
            return Object.ReferenceEquals(xKey, yKey);
        }

        public int GetHashCode(T key)
        {
            return RuntimeHelpers.GetHashCode(key);
        }
    }

    public class IdentityEqualityComparer : IEqualityComparer
    {
        public new bool Equals(Object xKey, Object yKey)
        {
            return Object.ReferenceEquals(xKey, yKey);
        }

        public int GetHashCode(Object key)
        {
            return RuntimeHelpers.GetHashCode(key);
        }
    }
}
