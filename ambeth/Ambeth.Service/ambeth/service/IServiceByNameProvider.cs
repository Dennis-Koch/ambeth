using System.IO;
using System.ServiceModel;
using System;

namespace De.Osthus.Ambeth.Service
{
    public interface IServiceByNameProvider
    {
        Object GetService(String serviceName);
    }
}
