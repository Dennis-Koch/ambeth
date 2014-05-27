package de.osthus.ambeth.merge;

import de.osthus.ambeth.merge.model.IEntityMetaData;

/**
 * Creates (enhanced) instances of classes and interfaces.
 * 
 */
public interface IEntityFactory
{
	<T> T createEntity(Class<T> entityType);

	Object createEntity(IEntityMetaData metaData);

	boolean supportsEnhancement(Class<?> enhancementType);
}
