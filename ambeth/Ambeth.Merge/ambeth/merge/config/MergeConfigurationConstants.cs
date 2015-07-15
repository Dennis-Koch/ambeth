using De.Osthus.Ambeth.Annotation;
using System;

namespace De.Osthus.Ambeth.Merge.Config
{
    [ConfigurationConstants]
    public sealed class MergeConfigurationConstants
    {
		/// <summary>
		/// Defines which {@link IEntityFactory} should be used. Has to be a fully qualified class name. If not specified a default {@link IEntityFactory} will be used.
		/// </summary>
        public const String EntityFactoryType = "ambeth.merge.entityfactory.type";

        public const String MergeServiceBeanActive = "merge.service.active";

        public const String MergeServiceMockType = "merge.service.mock.type";

        public const String FieldBasedMergeActive = "ambeth.merge.fieldbased.active";

        public const String ExactVersionForOptimisticLockingRequired = "ambeth.merge.exact.version.required";

        public const String AlwaysUpdateVersionInChangedEntities = "ambeth.merge.update.version.always";

		/// <summary>
		/// Allows to check the value-object mapping during parsing the mapping file. Valid values: "true" or "false". Default is "false".
		/// </summary>
        public const String ValueObjectConfigValidationActive = "ambeth.mapping.config.validate.active";

		/// <summary>
		/// If security is enabled all security relevant tasks are checked whether they are allowed by the current user.
		/// </summary>
        public const String SecurityActive = "ambeth.security.active";

		///
		/// <summary>
		/// If true Ambeth encapsulates a specific Prefetch-API call within the "lazy transaction" pattern. If false each cache miss within a single Prefetch-API
		/// usecase may acquire a individual short-term transaction. It is recommended to let this flag on its default value because there are very few cases where
		/// deactivating this property makes sense. Default value is "true".
		/// </summary>
		public const String PrefetchInLazyTransactionActive = "ambeth.prefetch.lazytransaction.active";

        private MergeConfigurationConstants()
        {
            // Intended blank
        }
    }
}