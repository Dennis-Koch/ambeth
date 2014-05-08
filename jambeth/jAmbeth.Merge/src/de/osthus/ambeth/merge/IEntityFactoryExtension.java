package de.osthus.ambeth.merge;

import de.osthus.ambeth.merge.model.IEntityMetaData;

public interface IEntityFactoryExtension
{
	<T> Class<? extends T> getMappedEntityType(Class<T> type);

	Object postProcessMappedEntity(Class<?> originalType, IEntityMetaData metaData, Object mappedEntity);
}
