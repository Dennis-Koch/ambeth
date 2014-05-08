using System;
using System.Net;

namespace De.Osthus.Ambeth.Service
{
    public interface IServiceUrlProvider 
    {
        String GetServiceURL(Type serviceInterface, String serviceName);

        bool IsOffline { get; set; }

        void LockForRestart(bool offlineAfterRestart);
    }
}
