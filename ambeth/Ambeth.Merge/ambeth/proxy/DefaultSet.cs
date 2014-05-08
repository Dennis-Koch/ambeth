using System.Collections;
using System.Collections.Generic;

namespace De.Osthus.Ambeth.Proxy
{
    public class DefaultSet<T> : ISet<T>, IDefaultCollection
    {
        protected readonly HashSet<T> set = new HashSet<T>();

        protected bool hasDefaultState = true;

        public bool HasDefaultState
        {
            get
            {
                return hasDefaultState;
            }
        }

        public bool Add(T item)
        {
            hasDefaultState = false;
            return set.Add(item);
        }

        public void ExceptWith(IEnumerable<T> other)
        {
            hasDefaultState = false;
            set.ExceptWith(other);
        }

        public void IntersectWith(IEnumerable<T> other)
        {
            hasDefaultState = false;
            set.ExceptWith(other);
        }

        public bool IsProperSubsetOf(IEnumerable<T> other)
        {
            return set.IsProperSubsetOf(other);
        }

        public bool IsProperSupersetOf(IEnumerable<T> other)
        {
            return set.IsProperSupersetOf(other);
        }

        public bool IsSubsetOf(IEnumerable<T> other)
        {
            return set.IsSubsetOf(other);
        }

        public bool IsSupersetOf(IEnumerable<T> other)
        {
            return set.IsSupersetOf(other);
        }

        public bool Overlaps(IEnumerable<T> other)
        {
            return set.Overlaps(other);
        }

        public bool SetEquals(IEnumerable<T> other)
        {
            return set.SetEquals(other);
        }

        public void SymmetricExceptWith(IEnumerable<T> other)
        {
            hasDefaultState = false;
            set.SymmetricExceptWith(other);
        }

        public void UnionWith(IEnumerable<T> other)
        {
            hasDefaultState = false;
            set.UnionWith(other);
        }

        public void Clear()
        {
            set.Clear();
        }

        public bool Contains(T item)
        {
            return set.Contains(item);
        }

        public void CopyTo(T[] array, int arrayIndex)
        {
            set.CopyTo(array, arrayIndex);
        }

        public int Count
        {
            get
            {
                return set.Count;
            }
        }

        public bool IsReadOnly
        {
            get
            {
                return false;
            }
        }

        public bool Remove(T item)
        {
            return set.Remove(item);
        }

        public IEnumerator<T> GetEnumerator()
        {
            return set.GetEnumerator();
        }

        void ICollection<T>.Add(T item)
        {
            hasDefaultState = false;
            set.Add(item);
        }

        IEnumerator IEnumerable.GetEnumerator()
        {
            return set.GetEnumerator();
        }
    }
}