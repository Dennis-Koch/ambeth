using De.Osthus.Ambeth.Ioc.Factory;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Rest;
using De.Osthus.Ambeth.Service;

namespace De.Osthus.Ambeth.Ioc
{
    public class RESTBootstrapModule : IInitializingModule
    {
        [LogInstance]
		public ILogger Log { private get; set; }

        public virtual void AfterPropertiesSet(IBeanContextFactory beanContextFactory)
        {
            //beanContextFactory.registerBean<SecurityScopeProvider>("securityScopeProviderAmbeth").autowireable<ISecurityScopeProvider>();
            beanContextFactory.RegisterBean<RESTClientServiceFactory>("clientServiceFactory").Autowireable<IClientServiceFactory>();
            beanContextFactory.RegisterBean<AuthenticationHolder>("authentificationHolder").Autowireable<IAuthenticationHolder>();
        }
    }
}
