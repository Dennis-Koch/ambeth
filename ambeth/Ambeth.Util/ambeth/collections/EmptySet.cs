using De.Osthus.Ambeth.Util;
using System;
using System.Collections;
using System.Collections.Generic;

namespace De.Osthus.Ambeth.Collections
{
    public static class EmptySet
    {
        public static IISet<T> Empty<T>()
        {
            // Note that the static type is only instantiated when
            // it is needed, and only then is the T[0] object created, once.
            return EmptySetIntern<T>.Instance;
        }

		private sealed class EmptySetIntern<T> : IISet<T>, IImmutableType
        {
            public static readonly EmptySetIntern<T> Instance = new EmptySetIntern<T>();
            
            public T Get(T key)
            {
                return default(T);
            }

            public IList<T> ToList()
            {
                return EmptyList.Empty<T>();
            }

            public T RemoveAndGet(T key)
            {
                throw new NotSupportedException("Set is read-only");
            }

            public bool Add(T item)
            {
                throw new NotSupportedException("Set is read-only");
            }

            public void ExceptWith(IEnumerable<T> other)
            {
                throw new NotSupportedException("Set is read-only");
            }

            public void IntersectWith(IEnumerable<T> other)
            {
                throw new NotSupportedException("Set is read-only");
            }

            public bool IsProperSubsetOf(IEnumerable<T> other)
            {
                return false;
            }

            public bool IsProperSupersetOf(IEnumerable<T> other)
            {
                return false;
            }

            public bool IsSubsetOf(IEnumerable<T> other)
            {
                return false;
            }

            public bool IsSupersetOf(IEnumerable<T> other)
            {
                return false;
            }

            public bool Overlaps(IEnumerable<T> other)
            {
                return false;
            }

            public bool SetEquals(IEnumerable<T> other)
            {
                return !other.GetEnumerator().MoveNext();
            }

            public void SymmetricExceptWith(IEnumerable<T> other)
            {
                throw new NotSupportedException("Set is read-only");
            }

            public void UnionWith(IEnumerable<T> other)
            {
                throw new NotSupportedException("Set is read-only");
            }

            void ICollection<T>.Add(T item)
            {
                throw new NotSupportedException("Set is read-only");
            }

            public void Clear()
            {
                throw new NotSupportedException("Set is read-only");
            }

            public bool Contains(T item)
            {
                return false;
            }

            public void CopyTo(T[] array, int arrayIndex)
            {
                // intended blank
            }

            public int Count
            {
                get
                {
                    return 0;
                }
            }

            public bool IsReadOnly
            {
                get
                {
                    return true;
                }
            }

            public bool Remove(T item)
            {
                throw new NotSupportedException("Set is read-only");
            }

            public IEnumerator<T> GetEnumerator()
            {
                return EmptyIterator<T>.Empty<T>();
            }

            System.Collections.IEnumerator System.Collections.IEnumerable.GetEnumerator()
            {
                return EmptyIterator<T>.Empty<T>();
            }

            public bool AddAll<S>(S[] array) where S : T
            {
                throw new NotSupportedException("Set is read-only");
            }

            public bool AddAll<S>(IEnumerable<S> coll) where S : T
            {
                throw new NotSupportedException("Set is read-only");
            }

            public bool AddAll(IEnumerable coll)
            {
                throw new NotSupportedException("Set is read-only");
            }

            public bool ContainsAny(IEnumerable c)
            {
				return false;
            }

			public bool ContainsAny<S>(S[] array) where S : T
			{
				return false;
			}

            public bool RemoveAll(IEnumerable c)
            {
                throw new NotSupportedException("Set is read-only");
            }

            public T[] ToArray()
            {
                return EmptyList.EmptyArray<T>();
            }

            public Iterator<T> Iterator()
            {
                return EmptyIterator<T>.Empty<T>();
            }

            public Iterator<T> Iterator(bool removeAllowed)
            {
                return EmptyIterator<T>.Empty<T>();
            }

            Iterator Iterable.Iterator()
            {
                return EmptyIterator<T>.Empty<T>();
            }

            Iterator Iterable.Iterator(bool removeAllowed)
            {
                return EmptyIterator<T>.Empty<T>();
            }

            public bool Contains(Object item)
            {
                return false;
            }

            public void CopyTo(Array array, int index)
            {
                // intended blank
            }

            public bool IsSynchronized
            {
                get
                {
                    return false;
                }
            }

            public Object SyncRoot
            {
                get
                {
                    return null;
                }
            }
        }
    }
}