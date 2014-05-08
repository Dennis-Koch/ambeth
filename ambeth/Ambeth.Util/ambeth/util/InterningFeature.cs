using System;
using De.Osthus.Ambeth.Collections;

namespace De.Osthus.Ambeth.Util
{
    public class InterningFeature : IInterningFeature
    {
        protected readonly Object writeLock = new Object();

        protected readonly WeakHashSet<Object> weakSet = new WeakHashSet<Object>();

        public T Intern<T>(T value)
        {
            if (value == null)
            {
                return default(T);
            }
            lock (writeLock)
            {
                Object internedValue = weakSet[value];
                if (internedValue == null)
                {
                    internedValue = value;
                    weakSet.Add(internedValue);
                }
                return (T)internedValue;
            }
        }
    }
}
