using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace De.Osthus.Ambeth.Service
{
    public interface IServiceExtendable
    {
        void registerService(Object service, String serviceName);

        void unregisterService(Object service, String serviceName);
    }
}
