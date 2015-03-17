using De.Osthus.Ambeth.Annotation;
using System;

namespace De.Osthus.Ambeth.Privilege.Config
{
    [ConfigurationConstants]
    public sealed class PrivilegeConfigurationConstants
    {
        public const String PrivilegeServiceBeanActive = "privilege.service.active";

        private PrivilegeConfigurationConstants()
        {
            // intended blank
        }
    }
}