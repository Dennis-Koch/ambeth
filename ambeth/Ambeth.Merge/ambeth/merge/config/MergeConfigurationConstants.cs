using System;

namespace De.Osthus.Ambeth.Merge.Config
{
    public class MergeConfigurationConstants
    {
        public const String EntityFactoryType = "ambeth.merge.entityfactory.type";

        public const String MergeServiceBeanActive = "merge.service.active";

        public const String MergeServiceMockType = "merge.service.mock.type";

        public const String FieldBasedMergeActive = "ambeth.merge.fieldbased.active";

        public const String ExactVersionForOptimisticLockingRequired = "ambeth.merge.exact.version.required";

        public const String ValueObjectConfigValidationActive = "ambeth.mapping.config.validate.active";

        private MergeConfigurationConstants()
        {
            // Intended blank
        }
    }
}