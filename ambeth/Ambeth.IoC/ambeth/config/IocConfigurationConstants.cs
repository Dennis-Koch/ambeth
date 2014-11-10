using De.Osthus.Ambeth.Annotation;
using System;

namespace De.Osthus.Ambeth.Config
{
    [ConfigurationConstants]
    public sealed class IocConfigurationConstants
    {
        public const String TrackDeclarationTrace = "ambeth.ioc.declaration.trace.active";

        //@ConfigurationConstantDescription("Allows to monitor all ioc managed beans via JMX. Valid values: \"true\" or \"false\". Default is \"true\".")
        public const String MonitorBeansActive = "ambeth.ioc.monitoring.active";

        public const String DebugModeActive = "ambeth.ioc.debug.active";

        private IocConfigurationConstants()
        {
        }
    }
}