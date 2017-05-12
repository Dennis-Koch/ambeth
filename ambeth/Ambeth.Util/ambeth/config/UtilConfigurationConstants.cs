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

		/// <summary>
		/// Allows to run Ambeth in debug mode. This e.g. enables more exception to
		/// ease debugging. Valid values: "true" or "false". Default is "false".
		/// </summary>
        public const String DebugMode = "ambeth.debug.active";

        public const String ForkName = "ambeth.test.forkname";

        private UtilConfigurationConstants()
        {
        }
    }
}
