using System;
using System.Collections.Generic;
using De.Osthus.Ambeth.Util;
using De.Osthus.Ambeth.Proxy;
using De.Osthus.Ambeth.Service;
using System.Reflection;
using De.Osthus.Ambeth.Config;
using De.Osthus.Ambeth.Ioc.Factory;

namespace De.Osthus.Ambeth.Ioc
{
    public class WCFBootstrapModule : IInitializingModule
    {
        [Property(ServiceWCFConfigurationConstants.TransferObjectsScope, DefaultValue = ".*\\.Transfer.*")]
        public virtual String TransferObjectsScope { get; set; }

        public virtual void AfterPropertiesSet(IBeanContextFactory beanContextFactory)
        {
            String[] transferObjectsScopes = TransferObjectsScope.Split(';');
            foreach (Assembly assembly in AssemblyHelper.Assemblies)
            {
                FullServiceModelProvider.ShareServiceModel(assembly, transferObjectsScopes);
            }
        }
    }
}
