using System;
using System.Collections.Generic;

namespace De.Osthus.Ambeth.Collections
{
    public static class EmptyList
    {
        public static IList<T> Empty<T>()
        {
            // Note that the static type is only instantiated when
            // it is needed, and only then is the T[0] object created, once.
            return EmptyArrayIntern<T>.Instance;
        }

        public static T[] EmptyArray<T>()
        {
            // Note that the static type is only instantiated when
            // it is needed, and only then is the T[0] object created, once.
            return EmptyArrayIntern<T>.Instance;
        }

        private sealed class EmptyArrayIntern<T>
        {
            public static readonly T[] Instance = new T[0];
        }
    }
}