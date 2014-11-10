using De.Osthus.Ambeth.Annotation;
using System;
using System.ServiceModel;

namespace De.Osthus.Ambeth.Log
{
    [ConfigurationConstants]
    public sealed class LogConfigurationConstants
    {
        public const String LogFile = "ambeth.log.file";

        private LogConfigurationConstants()
        {
            // intended blank
        }
    }
}
