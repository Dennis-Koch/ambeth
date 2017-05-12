using System;
using System.Net;

namespace De.Osthus.Ambeth.Service
{
    public interface IOfflineListener 
    {
        void BeginOnline();

        void HandleOnline();

        void EndOnline();

        void BeginOffline();

        void HandleOffline();

        void EndOffline();
    }
}
