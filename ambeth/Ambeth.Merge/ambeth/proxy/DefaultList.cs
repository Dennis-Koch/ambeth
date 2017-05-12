using System.Collections.Generic;
using System.Collections;

namespace De.Osthus.Ambeth.Proxy
{
    public class DefaultList<T> : IList<T>, IDefaultCollection
    {
        protected readonly List<T> list = new List<T>();

        protected bool hasDefaultState = true;

	    public bool HasDefaultState
	    {
            get
            {
                return hasDefaultState;
            }
	    }

        public int IndexOf(T item)
        {
            return list.IndexOf(item);
        }

        public void Insert(int index, T item)
        {
            hasDefaultState = false;
            list.Insert(index, item);
        }

        public void RemoveAt(int index)
        {
            list.RemoveAt(index);
        }

        public T this[int index]
        {
            get
            {
                return list[index];
            }
            set
            {
                hasDefaultState = false;
                list[index] = value;
            }
        }

        public void Add(T item)
        {
            hasDefaultState = false;
            list.Add(item);
        }

        public void Clear()
        {
            list.Clear();
        }

        public bool Contains(T item)
        {
            return list.Contains(item);
        }

        public void CopyTo(T[] array, int arrayIndex)
        {
            list.CopyTo(array, arrayIndex);
        }

        public int Count
        {
            get
            {
                return list.Count;
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
            return list.Remove(item);
        }

        public IEnumerator<T> GetEnumerator()
        {
            return list.GetEnumerator();
        }

        IEnumerator IEnumerable.GetEnumerator()
        {
            return list.GetEnumerator();
        }
    }
}