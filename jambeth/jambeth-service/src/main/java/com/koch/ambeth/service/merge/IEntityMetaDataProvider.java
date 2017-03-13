package com.koch.ambeth.service.merge;

import java.util.List;

import com.koch.ambeth.service.merge.model.IEntityMetaData;
import com.koch.ambeth.util.collections.IList;

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
