using System;
using De.Osthus.Ambeth.Model;
using System.ComponentModel;
using De.Osthus.Ambeth.Threading;

namespace De.Osthus.Ambeth.Cache
{
    public interface ICacheModification
    {
        bool InternalUpdate { get; set; }

        bool Active { get; set; }

        bool ActiveOrFlushing { get; }
        
        void QueuePropertyChangeEvent(IBackgroundWorkerDelegate task);
    }
}
