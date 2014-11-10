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
       	public static readonly String BootstrapPropertyFile = "property.file";

        public static readonly String DebugMode = "ambeth.debug.active";

        private UtilConfigurationConstants()
        {
        }
    }
}
