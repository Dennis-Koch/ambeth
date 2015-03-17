using De.Osthus.Ambeth.Annotation;
using System;

namespace De.Osthus.Ambeth.Security.Config
{
    [ConfigurationConstants]
    public sealed class SecurityConfigurationConstants
    {
        public const String EnryptionPassword = "encryption.password";

        public const String SecurityServiceBeanActive = "security.service.active";

        private SecurityConfigurationConstants()
        {
            // intended blank
        }
    }
}
