using De.Osthus.Ambeth.Config;
using De.Osthus.Ambeth.Crypto;
using De.Osthus.Ambeth.Ioc.Annotation;
using De.Osthus.Ambeth.Ioc.Factory;
using De.Osthus.Ambeth.Remote;
using De.Osthus.Ambeth.Security;
using De.Osthus.Ambeth.Security.Config;
using De.Osthus.Ambeth.Service;

namespace De.Osthus.Ambeth.Ioc
{
    [FrameworkModule]
    public class SecurityModule : IInitializingModule
    {
        [Property(ServiceConfigurationConstants.NetworkClientMode, DefaultValue = "false")]
        public bool IsNetworkClientMode { get; set; }

        [Property(SecurityConfigurationConstants.SecurityServiceBeanActive, DefaultValue = "true")]
        public bool IsSecurityBeanActive { get; set; }

        public void AfterPropertiesSet(IBeanContextFactory beanContextFactory)
        {
       		beanContextFactory.RegisterAnonymousBean<SecurityActivation>().Autowireable<ISecurityActivation>();

    		beanContextFactory.RegisterAnonymousBean<SecurityScopeProvider>().Autowireable(typeof(ISecurityScopeProvider), typeof(ISecurityScopeChangeListenerExtendable));

            if (IsNetworkClientMode && IsSecurityBeanActive)
            {
                beanContextFactory.RegisterBean<ClientServiceBean>("securityServiceWCF")
                       .PropertyValue("Interface", typeof(ISecurityService))
                       .PropertyValue("SyncRemoteInterface", typeof(ISecurityServiceWCF))
                       .PropertyValue("AsyncRemoteInterface", typeof(ISecurityClient))
                       .Autowireable<ISecurityService>();
                //beanContextFactory.registerBean<SecurityServiceDelegate>("securityService").autowireable<ISecurityService>();
            }
            else if (!IsNetworkClientMode)
            {
                //beanContextFactory.registerBean<SecurityScopeProvider>("securityScopeProvider").autowireable<ISecurityScopeProvider>();
            }
            beanContextFactory.RegisterBean<AESEncryption>("encryption").Autowireable<IEncryption>();
        }
    }
}
