using De.Osthus.Ambeth.Annotation;
using System;

namespace De.Osthus.Minerva.Core.Config
{
    [ConfigurationConstants]
    public sealed class MinervaCoreConfigurationConstants
    {
        public const String EntityProxyActive = "minerva.entityproxy.active";

        public const String AllowConcurrentCommands = "minerva.commands.concurrent.active";

        private MinervaCoreConfigurationConstants()
        {
            // intended blank
        }
    }
}
