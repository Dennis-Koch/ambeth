using System;
using System.Net;

namespace De.Osthus.Ambeth.Service
{
    public interface IOfflineListenerExtendable 
    {
        void AddOfflineListener(IOfflineListener offlineListener);

        void RemoveOfflineListener(IOfflineListener offlineListener);
    }
}
