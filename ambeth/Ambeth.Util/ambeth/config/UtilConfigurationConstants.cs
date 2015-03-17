using De.Osthus.Ambeth.Annotation;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace De.Osthus.Ambeth.Config
{
    [ConfigurationConstants]
    public sealed class UtilConfigurationConstants
    {
       	public const String BootstrapPropertyFile = "property.file";

        public const String DebugMode = "ambeth.debug.active";

        public const String ForkName = "ambeth.test.forkname";

        private UtilConfigurationConstants()
        {
        }
    }
}
