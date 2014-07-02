using System;
using System.Collections.Generic;

namespace De.Osthus.Ambeth.Collections
{
    public static class EmptyIterator<V>
    {
        public static Iterator<T> Empty<T>()
        {
            // Note that the static type is only instantiated when
            // it is needed, and only then is the T[0] object created, once.
            return EmptyIteratorIntern<T>.Instance;
        }

        private sealed class EmptyIteratorIntern<T> : Iterator<T>
        {
            public static readonly EmptyIteratorIntern<T> Instance = new EmptyIteratorIntern<T>();

            public T Current
            {
                get
                {
                    return default(T);
                }
            }

            public void Dispose()
            {
                // intended blank
            }

            Object System.Collections.IEnumerator.Current
            {
                get
                {
                    return null;
                }
            }

            public bool MoveNext()
            {
                return false;
            }

            public void Reset()
            {
                // intended blank
            }

            public void Remove()
            {
                throw new NotSupportedException("Iterator is read-only");
            }
        }
    }
}