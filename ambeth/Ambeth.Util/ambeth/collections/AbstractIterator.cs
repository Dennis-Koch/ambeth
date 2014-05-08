using System;
using System.Collections.Generic;

namespace De.Osthus.Ambeth.Collections
{
    public abstract class AbstractIterator<V> : Iterator<V>
    {
        protected readonly bool removeAllowed;

        public AbstractIterator()
            : this(false)
        {
            // Intended blank
        }

        public AbstractIterator(bool removeAllowed)
        {
            this.removeAllowed = removeAllowed;
        }

        public abstract void Remove();

        public abstract V Current { get; }

        public abstract void Dispose();

        public abstract bool MoveNext();

        public void Reset()
        {
            throw new System.NotImplementedException();
        }
        
        Object System.Collections.IEnumerator.Current
        {
            get { return Current; }
        }
    }
}