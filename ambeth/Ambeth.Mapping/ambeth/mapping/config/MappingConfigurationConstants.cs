using De.Osthus.Ambeth.Annotation;
using System;

namespace De.Osthus.Ambeth.Mapping.Config
{
    [ConfigurationConstants]
    public sealed class MappingConfigurationConstants
    {
      	public const String InitDirectRelationsInBusinessObjects = "mapping.businessobject.resolve.relations";

        private MappingConfigurationConstants()
        {
            // Intended blank
        }
    }
}
