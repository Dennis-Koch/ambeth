using De.Osthus.Ambeth.Config;
using De.Osthus.Ambeth.Ioc;
using System;

namespace De.Osthus.Ambeth.Service
{
    public class DefaultServiceUrlProvider : IServiceUrlProvider, IOfflineListenerExtendable
    {
        public bool IsOffline
        {
            get
            {
                return false;
            }
            set
            {
                throw new NotSupportedException("This " + typeof(IServiceUrlProvider).Name + " does not support this operation");
            }
        }

        [Property(ServiceConfigurationConstants.ServiceProtocol, DefaultValue = "http")]
        public String OnlineServiceProtocol { protected get; set; }

        [Property(ServiceConfigurationConstants.ServiceHostName, DefaultValue = "localhost")]
        public String OnlineServiceHostName { protected get; set; }

        [Property(ServiceConfigurationConstants.ServiceHostPort, DefaultValue = "8000")]
        public uint OnlineServiceHostPort { protected get; set; }
        
        public void LockForRestart(bool offlineAfterRestart)
        {
            throw new NotSupportedException("This " + typeof(IServiceUrlProvider).Name + " does not support this operation");
        }

        public String GetServiceURL(Type serviceInterface, String serviceName)
        {
            return OnlineServiceProtocol + "://" + OnlineServiceHostName + ":" + OnlineServiceHostPort + "/" + serviceName + "/";
        }

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
