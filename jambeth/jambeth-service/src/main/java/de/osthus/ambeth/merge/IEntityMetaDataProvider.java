package de.osthus.ambeth.merge;

import java.util.List;

import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.merge.model.IEntityMetaData;

public interface IEntityMetaDataProvider
{
	IEntityMetaData getMetaData(Class<?> entityType);

	IEntityMetaData getMetaData(Class<?> entityType, boolean tryOnly);

	IList<IEntityMetaData> getMetaData(List<Class<?>> entityTypes);

	IList<Class<?>> findMappableEntityTypes();

	IValueObjectConfig getValueObjectConfig(Class<?> valueType);

	IValueObjectConfig getValueObjectConfig(String xmlTypeName);

	List<Class<?>> getValueObjectTypesByEntityType(Class<?> entityType);

	Class<?>[] getEntityPersistOrder();

	String buildDotGraph();
}
