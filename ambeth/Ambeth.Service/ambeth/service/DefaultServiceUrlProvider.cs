using De.Osthus.Ambeth.Config;
using De.Osthus.Ambeth.Ioc;
using System;

namespace De.Osthus.Ambeth.Service
{
    public class DefaultServiceUrlProvider : IServiceUrlProvider, IInitializingBean, IOfflineListenerExtendable
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
        public virtual String OnlineServiceProtocol { get; set; }

        [Property(ServiceConfigurationConstants.ServiceHostName, DefaultValue = "localhost")]
        public virtual String OnlineServiceHostName { get; set; }

        [Property(ServiceConfigurationConstants.ServiceHostPort, DefaultValue = "8000")]
        public virtual uint OnlineServiceHostPort { get; set; }

        public virtual void AfterPropertiesSet()
        {
            // Intended blank
        }

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
