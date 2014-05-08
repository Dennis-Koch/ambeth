using System;

namespace De.Osthus.Ambeth.Service
{
    public class NoOpOfflineExtendable : IOfflineListenerExtendable
    {
        public void AddOfflineListener(IOfflineListener offlineListener)
        {
            // Intended NoOp!
        }

        public void RemoveOfflineListener(IOfflineListener offlineListener)
        {
            // Intended NoOp!
        }
    }
}
