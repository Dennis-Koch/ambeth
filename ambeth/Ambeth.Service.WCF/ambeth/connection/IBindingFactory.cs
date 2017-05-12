using System;
using System.ServiceModel.Channels;

namespace De.Osthus.Ambeth.Connection
{
    public interface IBindingFactory
    {
        Binding CreateBinding(Type serviceInterface, String serviceName, String serviceUrl);
    }
}
