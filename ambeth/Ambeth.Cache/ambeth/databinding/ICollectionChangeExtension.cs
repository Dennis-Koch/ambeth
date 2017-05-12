using System;
using System.Collections.Specialized;

namespace De.Osthus.Ambeth.Databinding
{
    public interface ICollectionChangeExtension
    {
        void CollectionChanged(Object obj, NotifyCollectionChangedEventArgs evnt);
    }
}