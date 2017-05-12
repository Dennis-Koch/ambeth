using De.Osthus.Ambeth.Annotation;
using System;

namespace De.Osthus.Ambeth.Config
{
    [ConfigurationConstants]
    public sealed class XmlConfigurationConstants
    {
        public const String PackageScanPatterns = "ambeth.xml.transfer.pattern";

        private XmlConfigurationConstants()
        {
            // Intended blank
        }
    }
}
