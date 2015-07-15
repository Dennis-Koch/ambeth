package de.osthus.ambeth.merge.config;

import de.osthus.ambeth.annotation.ConfigurationConstantDescription;
import de.osthus.ambeth.annotation.ConfigurationConstants;
import de.osthus.ambeth.changecontroller.IChangeController;
import de.osthus.ambeth.merge.IEntityFactory;
import de.osthus.ambeth.objectcollector.ICollectableControllerExtendable;

@ConfigurationConstants
public final class MergeConfigurationConstants
{
	/**
	 * Defines which {@link IEntityFactory} should be used. Has to be a fully qualified class name. If not specified a default {@link IEntityFactory} will be
	 * used.
	 */
	public static final String EntityFactoryType = "ambeth.merge.entityfactory.type";

	/**
	 * Defines which {@link IChangeController} should be used. Has to be a fully qualified class name. If not specified a default {@link IChangeController} will
	 * be used.
	 */
	public static final String ChangeControllerType = "ambeth.merge.changecontroller.type";

	@ConfigurationConstantDescription("TODO")
	public static final String FieldBasedMergeActive = "ambeth.merge.fieldbased.active";

	/**
	 * Defines whether the exact version is needed to avoid an OptimisticLockException during update of an entity or all versions equals or higher are accepted.
	 * Valid values are "true" and "false", default is "false".
	 */
	public static final String ExactVersionForOptimisticLockingRequired = "ambeth.merge.exact.version.required";

	/**
	 * Defines how to handle entity changes and the reload of entities to the cache. By default ("false") the version is not updated in changed entities,
	 * forcing the DataChangeEvent to refresh the cached object from the real data. If the version is updated the cache will not updated because the DCE will
	 * 'see' an already valid object - but it is NOT valid because it may not contain bi-directional information which can only be resolved by reloading the
	 * object from persistence layer. Valid values are "true" and "false", default is "false".
	 */
	public static final String AlwaysUpdateVersionInChangedEntities = "ambeth.merge.update.version.always";

	/**
	 * Allows to check the value-object mapping during parsing the mapping file. Valid values: "true" or "false". Default is "false".
	 */
	public static final String ValueObjectConfigValidationActive = "ambeth.mapping.config.validate.active";

	/**
	 * If security is enabled all security relevant tasks are checked whether they are allowed by the current user.
	 */
	public static final String SecurityActive = "ambeth.security.active";
	/**
	 * Switches the object EDBL (Event Driven Business Logic) on ("true") or off ("false"). Default is "true". Business rules and validations are activated or
	 * deactivated.
	 * 
	 * @see ICollectableControllerExtendable
	 */
	@ConfigurationConstantDescription("TODO")
	public static final String edblActive = "ambeth.merge.edbl.active";

	/**
	 * If true Ambeth encapsulates a specific Prefetch-API call within the "lazy transaction" pattern. If false each cache miss within a single Prefetch-API
	 * usecase may acquire a individual short-term transaction. It is recommended to let this flag on its default value because there are very few cases where
	 * deactivating this property makes sense. Default value is "true".
	 */
	public static final String PrefetchInLazyTransactionActive = "ambeth.prefetch.lazytransaction.active";

	private MergeConfigurationConstants()
	{
		// Intended blank
	}
}
